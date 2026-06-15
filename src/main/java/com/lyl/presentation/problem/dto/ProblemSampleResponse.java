package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemSample;

public record ProblemSampleResponse(
        String inputText,
        String expectedOutput,
        String description
) {

    public static ProblemSampleResponse from(ProblemSample sample) {
        return new ProblemSampleResponse(
                sample.inputText(),
                sample.expectedOutput(),
                sample.description()
        );
    }
}
