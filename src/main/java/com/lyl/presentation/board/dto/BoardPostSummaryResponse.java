package com.lyl.presentation.board.dto;

import com.lyl.domain.board.BoardCategory;
import com.lyl.domain.board.BoardPost;
import java.time.format.DateTimeFormatter;

public record BoardPostSummaryResponse(
        Long id,
        BoardCategory category,
        String categoryLabel,
        String title,
        String author,
        String date,
        int comments,
        long views
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static BoardPostSummaryResponse from(BoardPost post) {
        return new BoardPostSummaryResponse(
                post.getId(),
                post.getCategory(),
                post.getCategory().label(),
                post.getTitle(),
                post.getAuthor().getNickname(),
                post.getCreatedAt().format(DATE_FORMATTER),
                post.getCommentCount(),
                post.getViewCount()
        );
    }
}
