package com.lyl.domain.problem;

import java.util.List;

public record ProblemDetail(
        String id,
        Long problemNumber,
        String title,
        String description,
        String inputFormat,
        String outputFormat,
        String difficulty,
        List<ProblemConstraint> constraints,
        List<ProblemSample> samples,
        Integer timeLimitMs
) {
}
