package com.lyl.application.member;

import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.MemberSolvedProblemRepository;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.domain.problem.ProblemAlgorithm;
import com.lyl.domain.problem.ProblemAlgorithmCatalog;
import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDifficulty;
import com.lyl.domain.problem.ProblemSolvedMetadata;
import com.lyl.presentation.member.dto.MemberSolvedStatsResponse;
import com.lyl.presentation.member.dto.MemberSolvedStatsResponse.AlgorithmStatResponse;
import com.lyl.presentation.member.dto.MemberSolvedStatsResponse.DifficultyLevelStatResponse;
import com.lyl.presentation.member.dto.MemberSolvedStatsResponse.DifficultyStatResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSolvedStatsService {

    private static final List<DifficultyTier> DIFFICULTY_TIERS = List.of(
            new DifficultyTier("bronze", "Bronze"),
            new DifficultyTier("silver", "Silver"),
            new DifficultyTier("gold", "Gold"),
            new DifficultyTier("platinum", "Platinum"),
            new DifficultyTier("diamond", "Diamond")
    );

    private static final List<String> DIFFICULTY_LEVELS = List.of("V", "IV", "III", "II", "I");

    private final MemberRepository memberRepository;
    private final MemberSolvedProblemRepository solvedProblemRepository;
    private final ProblemBankProblemRepository problemBankProblemRepository;

    @Transactional(readOnly = true)
    public MemberSolvedStatsResponse findMySolvedStats(Long memberId) {
        if (memberRepository.findById(memberId).isEmpty()) {
            throw new MemberNotFoundException();
        }

        List<String> solvedProblemIds = solvedProblemRepository.findProblemIdsByMemberId(memberId);
        List<ProblemSolvedMetadata> metadata = solvedProblemIds.isEmpty()
                ? List.of()
                : problemBankProblemRepository.findSolvedMetadataByIds(solvedProblemIds);
        int totalSolvedCount = metadata.size();

        Map<String, Integer> tierCounts = initializeDifficultyTierCounts();
        Map<String, Map<String, Integer>> levelCounts = initializeDifficultyLevelCounts();
        Map<String, AlgorithmCounter> algorithmCounters = initializeAlgorithmCounters();

        for (ProblemSolvedMetadata item : metadata) {
            ProblemDifficulty.parse(item.difficulty()).ifPresent(difficulty -> {
                String tier = difficulty.tier().toLowerCase(Locale.ROOT);
                tierCounts.computeIfPresent(tier, (key, count) -> count + 1);
                levelCounts.getOrDefault(tier, Map.of())
                        .computeIfPresent(difficulty.roman(), (key, count) -> count + 1);
            });

            for (String algorithm : item.algorithms().stream().distinct().toList()) {
                AlgorithmCounter counter = algorithmCounters.get(algorithm);
                if (counter != null) {
                    counter.increase();
                }
            }
        }

        return new MemberSolvedStatsResponse(
                totalSolvedCount,
                toDifficultyResponses(totalSolvedCount, tierCounts, levelCounts),
                toAlgorithmResponses(totalSolvedCount, algorithmCounters)
        );
    }

    private Map<String, Integer> initializeDifficultyTierCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (DifficultyTier tier : DIFFICULTY_TIERS) {
            counts.put(tier.code(), 0);
        }
        return counts;
    }

    private Map<String, Map<String, Integer>> initializeDifficultyLevelCounts() {
        Map<String, Map<String, Integer>> counts = new LinkedHashMap<>();
        for (DifficultyTier tier : DIFFICULTY_TIERS) {
            Map<String, Integer> tierLevelCounts = new LinkedHashMap<>();
            for (String level : DIFFICULTY_LEVELS) {
                tierLevelCounts.put(level, 0);
            }
            counts.put(tier.code(), tierLevelCounts);
        }
        return counts;
    }

    private Map<String, AlgorithmCounter> initializeAlgorithmCounters() {
        Map<String, AlgorithmCounter> counters = new LinkedHashMap<>();
        for (ProblemAlgorithm algorithm : ProblemAlgorithmCatalog.supported()) {
            counters.put(algorithm.code(), new AlgorithmCounter(algorithm.code(), algorithm.label()));
        }
        return counters;
    }

    private List<DifficultyStatResponse> toDifficultyResponses(
            int totalSolvedCount,
            Map<String, Integer> tierCounts,
            Map<String, Map<String, Integer>> levelCounts
    ) {
        return DIFFICULTY_TIERS.stream()
                .map(tier -> new DifficultyStatResponse(
                        tier.code(),
                        tier.label(),
                        tierCounts.get(tier.code()),
                        percentage(tierCounts.get(tier.code()), totalSolvedCount),
                        toDifficultyLevelResponses(tier, levelCounts.get(tier.code()))
                ))
                .toList();
    }

    private List<DifficultyLevelStatResponse> toDifficultyLevelResponses(
            DifficultyTier tier,
            Map<String, Integer> levelCounts
    ) {
        return DIFFICULTY_LEVELS.stream()
                .map(level -> new DifficultyLevelStatResponse(
                        level,
                        tier.label() + " " + level,
                        levelCounts.get(level)
                ))
                .toList();
    }

    private List<AlgorithmStatResponse> toAlgorithmResponses(
            int totalSolvedCount,
            Map<String, AlgorithmCounter> algorithmCounters
    ) {
        return algorithmCounters.values().stream()
                .map(counter -> new AlgorithmStatResponse(
                        counter.code(),
                        counter.label(),
                        counter.solvedCount(),
                        percentage(counter.solvedCount(), totalSolvedCount)
                ))
                .toList();
    }

    private double percentage(int count, int total) {
        if (total <= 0) {
            return 0;
        }
        return Math.round(((double) count / total) * 1000.0) / 10.0;
    }

    private record DifficultyTier(
            String code,
            String label
    ) {
    }

    private static class AlgorithmCounter {

        private final String code;
        private final String label;
        private int solvedCount;

        AlgorithmCounter(String code, String label) {
            this.code = code;
            this.label = label;
        }

        void increase() {
            solvedCount++;
        }

        String code() {
            return code;
        }

        String label() {
            return label;
        }

        int solvedCount() {
            return solvedCount;
        }
    }
}
