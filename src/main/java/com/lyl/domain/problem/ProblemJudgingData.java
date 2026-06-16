package com.lyl.domain.problem;

import java.util.List;

public record ProblemJudgingData(
        String id,
        Integer timeLimitMs,
        List<ProblemTestCase> testCases
) {
}
