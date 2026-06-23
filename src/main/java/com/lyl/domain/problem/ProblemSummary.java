package com.lyl.domain.problem;

import java.time.OffsetDateTime;

public record ProblemSummary(
        String id,
        Long problemNumber,
        String title,
        String difficulty,
        Integer timeLimitMs,
        OffsetDateTime createdAt
) {
}
