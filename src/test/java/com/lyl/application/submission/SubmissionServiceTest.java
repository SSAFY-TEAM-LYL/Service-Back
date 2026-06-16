package com.lyl.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemJudgingData;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.ProblemTestCase;
import com.lyl.domain.problem.exception.ProblemNotFoundException;
import com.lyl.domain.submission.SubmissionStatus;
import com.lyl.presentation.submission.dto.SubmissionCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class SubmissionServiceTest {

    private static final String PROBLEM_ID = "11111111-2222-4333-8444-555555555555";

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProblemPublicationRepository problemPublicationRepository;

    @Autowired
    private FakeProblemBankProblemRepository problemBankProblemRepository;

    @Autowired
    private FakeJudge0Client judge0Client;

    @BeforeEach
    void setUp() {
        problemBankProblemRepository.clear();
        judge0Client.clear();
    }

    @Test
    void createSubmissionCreatesJudgingSubmissionWithJudgeTokens() {
        Member member = saveMember("submitter1@example.com", "submitter1");
        publishProblemWithTwoTestCases(PROBLEM_ID);

        SubmissionResponse response = submissionService.createSubmission(
                PROBLEM_ID,
                new SubmissionCreateRequest("PYTHON3", "print(input())"),
                member.getId()
        );

        assertThat(response.problemId()).isEqualTo(PROBLEM_ID);
        assertThat(response.language().name()).isEqualTo("PYTHON3");
        assertThat(response.status()).isEqualTo(SubmissionStatus.JUDGING);
        assertThat(response.totalTestCount()).isEqualTo(2);
        assertThat(response.testCaseResults())
                .extracting(result -> result.status())
                .containsOnly(SubmissionStatus.JUDGING);
    }

    @Test
    void refreshInProgressSubmissionsAggregatesAcceptedResult() {
        Member member = saveMember("submitter2@example.com", "submitter2");
        publishProblemWithTwoTestCases("22222222-2222-4333-8444-555555555555");
        SubmissionResponse created = submissionService.createSubmission(
                "22222222-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("CPP", "#include <iostream>\nint main(){return 0;}"),
                member.getId()
        );
        judge0Client.completeAll(SubmissionStatus.ACCEPTED);

        submissionService.refreshInProgressSubmissions(20);

        SubmissionResponse response = submissionService.findSubmission(created.id(), member.getId());
        assertThat(response.status()).isEqualTo(SubmissionStatus.ACCEPTED);
        assertThat(response.passedTestCount()).isEqualTo(2);
        assertThat(response.judgedAt()).isNotNull();
    }

    @Test
    void createSubmissionThrowsNotFoundWhenProblemIsNotPublished() {
        Member member = saveMember("submitter3@example.com", "submitter3");
        problemBankProblemRepository.addJudgingData(new ProblemJudgingData(
                "33333333-2222-4333-8444-555555555555",
                1000,
                List.of(new ProblemTestCase(0, "1\n", "1\n", "small"))
        ));

        assertThatThrownBy(() -> submissionService.createSubmission(
                "33333333-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("JAVA", "class Main {}"),
                member.getId()
        )).isInstanceOf(ProblemNotFoundException.class);
    }

    @Test
    void createSubmissionRejectsUnsupportedLanguage() {
        Member member = saveMember("submitter4@example.com", "submitter4");
        publishProblemWithTwoTestCases("44444444-2222-4333-8444-555555555555");

        assertThatThrownBy(() -> submissionService.createSubmission(
                "44444444-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("RUBY", "puts 1"),
                member.getId()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 언어입니다.");
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(new Member(email, nickname, "encoded-password"));
    }

    private void publishProblemWithTwoTestCases(String problemId) {
        problemPublicationRepository.save(new ProblemPublication(problemId));
        problemBankProblemRepository.addJudgingData(new ProblemJudgingData(
                problemId,
                1000,
                List.of(
                        new ProblemTestCase(0, "1 2\n", "3\n", "small"),
                        new ProblemTestCase(1, "2 3\n", "5\n", "medium")
                )
        ));
    }

    @TestConfiguration
    static class SubmissionServiceTestConfig {

        @Bean
        @Primary
        FakeProblemBankProblemRepository fakeProblemBankProblemRepository() {
            return new FakeProblemBankProblemRepository();
        }

        @Bean
        @Primary
        FakeJudge0Client fakeJudge0Client() {
            return new FakeJudge0Client();
        }
    }

    static class FakeProblemBankProblemRepository implements ProblemBankProblemRepository {

        private final Map<String, ProblemJudgingData> judgingData = new LinkedHashMap<>();

        @Override
        public List<ProblemSummary> findPublishedSummaries(int offset, int size) {
            return List.of();
        }

        @Override
        public List<ProblemSummary> findSummariesByIds(List<String> problemIds) {
            return List.of();
        }

        @Override
        public Optional<ProblemDetail> findDetailById(String problemId) {
            return Optional.empty();
        }

        @Override
        public Optional<ProblemJudgingData> findJudgingDataById(String problemId) {
            return Optional.ofNullable(judgingData.get(problemId));
        }

        void addJudgingData(ProblemJudgingData data) {
            judgingData.put(data.id(), data);
        }

        void clear() {
            judgingData.clear();
        }
    }

    static class FakeJudge0Client implements Judge0Client {

        private final AtomicInteger sequence = new AtomicInteger();
        private final List<String> tokens = new ArrayList<>();
        private SubmissionStatus completedStatus;

        @Override
        public List<Judge0SubmissionToken> submitBatch(List<Judge0SubmissionRequest> requests) {
            return requests.stream()
                    .map(request -> {
                        String token = "token-" + sequence.incrementAndGet();
                        tokens.add(token);
                        return new Judge0SubmissionToken(request.caseSeq(), token);
                    })
                    .toList();
        }

        @Override
        public List<Judge0SubmissionResult> fetchBatchResults(List<String> tokens) {
            if (completedStatus == null) {
                return tokens.stream()
                        .map(token -> new Judge0SubmissionResult(
                                token,
                                SubmissionStatus.JUDGING,
                                false,
                                null,
                                null,
                                null,
                                null,
                                null
                        ))
                        .toList();
            }
            return tokens.stream()
                    .map(token -> new Judge0SubmissionResult(
                            token,
                            completedStatus,
                            true,
                            10,
                            1024,
                            null,
                            null,
                            completedStatus.name()
                    ))
                    .toList();
        }

        void completeAll(SubmissionStatus status) {
            this.completedStatus = status;
        }

        void clear() {
            tokens.clear();
            completedStatus = null;
        }
    }
}
