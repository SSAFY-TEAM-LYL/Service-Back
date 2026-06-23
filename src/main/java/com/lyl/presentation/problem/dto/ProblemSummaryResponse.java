package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemSummary;
import java.time.OffsetDateTime;

public record ProblemSummaryResponse(
        String id,
        Long problemNumber,
        String title,
        String difficulty,
        Integer timeLimitMs,
        OffsetDateTime createdAt
) {

    public static ProblemSummaryResponse from(ProblemSummary summary) {
        return new ProblemSummaryResponse(
                summary.id(),
                summary.problemNumber(),
                summary.title(),
                summary.difficulty(),
                summary.timeLimitMs(),
                summary.createdAt()
        );
    }
}
