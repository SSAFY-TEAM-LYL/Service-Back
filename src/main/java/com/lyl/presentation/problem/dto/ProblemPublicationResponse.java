package com.lyl.presentation.problem.dto;

import com.lyl.domain.problem.ProblemPublication;
import java.time.LocalDateTime;

public record ProblemPublicationResponse(
        Long id,
        String problemId,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProblemPublicationResponse from(ProblemPublication publication) {
        return new ProblemPublicationResponse(
                publication.getId(),
                publication.getProblemId(),
                publication.isPublished(),
                publication.getCreatedAt(),
                publication.getUpdatedAt()
        );
    }
}
