package com.lyl.infrastructure.persistence.member;

import com.lyl.domain.member.MemberSolvedProblem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataMemberSolvedProblemRepository extends JpaRepository<MemberSolvedProblem, Long> {

    boolean existsByMemberIdAndProblemIdAndDeletedAtIsNull(Long memberId, String problemId);

    @Query("""
            select solvedProblem.problemId
            from MemberSolvedProblem solvedProblem
            where solvedProblem.member.id = :memberId
              and solvedProblem.deletedAt is null
            order by solvedProblem.createdAt asc, solvedProblem.id asc
            """)
    List<String> findProblemIdsByMemberId(@Param("memberId") Long memberId);
}
