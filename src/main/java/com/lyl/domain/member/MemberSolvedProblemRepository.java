package com.lyl.domain.member;

import java.util.List;

public interface MemberSolvedProblemRepository {

    boolean existsByMemberIdAndProblemId(Long memberId, String problemId);

    List<String> findProblemIdsByMemberId(Long memberId);

    MemberSolvedProblem save(MemberSolvedProblem solvedProblem);
}
