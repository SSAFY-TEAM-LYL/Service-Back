package com.lyl.presentation.submission.dto;

import com.lyl.domain.submission.SubmissionReview;
import java.time.LocalDateTime;

public record SubmissionReviewResponse(
        Long id,
        Long submissionId,
        String content,
        Long authorId,
        String author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SubmissionReviewResponse from(SubmissionReview review) {
        return new SubmissionReviewResponse(
                review.getId(),
                review.getSubmission().getId(),
                review.getContent(),
                review.getAuthor().getId(),
                review.getAuthor().getNickname(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
