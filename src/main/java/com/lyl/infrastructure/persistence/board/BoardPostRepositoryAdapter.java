package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardCategory;
import com.lyl.domain.board.BoardPostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        return repository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Override
    public List<BoardPost> findAllByCategoryOrderByCreatedAtDesc(BoardCategory category) {
        return repository.findAllByCategoryAndDeletedAtIsNullOrderByCreatedAtDesc(category);
    }

    @Override
    public List<BoardPost> findPage(BoardCategory category, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        return repository.findCursorPage(category, cursorCreatedAt, cursorId, PageRequest.of(0, size));
    }

    @Override
    public Optional<BoardPost> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public void delete(BoardPost boardPost) {
        boardPost.delete();
        repository.save(boardPost);
    }
}
