package com.lyl.presentation.submission.dto;

import com.lyl.domain.submission.SubmissionStatus;
import com.lyl.domain.submission.SubmissionTestCaseResult;

public record SubmissionTestCaseResultResponse(
        int caseSeq,
        SubmissionStatus status,
        Integer timeMs,
        Integer memoryKb,
        String message
) {

    public static SubmissionTestCaseResultResponse from(SubmissionTestCaseResult result) {
        return new SubmissionTestCaseResultResponse(
                result.getCaseSeq(),
                result.getStatus(),
                result.getTimeMs(),
                result.getMemoryKb(),
                result.failureMessage()
        );
    }
}
