package com.lyl.presentation.auth.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
