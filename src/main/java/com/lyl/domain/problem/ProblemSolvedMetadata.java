package com.lyl.domain.problem;

import java.util.List;

public record ProblemSolvedMetadata(
        String problemId,
        String difficulty,
        List<String> algorithms
) {
}
