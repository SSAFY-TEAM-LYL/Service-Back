package com.lyl.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.problem-bank.datasource")
public record ProblemBankDataSourceProperties(
        String url,
        String username,
        String password,
        String driverClassName
) {
}
