package com.lyl.infrastructure.judge0;

import static org.assertj.core.api.Assertions.assertThat;

import com.lyl.application.submission.Judge0SubmissionResult;
import com.lyl.domain.submission.SubmissionStatus;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
}
