package com.lyl.application.auth;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.OAuthAccount;
import com.lyl.domain.member.OAuthAccountRepository;
import com.lyl.domain.member.OAuthProvider;
import com.lyl.domain.member.exception.DeletedMemberException;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.domain.member.exception.OAuthAccountConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final MemberRepository memberRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member login(String registrationId, OAuth2User oauth2User) {
        OAuthProvider provider = OAuthProvider.fromRegistrationId(registrationId);
        OAuthUserProfile profile = OAuthUserProfile.from(provider, oauth2User.getAttributes());

        return oauthAccountRepository.findByProviderAndProviderUserId(provider, profile.providerUserId())
                .map(OAuthAccount::getMember)
                .map(this::validateActiveMember)
                .orElseGet(() -> createMemberWithOAuthAccount(profile));
    }

    @Transactional
    public Member restore(String registrationId, OAuth2User oauth2User) {
        OAuthProvider provider = OAuthProvider.fromRegistrationId(registrationId);
        OAuthUserProfile profile = OAuthUserProfile.from(provider, oauth2User.getAttributes());

        Member member = oauthAccountRepository.findByProviderAndProviderUserId(provider, profile.providerUserId())
                .map(OAuthAccount::getMember)
                .orElseThrow(MemberNotFoundException::new);

        if (member.isDeleted()) {
            member.restore();
        }
        if (member.getProfileImageUrl() == null && profile.profileImageUrl() != null) {
            member.updateProfile(member.getNickname(), profile.profileImageUrl());
        }
        return member;
    }

    private Member validateActiveMember(Member member) {
        if (member.isDeleted()) {
            throw new DeletedMemberException();
        }
        return member;
    }

    private Member createMemberWithOAuthAccount(OAuthUserProfile profile) {
        memberRepository.findByEmailIncludingDeleted(profile.email())
                .ifPresent(member -> {
                    if (member.isDeleted()) {
                        throw new DeletedMemberException();
                    }
                    throw new OAuthAccountConflictException();
                });

        Member member = memberRepository.save(new Member(
                profile.email(),
                profile.nickname(),
                passwordEncoder.encode(UUID.randomUUID().toString()),
                profile.profileImageUrl()
        ));
        oauthAccountRepository.save(new OAuthAccount(
                member,
                profile.provider(),
                profile.providerUserId(),
                profile.email()
        ));
        return member;
    }
}
