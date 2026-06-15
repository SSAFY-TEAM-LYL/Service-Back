package com.lyl.domain.problem;

import java.util.List;

public record ProblemDetail(
        String id,
        String title,
        String description,
        String inputFormat,
        String outputFormat,
        List<ProblemConstraint> constraints,
        List<ProblemSample> samples,
        Integer timeLimitMs
) {
}
