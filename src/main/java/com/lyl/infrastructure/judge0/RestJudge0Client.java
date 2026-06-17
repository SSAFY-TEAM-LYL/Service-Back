package com.lyl.infrastructure.judge0;

import com.lyl.application.submission.Judge0Client;
import com.lyl.application.submission.Judge0SubmissionRequest;
import com.lyl.application.submission.Judge0SubmissionResult;
import com.lyl.application.submission.Judge0SubmissionToken;
import com.lyl.domain.submission.SubmissionStatus;
import com.lyl.domain.submission.exception.JudgeServerUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class RestJudge0Client implements Judge0Client {

    private final Judge0Properties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public RestJudge0Client(Judge0Properties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(properties.requestTimeoutSeconds()))
                .build();
    }

    @Override
    public List<Judge0SubmissionToken> submitBatch(List<Judge0SubmissionRequest> requests) {
        ensureEnabled();
        return requests.stream()
                .map(request -> new Judge0SubmissionToken(request.caseSeq(), submitSingle(request).token()))
                .toList();
    }

    private Judge0SubmitResponse submitSingle(Judge0SubmissionRequest request) {
        try {
            Judge0SubmitResponse response = restClient.post()
                    .uri(baseUrl() + "/submissions?base64_encoded=true")
                    .header(properties.authHeader(), properties.authToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJson(toSubmitPayload(request)))
                    .retrieve()
                    .body(Judge0SubmitResponse.class);
            if (response == null || response.token() == null || response.token().isBlank()) {
                log.warn("Judge0 submission returned empty token. caseSeq={}, baseUrl={}", request.caseSeq(), baseUrl());
                throw new JudgeServerUnavailableException();
            }
            return response;
        } catch (RestClientResponseException e) {
            log.warn("Judge0 submission failed. status={}, caseSeq={}, baseUrl={}, responseBody={}",
                    e.getStatusCode(), request.caseSeq(), baseUrl(), abbreviate(e.getResponseBodyAsString()));
            throw new JudgeServerUnavailableException();
        } catch (RestClientException e) {
            log.warn("Judge0 submission connection failed. caseSeq={}, baseUrl={}, message={}",
                    request.caseSeq(), baseUrl(), e.getMessage(), e);
            throw new JudgeServerUnavailableException();
        }
    }

    @Override
    public List<Judge0SubmissionResult> fetchBatchResults(List<String> tokens) {
        ensureEnabled();
        if (tokens.isEmpty()) {
            return List.of();
        }
        try {
            String joinedTokens = tokens.stream().collect(Collectors.joining(","));
            Judge0BatchResultResponse response = restClient.get()
                    .uri(baseUrl()
                            + "/submissions/batch?base64_encoded=true&tokens="
                            + joinedTokens
                            + "&fields=token,status,time,memory,stderr,compile_output,message")
                    .header(properties.authHeader(), properties.authToken())
                    .retrieve()
                    .body(Judge0BatchResultResponse.class);
            return response == null || response.submissions() == null
                    ? List.of()
                    : response.submissions().stream()
                            .filter(Objects::nonNull)
                            .map(this::toResult)
                            .toList();
        } catch (RestClientResponseException e) {
            log.warn("Judge0 batch result fetch failed. status={}, baseUrl={}, responseBody={}",
                    e.getStatusCode(), baseUrl(), abbreviate(e.getResponseBodyAsString()));
            throw new JudgeServerUnavailableException();
        } catch (RestClientException e) {
            log.warn("Judge0 batch result fetch connection failed. baseUrl={}, tokenCount={}, message={}",
                    baseUrl(), tokens.size(), e.getMessage(), e);
            throw new JudgeServerUnavailableException();
        }
    }

    private Map<String, Object> toSubmitPayload(Judge0SubmissionRequest request) {
        int timeLimitSeconds = Math.max(1, (int) Math.ceil(request.timeLimitMs() / 1000.0));
        return Map.of(
                "source_code", encode(request.sourceCode()),
                "language_id", request.language().judge0LanguageId(),
                "stdin", encode(request.testCase().input()),
                "expected_output", encode(request.testCase().expected()),
                "cpu_time_limit", timeLimitSeconds,
                "wall_time_limit", timeLimitSeconds + 1
        );
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize Judge0 submission payload.", e);
            throw new JudgeServerUnavailableException();
        }
    }

    private Judge0SubmissionResult toResult(Judge0ResultResponse response) {
        int statusId = response.status() == null ? 13 : response.status().id();
        boolean completed = statusId != 1 && statusId != 2;
        return new Judge0SubmissionResult(
                response.token(),
                toStatus(statusId),
                completed,
                parseTimeMs(response.time()),
                response.memory(),
                decode(response.stderr()),
                decode(response.compileOutput()),
                decode(response.message())
        );
    }

    private SubmissionStatus toStatus(int judge0StatusId) {
        return switch (judge0StatusId) {
            case 1, 2 -> SubmissionStatus.JUDGING;
            case 3 -> SubmissionStatus.ACCEPTED;
            case 4 -> SubmissionStatus.WRONG_ANSWER;
            case 5 -> SubmissionStatus.TIME_LIMIT_EXCEEDED;
            case 6 -> SubmissionStatus.COMPILE_ERROR;
            case 7, 8, 9, 10, 11, 12 -> SubmissionStatus.RUNTIME_ERROR;
            default -> SubmissionStatus.INTERNAL_ERROR;
        };
    }

    private Integer parseTimeMs(String seconds) {
        if (seconds == null || seconds.isBlank()) {
            return null;
        }
        return (int) Math.round(Double.parseDouble(seconds) * 1000);
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private void ensureEnabled() {
        if (!properties.enabled() || properties.baseUrl() == null || properties.baseUrl().isBlank()) {
            log.warn("Judge0 client is disabled or base URL is empty. enabled={}, baseUrlConfigured={}",
                    properties.enabled(), properties.baseUrl() != null && !properties.baseUrl().isBlank());
            throw new JudgeServerUnavailableException();
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

    private record Judge0SubmitResponse(String token) {
    }

    private record Judge0BatchResultResponse(List<Judge0ResultResponse> submissions) {
    }

    private record Judge0ResultResponse(
            String token,
            Judge0Status status,
            String time,
            Integer memory,
            String stderr,
            String compileOutput,
            String message
    ) {
        @com.fasterxml.jackson.annotation.JsonCreator
        Judge0ResultResponse(
                @com.fasterxml.jackson.annotation.JsonProperty("token") String token,
                @com.fasterxml.jackson.annotation.JsonProperty("status") Judge0Status status,
                @com.fasterxml.jackson.annotation.JsonProperty("time") String time,
                @com.fasterxml.jackson.annotation.JsonProperty("memory") Integer memory,
                @com.fasterxml.jackson.annotation.JsonProperty("stderr") String stderr,
                @com.fasterxml.jackson.annotation.JsonProperty("compile_output") String compileOutput,
                @com.fasterxml.jackson.annotation.JsonProperty("message") String message
        ) {
            this.token = token;
            this.status = status;
            this.time = time;
            this.memory = memory;
            this.stderr = stderr;
            this.compileOutput = compileOutput;
            this.message = message;
        }
    }

    private record Judge0Status(int id, String description) {
    }
}
