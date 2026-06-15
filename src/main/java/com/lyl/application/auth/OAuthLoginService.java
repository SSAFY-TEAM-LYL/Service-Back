package com.lyl.application.auth;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.OAuthAccount;
import com.lyl.domain.member.OAuthAccountRepository;
import com.lyl.domain.member.OAuthProvider;
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
                .orElseGet(() -> createMemberWithOAuthAccount(profile));
    }

    private Member createMemberWithOAuthAccount(OAuthUserProfile profile) {
        if (memberRepository.existsByEmail(profile.email())) {
            throw new OAuthAccountConflictException();
        }

        Member member = memberRepository.save(new Member(
                profile.email(),
                profile.nickname(),
                passwordEncoder.encode(UUID.randomUUID().toString())
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
