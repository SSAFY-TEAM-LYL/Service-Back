package com.lyl.presentation.auth;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
