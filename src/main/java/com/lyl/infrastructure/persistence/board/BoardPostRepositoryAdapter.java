package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardPostRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardPostRepositoryAdapter implements BoardPostRepository {

    private final SpringDataBoardPostRepository repository;

    @Override
    public BoardPost save(BoardPost boardPost) {
        return repository.save(boardPost);
    }

    @Override
    public List<BoardPost> findAllOrderByCreatedAtDesc() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<BoardPost> findById(Long id) {
        return repository.findById(id);
    }
}
