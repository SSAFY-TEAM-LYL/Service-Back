package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemSummary;
import java.time.OffsetDateTime;

public record AdminProblemBankProblemResponse(
        String id,
        Long problemNumber,
        String title,
        String difficulty,
        Integer timeLimitMs,
        OffsetDateTime createdAt,
        boolean published
) {

    public static AdminProblemBankProblemResponse from(ProblemSummary summary, boolean published) {
        return new AdminProblemBankProblemResponse(
                summary.id(),
                summary.problemNumber(),
                summary.title(),
                summary.difficulty(),
                summary.timeLimitMs(),
                summary.createdAt(),
                published
        );
    }
}
