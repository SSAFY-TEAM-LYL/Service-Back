package com.lyl.domain.submission;

import com.lyl.domain.common.BaseEntity;
import com.lyl.domain.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "problem_id", nullable = false, length = 36)
    private String problemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionLanguage language;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SubmissionStatus status;

    @Column(nullable = false)
    private int totalTestCount;

    @Column(nullable = false)
    private int passedTestCount;

    @Column
    private Integer maxTimeMs;

    @Column
    private Integer maxMemoryKb;

    @Column
    private Integer firstFailedCaseSeq;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime judgedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionTestCaseResult> testCaseResults = new ArrayList<>();

    public Submission(Member member, String problemId, SubmissionLanguage language, String sourceCode, int totalTestCount) {
        this.member = member;
        this.problemId = problemId;
        this.language = language;
        this.sourceCode = sourceCode;
        this.status = SubmissionStatus.PENDING;
        this.totalTestCount = totalTestCount;
        this.passedTestCount = 0;
        this.submittedAt = LocalDateTime.now();
    }

    public void addTestCaseResult(SubmissionTestCaseResult result) {
        testCaseResults.add(result);
        result.assignSubmission(this);
    }

    public void markJudging() {
        this.status = SubmissionStatus.JUDGING;
    }

    public void markInternalError(String message) {
        this.status = SubmissionStatus.INTERNAL_ERROR;
        this.errorMessage = message;
        this.judgedAt = LocalDateTime.now();
    }

    public void aggregateCompletedResults() {
        this.passedTestCount = (int) testCaseResults.stream()
                .filter(SubmissionTestCaseResult::isAccepted)
                .count();
        this.maxTimeMs = testCaseResults.stream()
                .map(SubmissionTestCaseResult::getTimeMs)
                .filter(value -> value != null)
                .max(Integer::compareTo)
                .orElse(null);
        this.maxMemoryKb = testCaseResults.stream()
                .map(SubmissionTestCaseResult::getMemoryKb)
                .filter(value -> value != null)
                .max(Integer::compareTo)
                .orElse(null);

        testCaseResults.stream()
                .filter(result -> result.getStatus() != SubmissionStatus.ACCEPTED)
                .min(Comparator.comparingInt(SubmissionTestCaseResult::getCaseSeq))
                .ifPresentOrElse(
                        result -> {
                            this.status = result.getStatus();
                            this.firstFailedCaseSeq = result.getCaseSeq();
                            this.errorMessage = result.failureMessage();
                        },
                        () -> {
                            this.status = SubmissionStatus.ACCEPTED;
                            this.firstFailedCaseSeq = null;
                            this.errorMessage = null;
                        }
                );
        this.judgedAt = LocalDateTime.now();
    }

    public boolean isInProgress() {
        return this.status == SubmissionStatus.PENDING || this.status == SubmissionStatus.JUDGING;
    }
}
