package com.lyl.application.submission;

import com.lyl.domain.submission.SubmissionStatus;

public record Judge0SubmissionResult(
        String token,
        SubmissionStatus status,
        boolean completed,
        Integer timeMs,
        Integer memoryKb,
        String stderrText,
        String compileOutput,
        String message
) {
}
