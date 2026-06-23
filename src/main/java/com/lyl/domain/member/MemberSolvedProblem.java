package com.lyl.domain.member;

import com.lyl.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "member_solved_problems",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_solved_problems_member_problem",
                columnNames = {"member_id", "problem_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSolvedProblem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "problem_id", nullable = false, length = 36)
    private String problemId;

    @Column(nullable = false)
    private int earnedXp;

    public MemberSolvedProblem(Member member, String problemId, int earnedXp) {
        this.member = member;
        this.problemId = problemId;
        this.earnedXp = earnedXp;
    }
}
