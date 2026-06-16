package com.lyl.infrastructure.judge0;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.judge0", name = "enabled", havingValue = "true")
public class Judge0ConnectionVerifier implements ApplicationRunner {

    private final Judge0Properties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.baseUrl() == null || properties.baseUrl().isBlank()) {
            log.warn("Judge0 connection check skipped. baseUrl is empty.");
            return;
        }

        try {
            Judge0AboutResponse about = RestClient.builder()
                    .requestFactory(requestFactory(properties.requestTimeoutSeconds()))
                    .build()
                    .get()
                    .uri(baseUrl() + "/about")
                    .header(properties.authHeader(), properties.authToken())
                    .retrieve()
                    .body(Judge0AboutResponse.class);
            log.info("Judge0 connection verified. baseUrl={}, authHeader={}, tokenConfigured={}, version={}",
                    baseUrl(),
                    properties.authHeader(),
                    properties.authToken() != null && !properties.authToken().isBlank(),
                    about == null || about.version() == null ? "unknown" : about.version());
        } catch (RestClientResponseException e) {
            log.warn("Judge0 connection check failed. status={}, baseUrl={}, authHeader={}, tokenConfigured={}, responseBody={}",
                    e.getStatusCode(),
                    baseUrl(),
                    properties.authHeader(),
                    properties.authToken() != null && !properties.authToken().isBlank(),
                    abbreviate(e.getResponseBodyAsString()),
                    e);
        } catch (RestClientException e) {
            log.warn("Judge0 connection check failed. baseUrl={}, authHeader={}, tokenConfigured={}, message={}",
                    baseUrl(),
                    properties.authHeader(),
                    properties.authToken() != null && !properties.authToken().isBlank(),
                    e.getMessage(),
                    e);
        }
    }

    private String baseUrl() {
        return properties.baseUrl().replaceAll("/+$", "");
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.length() <= 500 ? value : value.substring(0, 500) + "...";
    }

    private record Judge0AboutResponse(String version) {
    }
}
