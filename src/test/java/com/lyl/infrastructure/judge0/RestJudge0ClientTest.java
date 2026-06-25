package com.lyl.infrastructure.judge0;

import static org.assertj.core.api.Assertions.assertThat;

import com.lyl.application.submission.Judge0SubmissionResult;
import com.lyl.domain.submission.SubmissionStatus;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RestJudge0ClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void fetchBatchResultsKeepsPlainTextMessageWhenJudge0DoesNotBase64EncodeIt() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/submissions/batch", exchange -> {
            String response = """
                    {
                      "submissions": [
                        {
                          "token": "token-1",
                          "status": { "id": 4, "description": "Wrong Answer" },
                          "time": "0.01",
                          "memory": 512,
                          "stderr": null,
                          "compile_output": null,
                          "message": "Wrong Answer"
                        }
                      ]
                    }
                    """;
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        RestJudge0Client client = new RestJudge0Client(new Judge0Properties(
                "http://127.0.0.1:" + server.getAddress().getPort(),
                "X-Auth-Token",
                "test-token",
                5,
                120,
                20,
                true
        ));

        List<Judge0SubmissionResult> results = client.fetchBatchResults(List.of("token-1"));

        assertThat(results).hasSize(1);
        Judge0SubmissionResult result = results.getFirst();
        assertThat(result.token()).isEqualTo("token-1");
        assertThat(result.status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(result.completed()).isTrue();
        assertThat(result.timeMs()).isEqualTo(10);
        assertThat(result.memoryKb()).isEqualTo(512);
        assertThat(result.message()).isEqualTo("Wrong Answer");
    }

    @Test
    void fetchBatchResultsSplitsTokensByJudge0BatchLimit() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        AtomicInteger callCount = new AtomicInteger();
        List<Integer> requestedSizes = new ArrayList<>();
        server.createContext("/submissions/batch", exchange -> {
            callCount.incrementAndGet();
            String tokens = extractTokens(exchange.getRequestURI().getRawQuery());
            List<String> requestedTokens = tokens.isBlank() ? List.of() : List.of(tokens.split(","));
            requestedSizes.add(requestedTokens.size());
            if (requestedTokens.size() > 20) {
                byte[] error = "{\"error\":\"too many\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, error.length);
                exchange.getResponseBody().write(error);
                exchange.close();
                return;
            }
            String submissions = requestedTokens.stream()
                    .map(token -> """
                            {
                              "token": "%s",
                              "status": { "id": 3, "description": "Accepted" },
                              "time": "0.01",
                              "memory": 512,
                              "stderr": null,
                              "compile_output": null,
                              "message": null
                            }
                            """.formatted(token))
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
            byte[] body = ("{\"submissions\":[" + submissions + "]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        RestJudge0Client client = new RestJudge0Client(new Judge0Properties(
                "http://127.0.0.1:" + server.getAddress().getPort(),
                "X-Auth-Token",
                "test-token",
                5,
                120,
                50,
                true
        ));
        List<String> tokens = IntStream.rangeClosed(1, 21)
                .mapToObj(index -> "token-" + index)
                .toList();

        List<Judge0SubmissionResult> results = client.fetchBatchResults(tokens);

        assertThat(results).hasSize(21);
        assertThat(callCount).hasValue(2);
        assertThat(requestedSizes).containsExactly(20, 1);
    }

    private String extractTokens(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        for (String parameter : query.split("&")) {
            if (parameter.startsWith("tokens=")) {
                return parameter.substring("tokens=".length());
            }
        }
        return "";
    }
}
