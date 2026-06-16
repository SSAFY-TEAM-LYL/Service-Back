package com.lyl.presentation.submission.dto;

import com.lyl.domain.submission.Submission;
import com.lyl.domain.submission.SubmissionLanguage;
import com.lyl.domain.submission.SubmissionStatus;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record SubmissionResponse(
        Long id,
        String problemId,
        SubmissionLanguage language,
        SubmissionStatus status,
        int totalTestCount,
        int passedTestCount,
        Integer maxTimeMs,
        Integer maxMemoryKb,
        Integer firstFailedCaseSeq,
        String errorMessage,
        LocalDateTime submittedAt,
        LocalDateTime judgedAt,
        List<SubmissionTestCaseResultResponse> testCaseResults
) {

    public static SubmissionResponse from(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getTotalTestCount(),
                submission.getPassedTestCount(),
                submission.getMaxTimeMs(),
                submission.getMaxMemoryKb(),
                submission.getFirstFailedCaseSeq(),
                submission.getErrorMessage(),
                submission.getSubmittedAt(),
                submission.getJudgedAt(),
                submission.getTestCaseResults().stream()
                        .sorted(Comparator.comparingInt(result -> result.getCaseSeq()))
                        .map(SubmissionTestCaseResultResponse::from)
                        .toList()
        );
    }
}
