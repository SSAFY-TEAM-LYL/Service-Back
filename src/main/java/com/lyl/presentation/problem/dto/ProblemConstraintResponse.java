package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemConstraint;

public record ProblemConstraintResponse(
        String name,
        Long minValue,
        Long maxValue,
        String description
) {

    public static ProblemConstraintResponse from(ProblemConstraint constraint) {
        return new ProblemConstraintResponse(
                constraint.name(),
                constraint.minValue(),
                constraint.maxValue(),
                constraint.description()
        );
    }
}
