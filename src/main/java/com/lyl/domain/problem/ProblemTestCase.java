package com.lyl.domain.problem;

public record ProblemTestCase(
        int seq,
        String input,
        String expected,
        String category
) {
}
