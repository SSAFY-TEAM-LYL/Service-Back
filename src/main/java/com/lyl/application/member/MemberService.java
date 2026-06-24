package com.lyl.application.member;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.OAuthAccountRepository;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.presentation.auth.dto.UserResponse;
import com.lyl.presentation.member.dto.MemberRankingResponse;
import com.lyl.presentation.member.dto.MemberRankingSummaryResponse;
import com.lyl.presentation.member.dto.MemberUpdateRequest;
import com.lyl.presentation.member.dto.MemberWithdrawalRequest;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int RANKING_TOP_LIMIT = 10;

    private final MemberRepository memberRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long memberId) {
        Member member = findActiveMember(memberId);
        return UserResponse.from(member);
    }

    @Transactional(readOnly = true)
    public MemberRankingSummaryResponse findRankings(Long currentMemberId) {
        List<Member> topMembers = memberRepository.findRankingPage(0, RANKING_TOP_LIMIT);
        List<MemberRankingResponse> items = IntStream.range(0, topMembers.size())
                .mapToObj(index -> MemberRankingResponse.from(
                        topMembers.get(index),
                        index + 1
                ))
                .toList();
        return new MemberRankingSummaryResponse(items, findCurrentMemberRanking(currentMemberId));
    }

    @Transactional
    public UserResponse updateMe(Long memberId, MemberUpdateRequest request) {
        Member member = findActiveMember(memberId);

        String nickname = normalizeNickname(request.nickname(), member.getNickname());
        String profileImageUrl = normalizeProfileImageUrl(request.profileImageUrl());
        member.updateProfile(nickname, profileImageUrl);

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 8) {
                throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
            }
            member.updatePassword(passwordEncoder.encode(request.password()));
        }

        return UserResponse.from(member);
    }

    @Transactional
    public void withdrawMe(Long memberId, MemberWithdrawalRequest request) {
        Member member = findActiveMember(memberId);
        boolean oauthMember = oauthAccountRepository.existsByMemberId(member.getId());
        if (!oauthMember && (request.password() == null || request.password().isBlank())) {
            throw new BadCredentialsException("비밀번호를 입력해주세요.");
        }
        if (!oauthMember && !passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        member.delete();
    }

    private Member findActiveMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private MemberRankingResponse findCurrentMemberRanking(Long currentMemberId) {
        if (currentMemberId == null) {
            return null;
        }

        return memberRepository.findById(currentMemberId)
                .map(member -> MemberRankingResponse.from(
                        member,
                        Math.toIntExact(memberRepository.countActiveMembersAheadOf(member.getXp(), member.getId()) + 1)
                ))
                .orElse(null);
    }

    private String normalizeNickname(String nickname, String currentNickname) {
        if (nickname == null || nickname.isBlank()) {
            return currentNickname;
        }
        String trimmed = nickname.trim();
        if (trimmed.length() > 30) {
            throw new IllegalArgumentException("닉네임은 30자 이하여야 합니다.");
        }
        return trimmed;
    }

    private String normalizeProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return null;
        }
        String trimmed = profileImageUrl.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("프로필 이미지 URL은 500자 이하여야 합니다.");
        }
        return trimmed;
    }
}
