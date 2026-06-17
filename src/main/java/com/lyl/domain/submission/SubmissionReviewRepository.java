package com.lyl.domain.submission;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubmissionReviewRepository {

    SubmissionReview save(SubmissionReview review);

    Optional<SubmissionReview> findById(Long id);

    List<SubmissionReview> findPageBySubmissionId(Long submissionId, LocalDateTime cursorCreatedAt, Long cursorId, int size);

    void delete(SubmissionReview review);
}
