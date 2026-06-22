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
import com.lyl.domain.streak.DailyStreakRepository;
import com.lyl.domain.submission.exception.SubmissionNotFoundException;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.domain.submission.SubmissionStatus;
import com.lyl.presentation.submission.dto.SubmissionCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionResponse;
import com.lyl.presentation.submission.dto.SubmissionReviewCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionReviewResponse;
import com.lyl.presentation.submission.dto.SubmissionReviewUpdateRequest;
import com.lyl.presentation.submission.dto.SubmissionUpdateRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDate;
import java.time.ZoneId;
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

    @Autowired
    private DailyStreakRepository dailyStreakRepository;

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
        assertThat(dailyStreakRepository.findAllByMemberId(member.getId()))
                .extracting(streak -> streak.getStreakDate())
                .contains(LocalDate.now(ZoneId.of("Asia/Seoul")));
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
    void refreshInProgressSubmissionsAggregatesWrongAnswerResult() {
        Member member = saveMember("submitter8@example.com", "submitter8");
        publishProblemWithTwoTestCases("88888888-2222-4333-8444-555555555555");
        SubmissionResponse created = submissionService.createSubmission(
                "88888888-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("PYTHON3", "print(0)"),
                member.getId()
        );
        judge0Client.completeAll(SubmissionStatus.WRONG_ANSWER);

        submissionService.refreshInProgressSubmissions(20);

        SubmissionResponse response = submissionService.findSubmission(created.id(), member.getId());
        assertThat(response.status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(response.passedTestCount()).isZero();
        assertThat(response.firstFailedCaseSeq()).isZero();
        assertThat(response.errorMessage()).isEqualTo("WRONG_ANSWER");
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

    @Test
    void updateSubmissionRejudgesAndReturnsSourceCode() {
        Member member = saveMember("submitter5@example.com", "submitter5");
        publishProblemWithTwoTestCases("55555555-2222-4333-8444-555555555555");
        SubmissionResponse created = submissionService.createSubmission(
                "55555555-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("PYTHON3", "print(1)"),
                member.getId()
        );

        SubmissionResponse updated = submissionService.updateSubmission(
                created.id(),
                new SubmissionUpdateRequest("JAVA", "public class Main {}"),
                member.getId()
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.language().name()).isEqualTo("JAVA");
        assertThat(updated.sourceCode()).isEqualTo("public class Main {}");
        assertThat(updated.status()).isEqualTo(SubmissionStatus.JUDGING);
        assertThat(updated.passedTestCount()).isZero();
        assertThat(updated.testCaseResults()).hasSize(2);
    }

    @Test
    void deleteSubmissionHidesSubmission() {
        Member member = saveMember("submitter6@example.com", "submitter6");
        publishProblemWithTwoTestCases("66666666-2222-4333-8444-555555555555");
        SubmissionResponse created = submissionService.createSubmission(
                "66666666-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("CPP", "int main(){return 0;}"),
                member.getId()
        );

        submissionService.deleteSubmission(created.id(), member.getId());

        assertThatThrownBy(() -> submissionService.findSubmission(created.id(), member.getId()))
                .isInstanceOf(SubmissionNotFoundException.class);
    }

    @Test
    void submissionReviewCrudManagesOwnReview() {
        Member member = saveMember("submitter7@example.com", "submitter7");
        publishProblemWithTwoTestCases("77777777-2222-4333-8444-555555555555");
        SubmissionResponse submission = submissionService.createSubmission(
                "77777777-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("PYTHON3", "print(1)"),
                member.getId()
        );

        SubmissionReviewResponse created = submissionService.createSubmissionReview(
                submission.id(),
                new SubmissionReviewCreateRequest("첫 리뷰"),
                member.getId()
        );
        CursorPageResponse<SubmissionReviewResponse> listed = submissionService.findSubmissionReviews(
                submission.id(),
                member.getId(),
                null,
                20
        );
        SubmissionReviewResponse updated = submissionService.updateSubmissionReview(
                created.id(),
                new SubmissionReviewUpdateRequest("수정된 리뷰"),
                member.getId()
        );
        submissionService.deleteSubmissionReview(created.id(), member.getId());
        CursorPageResponse<SubmissionReviewResponse> afterDelete = submissionService.findSubmissionReviews(
                submission.id(),
                member.getId(),
                null,
                20
        );

        assertThat(listed.items()).hasSize(1);
        assertThat(updated.content()).isEqualTo("수정된 리뷰");
        assertThat(afterDelete.items()).isEmpty();
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
