package com.lyl.domain.member;

public interface MemberSolvedProblemRepository {

    boolean existsByMemberIdAndProblemId(Long memberId, String problemId);

    MemberSolvedProblem save(MemberSolvedProblem solvedProblem);
}
