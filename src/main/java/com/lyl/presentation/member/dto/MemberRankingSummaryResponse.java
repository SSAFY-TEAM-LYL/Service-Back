package com.lyl.presentation.member.dto;

import java.util.List;

public record MemberRankingSummaryResponse(
        List<MemberRankingResponse> items,
        MemberRankingResponse myRanking
) {
}
