package com.lyl.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableConfigurationProperties(ProblemBankDataSourceProperties.class)
public class ProblemBankDataSourceConfig {

    @Bean(name = "problemBankJdbcTemplate")
    @ConditionalOnProperty(prefix = "app.problem-bank", name = "enabled", havingValue = "true")
    public NamedParameterJdbcTemplate problemBankJdbcTemplate(ProblemBankDataSourceProperties properties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(normalizeLocalProxyUrl(properties.url()));
        dataSource.setUsername(properties.username());
        dataSource.setPassword(properties.password());
        dataSource.setDriverClassName(properties.driverClassName());
        return new NamedParameterJdbcTemplate(dataSource);
    }

    private String normalizeLocalProxyUrl(String url) {
        if (url == null || url.contains("sslmode=")) {
            return url;
        }
        if (!url.contains("127.0.0.1") && !url.contains("localhost")) {
            return url;
        }
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "sslmode=disable";
    }
}
