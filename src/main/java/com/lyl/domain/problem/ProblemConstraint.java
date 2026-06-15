package com.lyl.domain.problem;

public record ProblemConstraint(
        String name,
        Long minValue,
        Long maxValue,
        String description
) {
}
