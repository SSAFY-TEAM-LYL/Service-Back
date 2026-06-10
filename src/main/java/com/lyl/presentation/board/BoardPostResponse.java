package com.lyl.presentation.board;

import com.lyl.domain.board.BoardPost;
import java.time.format.DateTimeFormatter;

public record BoardPostResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String author,
        String date,
        int comments,
        long views
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static BoardPostResponse from(BoardPost post) {
        return new BoardPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getAuthor().getNickname(),
                post.getCreatedAt().format(DATE_FORMATTER),
                post.getCommentCount(),
                post.getViewCount()
        );
    }
}
