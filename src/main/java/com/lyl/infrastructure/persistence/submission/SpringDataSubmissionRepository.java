package com.lyl.infrastructure.persistence.submission;

import com.lyl.domain.submission.Submission;
import com.lyl.domain.submission.SubmissionStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataSubmissionRepository extends JpaRepository<Submission, Long> {

    @EntityGraph(attributePaths = {"member", "testCaseResults"})
    Optional<Submission> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"member", "testCaseResults"})
    Optional<Submission> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);

    @Query("""
            select s.id
            from Submission s
            where s.deletedAt is null
              and s.problemId = :problemId
              and (:memberId is null or s.member.id = :memberId)
              and (
                  :cursorCreatedAt is null
                  or s.createdAt < :cursorCreatedAt
                  or (s.createdAt = :cursorCreatedAt and s.id < :cursorId)
              )
            order by s.createdAt desc, s.id desc
            """)
    List<Long> findProblemSubmissionIdsPage(
            @Param("problemId") String problemId,
            @Param("memberId") Long memberId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select s.id
            from Submission s
            where s.deletedAt is null
              and s.status in :statuses
            order by s.createdAt asc, s.id asc
            """)
    List<Long> findInProgressSubmissionIds(
            Collection<SubmissionStatus> statuses,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"member", "testCaseResults"})
    List<Submission> findByIdInAndDeletedAtIsNull(Collection<Long> ids);
}
