package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardCategory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataBoardPostRepository extends JpaRepository<BoardPost, Long> {

    @EntityGraph(attributePaths = "author")
    List<BoardPost> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "author")
    List<BoardPost> findAllByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(BoardCategory category);

    @EntityGraph(attributePaths = "author")
    @Query("""
            select p
            from BoardPost p
            where p.deletedAt is null
              and (:category is null or p.category = :category)
              and (
                    :cursorCreatedAt is null
                    or p.createdAt < :cursorCreatedAt
                    or (p.createdAt = :cursorCreatedAt and p.id < :cursorId)
                  )
            order by p.createdAt desc, p.id desc
            """)
    List<BoardPost> findCursorPage(
            @Param("category") BoardCategory category,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "author")
    Optional<BoardPost> findByIdAndDeletedAtIsNull(Long id);
}
