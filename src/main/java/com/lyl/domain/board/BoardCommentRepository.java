package com.lyl.domain.board;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoardCommentRepository {

    BoardComment save(BoardComment boardComment);

    List<BoardComment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    List<BoardComment> findPageByPostId(Long postId, LocalDateTime cursorCreatedAt, Long cursorId, int size);

    Optional<BoardComment> findById(Long id);

    void delete(BoardComment boardComment);
}
