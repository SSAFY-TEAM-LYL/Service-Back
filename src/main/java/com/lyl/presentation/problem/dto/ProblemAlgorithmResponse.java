package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemAlgorithm;

public record ProblemAlgorithmResponse(
        String code,
        String label
) {

    public static ProblemAlgorithmResponse from(ProblemAlgorithm algorithm) {
        return new ProblemAlgorithmResponse(algorithm.code(), algorithm.label());
    }
}
