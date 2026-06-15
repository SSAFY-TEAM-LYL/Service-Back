package com.lyl.presentation.problem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ProblemPublicationCreateRequest(
        @NotBlank(message = "문제 ID를 입력해주세요.")
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "문제 ID는 UUID 형식이어야 합니다."
        )
        String problemId
) {
}
