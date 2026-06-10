package com.lyl.presentation.board;

import com.lyl.domain.board.BoardComment;
import java.time.format.DateTimeFormatter;

public record BoardCommentResponse(
        Long id,
        Long postId,
        String content,
        Long authorId,
        String author,
        String date
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static BoardCommentResponse from(BoardComment comment) {
        return new BoardCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getCreatedAt().format(DATE_FORMATTER)
        );
    }
}
