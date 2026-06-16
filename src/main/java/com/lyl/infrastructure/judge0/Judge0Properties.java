package com.lyl.infrastructure.judge0;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.judge0")
public record Judge0Properties(
        String baseUrl,
        String authHeader,
        String authToken,
        int requestTimeoutSeconds,
        int pollBatchSize,
        boolean enabled
) {
    public Judge0Properties {
        if (authHeader == null || authHeader.isBlank()) {
            authHeader = "X-Auth-Token";
        }
        if (requestTimeoutSeconds <= 0) {
            requestTimeoutSeconds = 5;
        }
        if (pollBatchSize <= 0) {
            pollBatchSize = 20;
        }
    }
}
