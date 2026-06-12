package com.lyl.infrastructure.persistence.board;

import com.lyl.domain.board.BoardComment;
import com.lyl.domain.board.BoardCommentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardCommentRepositoryAdapter implements BoardCommentRepository {

    private final SpringDataBoardCommentRepository repository;

    @Override
    public BoardComment save(BoardComment boardComment) {
        return repository.save(boardComment);
    }

    @Override
    public List<BoardComment> findAllByPostIdOrderByCreatedAtAsc(Long postId) {
        return repository.findAllByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
    }

    @Override
    public List<BoardComment> findPageByPostId(Long postId, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        return repository.findCursorPageByPostId(postId, cursorCreatedAt, cursorId, PageRequest.of(0, size));
    }

    @Override
    public Optional<BoardComment> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public void delete(BoardComment boardComment) {
        boardComment.delete();
        repository.save(boardComment);
    }
}
