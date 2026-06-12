package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardComment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataBoardCommentRepository extends JpaRepository<BoardComment, Long> {

    @EntityGraph(attributePaths = "author")
    List<BoardComment> findAllByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);

    @EntityGraph(attributePaths = "author")
    @Query("""
            select c
            from BoardComment c
            where c.deletedAt is null
              and c.post.id = :postId
              and (
                    :cursorCreatedAt is null
                    or c.createdAt > :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.id > :cursorId)
                  )
            order by c.createdAt asc, c.id asc
            """)
    List<BoardComment> findCursorPageByPostId(
            @Param("postId") Long postId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"author", "post"})
    Optional<BoardComment> findByIdAndDeletedAtIsNull(Long id);
}
