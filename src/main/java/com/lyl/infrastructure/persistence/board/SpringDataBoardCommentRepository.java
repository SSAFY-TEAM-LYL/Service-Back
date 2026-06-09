package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataBoardCommentRepository extends JpaRepository<BoardComment, Long> {

    @EntityGraph(attributePaths = "author")
    List<BoardComment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    @Override
    @EntityGraph(attributePaths = {"author", "post"})
    Optional<BoardComment> findById(Long id);
}
