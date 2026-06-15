package com.lyl.domain.problem;

import java.time.OffsetDateTime;

public record ProblemSummary(
        String id,
        String title,
        Integer timeLimitMs,
        OffsetDateTime createdAt
) {
}
