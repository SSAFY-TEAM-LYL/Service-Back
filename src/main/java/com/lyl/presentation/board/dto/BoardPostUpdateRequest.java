package com.lyl.presentation.board.dto;

import com.lyl.domain.board.BoardCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardPostUpdateRequest(
        @NotNull(message = "게시판 카테고리를 선택해주세요.")
        BoardCategory category,

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 150, message = "제목은 150자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        String content
) {
}
