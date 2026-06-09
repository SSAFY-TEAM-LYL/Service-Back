package com.lyl.domain.board;

import java.util.List;
import java.util.Optional;

public interface BoardCommentRepository {

    BoardComment save(BoardComment boardComment);

    List<BoardComment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    Optional<BoardComment> findById(Long id);

    void delete(BoardComment boardComment);
}
