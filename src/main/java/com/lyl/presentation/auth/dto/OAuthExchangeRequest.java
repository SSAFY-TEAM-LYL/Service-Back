package com.lyl.presentation.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthExchangeRequest(
        @NotBlank(message = "OAuth 로그인 코드는 필수입니다.")
        String code
) {
}
