package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemDetail;
import java.util.List;

public record ProblemDetailResponse(
        String id,
        Long problemNumber,
        String title,
        String description,
        String inputFormat,
        String outputFormat,
        String difficulty,
        List<ProblemConstraintResponse> constraints,
        List<ProblemSampleResponse> samples,
        Integer timeLimitMs
) {

    public static ProblemDetailResponse from(ProblemDetail detail) {
        return new ProblemDetailResponse(
                detail.id(),
                detail.problemNumber(),
                detail.title(),
                detail.description(),
                detail.inputFormat(),
                detail.outputFormat(),
                detail.difficulty(),
                detail.constraints().stream()
                        .map(ProblemConstraintResponse::from)
                        .toList(),
                detail.samples().stream()
                        .map(ProblemSampleResponse::from)
                        .toList(),
                detail.timeLimitMs()
        );
    }
}
