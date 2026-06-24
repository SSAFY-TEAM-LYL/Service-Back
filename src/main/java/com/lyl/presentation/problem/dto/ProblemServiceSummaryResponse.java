package com.lyl.presentation.problem.dto;

public record ProblemServiceSummaryResponse(
        long publishedProblemCount,
        long todaySubmissionCount
) {
}
