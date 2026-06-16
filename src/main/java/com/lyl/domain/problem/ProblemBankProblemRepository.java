package com.lyl.domain.problem;

import java.util.List;
import java.util.Optional;

public interface ProblemBankProblemRepository {

    List<ProblemSummary> findPublishedSummaries(int offset, int size);

    List<ProblemSummary> findSummariesByIds(List<String> problemIds);

    Optional<ProblemDetail> findDetailById(String problemId);

    Optional<ProblemJudgingData> findJudgingDataById(String problemId);
}
