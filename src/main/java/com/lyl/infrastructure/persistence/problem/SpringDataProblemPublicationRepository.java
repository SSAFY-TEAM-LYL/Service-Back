package com.lyl.infrastructure.persistence.problem;

import com.lyl.domain.problem.ProblemPublication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataProblemPublicationRepository extends JpaRepository<ProblemPublication, Long> {

    Optional<ProblemPublication> findByProblemId(String problemId);

    Optional<ProblemPublication> findByProblemIdAndDeletedAtIsNull(String problemId);

    @Query("""
            select p.problemId
            from ProblemPublication p
            where p.deletedAt is null
              and p.problemId in :problemIds
            """)
    List<String> findPublishedProblemIds(@Param("problemIds") List<String> problemIds);

    @Query("""
            select p.problemId
            from ProblemPublication p
            where p.deletedAt is null
            order by p.createdAt desc, p.id desc
            """)
    List<String> findAllPublishedProblemIds();

    long countByDeletedAtIsNull();

    @Query("""
            select p
            from ProblemPublication p
            where p.deletedAt is null
              and (
                    :cursorCreatedAt is null
                    or p.createdAt < :cursorCreatedAt
                    or (p.createdAt = :cursorCreatedAt and p.id < :cursorId)
                  )
            order by p.createdAt desc, p.id desc
            """)
    List<ProblemPublication> findPublishedCursorPage(
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
