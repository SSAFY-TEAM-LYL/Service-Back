package com.lyl.application.common;

import java.time.LocalDateTime;

public record Cursor(
        LocalDateTime createdAt,
        Long id
) {
}
