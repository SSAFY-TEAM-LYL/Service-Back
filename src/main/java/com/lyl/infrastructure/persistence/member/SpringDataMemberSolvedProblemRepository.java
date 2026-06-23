package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.MemberSolvedProblem;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMemberSolvedProblemRepository extends JpaRepository<MemberSolvedProblem, Long> {

    boolean existsByMemberIdAndProblemIdAndDeletedAtIsNull(Long memberId, String problemId);
}
