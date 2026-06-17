package com.lyl.infrastructure.persistence.submission;

import com.lyl.domain.submission.SubmissionReview;
import com.lyl.domain.submission.SubmissionReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubmissionReviewRepositoryAdapter implements SubmissionReviewRepository {

    private final SpringDataSubmissionReviewRepository repository;

    @Override
    public SubmissionReview save(SubmissionReview review) {
        return repository.save(review);
    }

    @Override
    public Optional<SubmissionReview> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<SubmissionReview> findPageBySubmissionId(
            Long submissionId,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    ) {
        return repository.findCursorPageBySubmissionId(
                submissionId,
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, size)
        );
    }

    @Override
    public void delete(SubmissionReview review) {
        review.delete();
        repository.save(review);
    }
}
