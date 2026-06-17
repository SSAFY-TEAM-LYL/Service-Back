package com.lyl.infrastructure.persistence.submission;

import com.lyl.domain.submission.SubmissionReview;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataSubmissionReviewRepository extends JpaRepository<SubmissionReview, Long> {

    @EntityGraph(attributePaths = {"author", "submission"})
    Optional<SubmissionReview> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"author"})
    @Query("""
            select r
            from SubmissionReview r
            where r.deletedAt is null
              and r.submission.id = :submissionId
              and (
                    :cursorCreatedAt is null
                    or r.createdAt > :cursorCreatedAt
                    or (r.createdAt = :cursorCreatedAt and r.id > :cursorId)
                  )
            order by r.createdAt asc, r.id asc
            """)
    List<SubmissionReview> findCursorPageBySubmissionId(
            @Param("submissionId") Long submissionId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
