package com.lyl.presentation.board;

import jakarta.validation.constraints.NotBlank;

public record BoardCommentUpdateRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content
) {
}
