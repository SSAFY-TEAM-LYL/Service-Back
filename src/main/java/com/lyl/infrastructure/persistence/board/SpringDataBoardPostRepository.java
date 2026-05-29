package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataBoardPostRepository extends JpaRepository<BoardPost, Long> {

    @EntityGraph(attributePaths = "author")
    List<BoardPost> findAllByOrderByCreatedAtDesc();

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<BoardPost> findById(Long id);
}
