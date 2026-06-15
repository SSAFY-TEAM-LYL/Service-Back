package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemSummary;
import java.time.OffsetDateTime;

public record ProblemSummaryResponse(
        String id,
        String title,
        Integer timeLimitMs,
        OffsetDateTime createdAt
) {

    public static ProblemSummaryResponse from(ProblemSummary summary) {
        return new ProblemSummaryResponse(
                summary.id(),
                summary.title(),
                summary.timeLimitMs(),
                summary.createdAt()
        );
    }
}
