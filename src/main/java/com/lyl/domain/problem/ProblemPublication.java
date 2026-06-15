package com.lyl.domain.problem;

import com.lyl.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "problem_publications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_problem_publications_problem_id",
                columnNames = "problem_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemPublication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false, unique = true, length = 36)
    private String problemId;

    public ProblemPublication(String problemId) {
        this.problemId = problemId;
    }

    public void publish() {
        restore();
    }

    public void unpublish() {
        delete();
    }

    public boolean isPublished() {
        return !isDeleted();
    }
}
