package com.lyl.presentation.member.dto;

import com.lyl.domain.member.Member;

public record MemberRankingResponse(
        int rank,
        Long memberId,
        String nickname,
        String profileImageUrl,
        int xp,
        int level
) {

    public static MemberRankingResponse from(Member member, int rank) {
        return new MemberRankingResponse(
                rank,
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getXp(),
                member.getLevel()
        );
    }
}
