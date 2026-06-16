package com.lyl.application.submission;

import com.lyl.domain.problem.ProblemTestCase;
import com.lyl.domain.submission.SubmissionLanguage;

public record Judge0SubmissionRequest(
        int caseSeq,
        SubmissionLanguage language,
        String sourceCode,
        ProblemTestCase testCase,
        int timeLimitMs
) {
}
