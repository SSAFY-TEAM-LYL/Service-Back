package com.lyl.application.submission;

import com.lyl.infrastructure.judge0.Judge0Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.judge0", name = "enabled", havingValue = "true")
public class SubmissionPollingService {

    private final SubmissionService submissionService;
    private final Judge0Properties judge0Properties;

    @Scheduled(fixedDelayString = "${app.judge0.poll-delay-millis:2000}")
    public void pollInProgressSubmissions() {
        try {
            submissionService.refreshInProgressSubmissions(judge0Properties.pollBatchSize());
        } catch (RuntimeException e) {
            log.warn("Failed to poll Judge0 submissions", e);
        }
    }
}
