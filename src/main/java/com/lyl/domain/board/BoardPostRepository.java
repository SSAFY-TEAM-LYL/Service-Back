package com.lyl.domain.board;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BoardPostRepository {

    BoardPost save(BoardPost boardPost);

    List<BoardPost> findAllOrderByCreatedAtDesc();

    List<BoardPost> findAllByCategoryOrderByCreatedAtDesc(BoardCategory category);

    List<BoardPost> findPage(BoardCategory category, LocalDateTime cursorCreatedAt, Long cursorId, int size);

    Optional<BoardPost> findById(Long id);

    void delete(BoardPost boardPost);
}
