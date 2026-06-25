package com.lyl.application.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemAlgorithm;
import com.lyl.domain.problem.ProblemConstraint;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemJudgingData;
import com.lyl.domain.problem.ProblemPublication;
import com.lyl.domain.problem.ProblemPublicationRepository;
import com.lyl.domain.problem.ProblemSample;
import com.lyl.domain.problem.ProblemSolvedMetadata;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.exception.ProblemNotFoundException;
import com.lyl.domain.submission.Submission;
import com.lyl.domain.submission.SubmissionLanguage;
import com.lyl.domain.submission.SubmissionRepository;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemAlgorithmResponse;
import com.lyl.presentation.problem.dto.ProblemDetailResponse;
import com.lyl.presentation.problem.dto.ProblemServiceSummaryResponse;
import com.lyl.presentation.problem.dto.ProblemSummaryResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class ProblemQueryServiceTest {

    @Autowired
    private ProblemQueryService problemQueryService;

    @Autowired
    private ProblemPublicationRepository problemPublicationRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FakeProblemBankProblemRepository problemBankProblemRepository;

    @BeforeEach
    void setUp() {
        problemBankProblemRepository.clear();
    }

    @Test
    void findProblemsReturnsPublishedProblemSummariesFromProblemBank() {
        String firstProblemId = "77777777-7777-4777-8777-777777777777";
        String secondProblemId = "88888888-8888-4888-8888-888888888888";
        problemPublicationRepository.save(new ProblemPublication(firstProblemId));
        problemPublicationRepository.save(new ProblemPublication(secondProblemId));
        problemBankProblemRepository.addSummary(new ProblemSummary(
                firstProblemId,
                1001L,
                "첫 번째 문제",
                "Silver III",
                1000,
                OffsetDateTime.parse("2026-06-15T01:00:00Z")
        ));
        problemBankProblemRepository.addSummary(new ProblemSummary(
                secondProblemId,
                1002L,
                "두 번째 문제",
                "Gold IV",
                2000,
                OffsetDateTime.parse("2026-06-15T02:00:00Z")
        ));

        CursorPageResponse<ProblemSummaryResponse> response = problemQueryService.findProblems(null, 20, null, null, null);

        assertThat(response.items())
                .extracting(ProblemSummaryResponse::id)
                .contains(firstProblemId, secondProblemId);
        assertThat(response.items())
                .extracting(ProblemSummaryResponse::title)
                .contains("첫 번째 문제", "두 번째 문제");
    }

    @Test
    void findProblemsSearchesByPartialTitleOrProblemNumber() {
        String firstProblemId = "10101010-1010-4010-8010-101010101010";
        String secondProblemId = "20202020-2020-4020-8020-202020202020";
        problemPublicationRepository.save(new ProblemPublication(firstProblemId));
        problemPublicationRepository.save(new ProblemPublication(secondProblemId));
        problemBankProblemRepository.addSummary(new ProblemSummary(
                firstProblemId,
                2101L,
                "두 수의 합",
                "Bronze V",
                1000,
                OffsetDateTime.parse("2026-06-15T01:00:00Z")
        ));
        problemBankProblemRepository.addSummary(new ProblemSummary(
                secondProblemId,
                3102L,
                "최단 경로",
                "Gold IV",
                2000,
                OffsetDateTime.parse("2026-06-15T02:00:00Z")
        ));

        CursorPageResponse<ProblemSummaryResponse> titleResponse =
                problemQueryService.findProblems(null, 20, null, null, "수의");
        CursorPageResponse<ProblemSummaryResponse> numberResponse =
                problemQueryService.findProblems(null, 20, null, null, "102");

        assertThat(titleResponse.items())
                .extracting(ProblemSummaryResponse::id)
                .containsExactly(firstProblemId);
        assertThat(numberResponse.items())
                .extracting(ProblemSummaryResponse::id)
                .containsExactly(secondProblemId);
    }

    @Test
    void findSummaryReturnsPublishedProblemCountAndTodaySubmissionCount() {
        String problemId = "30303030-3030-4030-8030-303030303030";
        Member member = memberRepository.save(new Member("summary@example.com", "summaryUser", "encoded-password"));
        problemPublicationRepository.save(new ProblemPublication(problemId));
        submissionRepository.save(new Submission(member, problemId, SubmissionLanguage.PYTHON3, "print(1)", 1));

        ProblemServiceSummaryResponse response = problemQueryService.findSummary();

        assertThat(response.publishedProblemCount()).isGreaterThanOrEqualTo(1);
        assertThat(response.todaySubmissionCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findAlgorithmsReturnsSupportedAlgorithmTypesFromEnum() {
        List<ProblemAlgorithmResponse> response = problemQueryService.findAlgorithms();

        assertThat(response)
                .extracting(ProblemAlgorithmResponse::code)
                .containsExactly(
                        "arithmetic",
                        "basic_io",
                        "conditional",
                        "loop_accumulate",
                        "dijkstra",
                        "lis",
                        "segtree",
                        "two_sum",
                        "bfs",
                        "binary_search",
                        "union_find",
                        "toposort",
                        "knapsack",
                        "sort",
                        "string_match",
                        "max_flow",
                        "sieve",
                        "bellman_ford",
                        "floyd_warshall",
                        "kruskal_mst",
                        "heap",
                        "fenwick",
                        "coin_change"
                );
    }

    @Test
    void findProblemReturnsDetailWhenProblemIsPublished() {
        String problemId = "99999999-9999-4999-8999-999999999999";
        problemPublicationRepository.save(new ProblemPublication(problemId));
        problemBankProblemRepository.addDetail(new ProblemDetail(
                problemId,
                1003L,
                "상세 문제",
                "문제 설명",
                "입력 형식",
                "출력 형식",
                "Gold V",
                List.of(new ProblemConstraint("N", 1L, 100L, "N의 범위")),
                List.of(new ProblemSample("1 2\n", "3\n", "기본 예시")),
                1000
        ));

        ProblemDetailResponse response = problemQueryService.findProblem(problemId);

        assertThat(response.id()).isEqualTo(problemId);
        assertThat(response.problemNumber()).isEqualTo(1003L);
        assertThat(response.title()).isEqualTo("상세 문제");
        assertThat(response.difficulty()).isEqualTo("Gold V");
        assertThat(response.constraints()).hasSize(1);
        assertThat(response.samples()).hasSize(1);
    }

    @Test
    void findProblemThrowsNotFoundWhenProblemIsNotPublished() {
        String problemId = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
        problemBankProblemRepository.addDetail(new ProblemDetail(
                problemId,
                1004L,
                "미공개 문제",
                "문제 설명",
                "입력 형식",
                "출력 형식",
                "Bronze V",
                List.of(),
                List.of(),
                1000
        ));

        assertThatThrownBy(() -> problemQueryService.findProblem(problemId))
                .isInstanceOf(ProblemNotFoundException.class);
    }

    @TestConfiguration
    static class ProblemQueryServiceTestConfig {

        @Bean
        @Primary
        FakeProblemBankProblemRepository fakeProblemBankProblemRepository() {
            return new FakeProblemBankProblemRepository();
        }
    }

    static class FakeProblemBankProblemRepository implements ProblemBankProblemRepository {

        private final Map<String, ProblemSummary> summaries = new LinkedHashMap<>();
        private final Map<String, ProblemDetail> details = new LinkedHashMap<>();

        @Override
        public List<ProblemSummary> findPublishedSummaries(int offset, int size) {
            return summaries.values().stream()
                    .skip(offset)
                    .limit(size)
                    .toList();
        }

        @Override
        public List<ProblemSummary> findSummariesByIds(List<String> problemIds) {
            List<ProblemSummary> result = new ArrayList<>();
            for (String problemId : problemIds) {
                ProblemSummary summary = summaries.get(problemId);
                if (summary != null) {
                    result.add(summary);
                }
            }
            return result;
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
            return findSummariesByIds(problemIds).stream()
                    .filter(summary -> difficultyTier == null || summary.difficulty().toLowerCase().startsWith(difficultyTier.toLowerCase()))
                    .filter(summary -> query == null
                            || summary.title().toLowerCase().contains(query.toLowerCase())
                            || String.valueOf(summary.problemNumber()).contains(query))
                    .skip(offset)
                    .limit(size)
                    .toList();
        }

        @Override
        public Optional<ProblemDetail> findDetailById(String problemId) {
            return Optional.ofNullable(details.get(problemId));
        }

        @Override
        public Optional<String> findDifficultyById(String problemId) {
            return Optional.ofNullable(details.get(problemId))
                    .map(ProblemDetail::difficulty);
        }

        @Override
        public List<ProblemSolvedMetadata> findSolvedMetadataByIds(List<String> problemIds) {
            return findSummariesByIds(problemIds).stream()
                    .map(summary -> new ProblemSolvedMetadata(summary.id(), summary.difficulty(), List.of()))
                    .toList();
        }

        @Override
        public List<ProblemAlgorithm> findAlgorithms() {
            return List.of();
        }

        @Override
        public Optional<ProblemJudgingData> findJudgingDataById(String problemId) {
            ProblemDetail detail = details.get(problemId);
            if (detail == null) {
                return Optional.empty();
            }
            return Optional.of(new ProblemJudgingData(problemId, detail.timeLimitMs(), List.of()));
        }

        void addSummary(ProblemSummary summary) {
            summaries.put(summary.id(), summary);
        }

        void addDetail(ProblemDetail detail) {
            details.put(detail.id(), detail);
            summaries.putIfAbsent(detail.id(), new ProblemSummary(
                    detail.id(),
                    detail.problemNumber(),
                    detail.title(),
                    detail.difficulty(),
                    detail.timeLimitMs(),
                    OffsetDateTime.parse("2026-06-15T00:00:00Z")
            ));
        }

        void clear() {
            summaries.clear();
            details.clear();
        }
    }
}
