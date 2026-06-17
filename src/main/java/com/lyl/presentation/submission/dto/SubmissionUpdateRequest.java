package com.lyl.presentation.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionUpdateRequest(
        @NotBlank(message = "언어를 선택해주세요.")
        String language,

        @NotBlank(message = "제출할 코드를 입력해주세요.")
        @Size(max = 200_000, message = "코드는 200,000자 이하여야 합니다.")
        String sourceCode
) {
}
