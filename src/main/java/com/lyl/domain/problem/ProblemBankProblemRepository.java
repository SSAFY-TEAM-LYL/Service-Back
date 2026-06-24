package com.lyl.domain.problem;

import java.util.List;
import java.util.Optional;

public interface ProblemBankProblemRepository {

    List<ProblemSummary> findPublishedSummaries(int offset, int size);

    List<ProblemSummary> findSummariesByIds(List<String> problemIds);

    List<ProblemSummary> findSummariesByIds(
            List<String> problemIds,
            String difficultyTier,
            String algorithm,
            String query,
            int offset,
            int size
    );

    Optional<ProblemDetail> findDetailById(String problemId);

    Optional<String> findDifficultyById(String problemId);

    List<ProblemSolvedMetadata> findSolvedMetadataByIds(List<String> problemIds);

    List<ProblemAlgorithm> findAlgorithms();

    Optional<ProblemJudgingData> findJudgingDataById(String problemId);
}
