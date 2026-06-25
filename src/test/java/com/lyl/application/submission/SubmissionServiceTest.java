package com.lyl.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.problem.ProblemAlgorithm;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemJudgingData;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemSolvedMetadata;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.ProblemTestCase;
import com.lyl.domain.problem.exception.ProblemNotFoundException;
import com.lyl.domain.streak.DailyStreakRepository;
import com.lyl.domain.submission.exception.SubmissionNotFoundException;
import com.lyl.domain.submission.SubmissionRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Autowired
    private SubmissionRepository submissionRepository;

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

        SubmissionResponse response = submissionService.findSubmission(created.id());
        Member rewardedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(response.status()).isEqualTo(SubmissionStatus.ACCEPTED);
        assertThat(response.passedTestCount()).isEqualTo(2);
        assertThat(response.judgedAt()).isNotNull();
        assertThat(rewardedMember.getXp()).isEqualTo(20);
        assertThat(rewardedMember.getLevel()).isEqualTo(1);
    }

    @Test
    void acceptedSameProblemRewardsXpOnlyOnce() {
        Member member = saveMember("submitter9@example.com", "submitter9");
        String problemId = "99999999-2222-4333-8444-555555555555";
        publishProblemWithTwoTestCases(problemId);
        SubmissionResponse first = submissionService.createSubmission(
                problemId,
                new SubmissionCreateRequest("PYTHON3", "print(3)"),
                member.getId()
        );
        judge0Client.completeAll(SubmissionStatus.ACCEPTED);
        submissionService.refreshInProgressSubmissions(20);

        SubmissionResponse second = submissionService.createSubmission(
                problemId,
                new SubmissionCreateRequest("PYTHON3", "print(3)"),
                member.getId()
        );
        judge0Client.completeAll(SubmissionStatus.ACCEPTED);
        submissionService.refreshInProgressSubmissions(20);

        Member rewardedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(first.status()).isEqualTo(SubmissionStatus.JUDGING);
        assertThat(second.status()).isEqualTo(SubmissionStatus.JUDGING);
        assertThat(rewardedMember.getXp()).isEqualTo(20);
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

        SubmissionResponse response = submissionService.findSubmission(created.id());
        assertThat(response.status()).isEqualTo(SubmissionStatus.WRONG_ANSWER);
        assertThat(response.passedTestCount()).isZero();
        assertThat(response.firstFailedCaseSeq()).isZero();
        assertThat(response.errorMessage()).isEqualTo("WRONG_ANSWER");
        assertThat(response.judgedAt()).isNotNull();
    }

    @Test
    void refreshInProgressSubmissionsMarksOldJudgingSubmissionAsInternalError() {
        Member member = saveMember("submitter12@example.com", "submitter12");
        publishProblemWithTwoTestCases("12121212-2222-4333-8444-555555555555");
        SubmissionResponse created = submissionService.createSubmission(
                "12121212-2222-4333-8444-555555555555",
                new SubmissionCreateRequest("PYTHON3", "print(1)"),
                member.getId()
        );
        com.lyl.domain.submission.Submission submission = submissionRepository.findById(created.id()).orElseThrow();
        ReflectionTestUtils.setField(submission, "submittedAt", LocalDateTime.now().minusSeconds(180));
        submissionRepository.save(submission);

        submissionService.refreshInProgressSubmissions(20);

        SubmissionResponse response = submissionService.findSubmission(created.id());
        assertThat(response.status()).isEqualTo(SubmissionStatus.INTERNAL_ERROR);
        assertThat(response.errorMessage()).isEqualTo("채점 서버 응답 시간이 초과되었습니다.");
        assertThat(response.judgedAt()).isNotNull();
        assertThat(response.testCaseResults())
                .extracting(result -> result.status())
                .containsOnly(SubmissionStatus.INTERNAL_ERROR);
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

        assertThatThrownBy(() -> submissionService.findSubmission(created.id()))
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
                null,
                20
        );

        assertThat(listed.items()).hasSize(1);
        assertThat(updated.content()).isEqualTo("수정된 리뷰");
        assertThat(afterDelete.items()).isEmpty();
    }

    @Test
    void problemSubmissionListIncludesOtherMembersSubmissionsAndAllowsReview() {
        Member submitter = saveMember("submitter10@example.com", "submitter10");
        Member reviewer = saveMember("reviewer10@example.com", "reviewer10");
        String problemId = "10101010-2222-4333-8444-555555555555";
        publishProblemWithTwoTestCases(problemId);
        SubmissionResponse submission = submissionService.createSubmission(
                problemId,
                new SubmissionCreateRequest("PYTHON3", "print(1)"),
                submitter.getId()
        );

        CursorPageResponse<SubmissionResponse> listed = submissionService.findProblemSubmissions(
                problemId,
                null,
                null,
                20
        );
        SubmissionResponse found = submissionService.findSubmission(submission.id());
        SubmissionReviewResponse review = submissionService.createSubmissionReview(
                submission.id(),
                new SubmissionReviewCreateRequest("다른 사람 풀이 리뷰"),
                reviewer.getId()
        );
        SubmissionReviewResponse updatedReview = submissionService.updateSubmissionReview(
                review.id(),
                new SubmissionReviewUpdateRequest("수정한 리뷰"),
                reviewer.getId()
        );

        assertThat(listed.items())
                .extracting(SubmissionResponse::id)
                .contains(submission.id());
        assertThat(found.authorId()).isEqualTo(submitter.getId());
        assertThat(review.authorId()).isEqualTo(reviewer.getId());
        assertThat(updatedReview.content()).isEqualTo("수정한 리뷰");
    }

    @Test
    void problemSubmissionListCanBeFilteredByCurrentMember() {
        Member submitter = saveMember("submitter11@example.com", "submitter11");
        Member other = saveMember("other11@example.com", "other11");
        String problemId = "11111111-3333-4333-8444-555555555555";
        publishProblemWithTwoTestCases(problemId);
        SubmissionResponse mine = submissionService.createSubmission(
                problemId,
                new SubmissionCreateRequest("PYTHON3", "print(1)"),
                submitter.getId()
        );
        SubmissionResponse others = submissionService.createSubmission(
                problemId,
                new SubmissionCreateRequest("JAVA", "public class Main {}"),
                other.getId()
        );

        CursorPageResponse<SubmissionResponse> listed = submissionService.findProblemSubmissions(
                problemId,
                submitter.getId(),
                null,
                20
        );

        assertThat(listed.items())
                .extracting(SubmissionResponse::id)
                .contains(mine.id())
                .doesNotContain(others.id());
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
        problemBankProblemRepository.addDifficulty(problemId, "Bronze III");
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
        private final Map<String, String> difficulties = new LinkedHashMap<>();

        @Override
        public List<ProblemSummary> findPublishedSummaries(int offset, int size) {
            return List.of();
        }

        @Override
        public List<ProblemSummary> findSummariesByIds(List<String> problemIds) {
            return List.of();
        }

        @Override
        public List<ProblemSummary> findSummariesByIds(
                List<String> problemIds,
                String difficultyTier,
                String algorithm,
                String query,
                int offset,
                int size
        ) {
            return List.of();
        }

        @Override
        public Optional<ProblemDetail> findDetailById(String problemId) {
            return Optional.empty();
        }

        @Override
        public Optional<String> findDifficultyById(String problemId) {
            return Optional.ofNullable(difficulties.get(problemId));
        }

        @Override
        public List<ProblemSolvedMetadata> findSolvedMetadataByIds(List<String> problemIds) {
            return problemIds.stream()
                    .filter(difficulties::containsKey)
                    .map(problemId -> new ProblemSolvedMetadata(problemId, difficulties.get(problemId), List.of()))
                    .toList();
        }

        @Override
        public List<ProblemAlgorithm> findAlgorithms() {
            return List.of();
        }

        @Override
        public Optional<ProblemJudgingData> findJudgingDataById(String problemId) {
            return Optional.ofNullable(judgingData.get(problemId));
        }

        void addDifficulty(String problemId, String difficulty) {
            difficulties.put(problemId, difficulty);
        }

        void addJudgingData(ProblemJudgingData data) {
            judgingData.put(data.id(), data);
        }

        void clear() {
            judgingData.clear();
            difficulties.clear();
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
