package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.MemberSolvedProblem;
import com.lyl.domain.member.MemberSolvedProblemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberSolvedProblemRepositoryAdapter implements MemberSolvedProblemRepository {

    private final SpringDataMemberSolvedProblemRepository repository;

    @Override
    public boolean existsByMemberIdAndProblemId(Long memberId, String problemId) {
        return repository.existsByMemberIdAndProblemIdAndDeletedAtIsNull(memberId, problemId);
    }

    @Override
    public List<String> findProblemIdsByMemberId(Long memberId) {
        return repository.findProblemIdsByMemberId(memberId);
    }

    @Override
    public MemberSolvedProblem save(MemberSolvedProblem solvedProblem) {
        return repository.save(solvedProblem);
    }
}
