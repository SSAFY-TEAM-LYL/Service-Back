package com.lyl.presentation.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionReviewCreateRequest(
        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Size(max = 5_000, message = "리뷰는 5,000자 이하여야 합니다.")
        String content
) {
}
