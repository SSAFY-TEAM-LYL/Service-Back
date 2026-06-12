package com.lyl.domain.board;

import java.util.List;
import java.util.Optional;

public interface BoardPostRepository {

    BoardPost save(BoardPost boardPost);

    List<BoardPost> findAllOrderByCreatedAtDesc();

    List<BoardPost> findAllByCategoryOrderByCreatedAtDesc(BoardCategory category);

    Optional<BoardPost> findById(Long id);

    void delete(BoardPost boardPost);
}
