package com.lyl.infrastructure.problem;

import com.lyl.domain.problem.ProblemBankProblemRepository;
import com.lyl.domain.problem.ProblemDetail;
import com.lyl.domain.problem.ProblemSummary;
import com.lyl.domain.problem.exception.ProblemBankUnavailableException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UnavailableProblemBankProblemRepository implements ProblemBankProblemRepository {

    @Override
    public List<ProblemSummary> findPublishedSummaries(int offset, int size) {
        throw new ProblemBankUnavailableException();
    }

    @Override
    public List<ProblemSummary> findSummariesByIds(List<String> problemIds) {
        throw new ProblemBankUnavailableException();
    }

    @Override
    public Optional<ProblemDetail> findDetailById(String problemId) {
        throw new ProblemBankUnavailableException();
    }
}
