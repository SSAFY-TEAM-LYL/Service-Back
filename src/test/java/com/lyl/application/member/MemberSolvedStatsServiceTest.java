package com.lyl.application.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.MemberSolvedProblem;
import com.lyl.domain.member.MemberSolvedProblemRepository;
import com.lyl.domain.problem.ProblemAlgorithm;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemJudgingData;
import com.lyl.domain.problem.ProblemSolvedMetadata;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.presentation.member.dto.MemberSolvedStatsResponse;
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
class MemberSolvedStatsServiceTest {

    @Autowired
    private MemberSolvedStatsService solvedStatsService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberSolvedProblemRepository solvedProblemRepository;

    @Autowired
    private FakeProblemBankProblemRepository problemBankProblemRepository;

    @BeforeEach
    void setUp() {
        problemBankProblemRepository.clear();
    }

    @Test
    void findMySolvedStatsAggregatesAcceptedSolvedProblemsByDifficultyAndAlgorithm() {
        Member member = memberRepository.save(new Member("stats@example.com", "statsUser", "encoded-password"));
        String bronzeProblemId = "11111111-1111-4111-8111-111111111111";
        String goldProblemId = "22222222-2222-4222-8222-222222222222";
        String diamondProblemId = "33333333-3333-4333-8333-333333333333";
        solvedProblemRepository.save(new MemberSolvedProblem(member, bronzeProblemId, 10));
        solvedProblemRepository.save(new MemberSolvedProblem(member, goldProblemId, 65));
        solvedProblemRepository.save(new MemberSolvedProblem(member, diamondProblemId, 130));
        problemBankProblemRepository.addMetadata(new ProblemSolvedMetadata(
                bronzeProblemId,
                "Bronze V",
                List.of("bfs", "two_sum")
        ));
        problemBankProblemRepository.addMetadata(new ProblemSolvedMetadata(
                goldProblemId,
                "Gold I",
                List.of("bfs", "dijkstra")
        ));
        problemBankProblemRepository.addMetadata(new ProblemSolvedMetadata(
                diamondProblemId,
                "Diamond III",
                List.of("max_flow")
        ));

        MemberSolvedStatsResponse response = solvedStatsService.findMySolvedStats(member.getId());

        assertThat(response.totalSolvedCount()).isEqualTo(3);
        assertThat(response.difficulties())
                .filteredOn(difficulty -> difficulty.tier().equals("bronze"))
                .singleElement()
                .satisfies(difficulty -> {
                    assertThat(difficulty.solvedCount()).isEqualTo(1);
                    assertThat(difficulty.levels())
                            .filteredOn(level -> level.level().equals("V"))
                            .singleElement()
                            .extracting(MemberSolvedStatsResponse.DifficultyLevelStatResponse::solvedCount)
                            .isEqualTo(1);
                });
        assertThat(response.difficulties())
                .filteredOn(difficulty -> difficulty.tier().equals("gold"))
                .singleElement()
                .satisfies(difficulty -> {
                    assertThat(difficulty.solvedCount()).isEqualTo(1);
                    assertThat(difficulty.levels())
                            .filteredOn(level -> level.level().equals("I"))
                            .singleElement()
                            .extracting(MemberSolvedStatsResponse.DifficultyLevelStatResponse::solvedCount)
                            .isEqualTo(1);
                });
        assertThat(response.algorithms())
                .filteredOn(algorithm -> algorithm.code().equals("bfs"))
                .singleElement()
                .satisfies(algorithm -> {
                    assertThat(algorithm.solvedCount()).isEqualTo(2);
                    assertThat(algorithm.percent()).isEqualTo(66.7);
                });
        assertThat(response.algorithms()).hasSize(19);
    }

    @Test
    void findMySolvedStatsReturnsZeroFilledCatalogWhenMemberHasNoSolvedProblem() {
        Member member = memberRepository.save(new Member("empty-stats@example.com", "emptyStats", "encoded-password"));

        MemberSolvedStatsResponse response = solvedStatsService.findMySolvedStats(member.getId());

        assertThat(response.totalSolvedCount()).isZero();
        assertThat(response.difficulties()).hasSize(5);
        assertThat(response.difficulties()).allSatisfy(difficulty -> {
            assertThat(difficulty.solvedCount()).isZero();
            assertThat(difficulty.percent()).isZero();
            assertThat(difficulty.levels()).hasSize(5);
        });
        assertThat(response.algorithms()).hasSize(19);
        assertThat(response.algorithms()).allSatisfy(algorithm -> {
            assertThat(algorithm.solvedCount()).isZero();
            assertThat(algorithm.percent()).isZero();
        });
    }

    @TestConfiguration
    static class MemberSolvedStatsServiceTestConfig {

        @Bean
        @Primary
        FakeProblemBankProblemRepository fakeProblemBankProblemRepository() {
            return new FakeProblemBankProblemRepository();
        }
    }

    static class FakeProblemBankProblemRepository implements ProblemBankProblemRepository {

        private final Map<String, ProblemSolvedMetadata> metadataByProblemId = new LinkedHashMap<>();

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
            return Optional.ofNullable(metadataByProblemId.get(problemId))
                    .map(ProblemSolvedMetadata::difficulty);
        }

        @Override
        public List<ProblemSolvedMetadata> findSolvedMetadataByIds(List<String> problemIds) {
            return problemIds.stream()
                    .map(metadataByProblemId::get)
                    .filter(metadata -> metadata != null)
                    .toList();
        }

        @Override
        public List<ProblemAlgorithm> findAlgorithms() {
            return List.of();
        }

        @Override
        public Optional<ProblemJudgingData> findJudgingDataById(String problemId) {
            return Optional.empty();
        }

        void addMetadata(ProblemSolvedMetadata metadata) {
            metadataByProblemId.put(metadata.problemId(), metadata);
        }

        void clear() {
            metadataByProblemId.clear();
        }
    }
}
