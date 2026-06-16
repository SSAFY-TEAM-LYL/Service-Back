package com.lyl.domain.submission;

import com.lyl.domain.common.BaseEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "submission_test_case_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionTestCaseResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(nullable = false)
    private int caseSeq;

    @Column(length = 100)
    private String judge0Token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SubmissionStatus status;

    @Column
    private Integer timeMs;

    @Column
    private Integer memoryKb;

    @Lob
    @Column
    private String stderrText;

    @Lob
    @Column
    private String compileOutput;

    @Column(length = 1000)
    private String message;

    public SubmissionTestCaseResult(int caseSeq) {
        this.caseSeq = caseSeq;
        this.status = SubmissionStatus.PENDING;
    }

    void assignSubmission(Submission submission) {
        this.submission = submission;
    }

    public void assignJudge0Token(String judge0Token) {
        this.judge0Token = judge0Token;
        this.status = SubmissionStatus.JUDGING;
    }

    public void updateResult(
            SubmissionStatus status,
            Integer timeMs,
            Integer memoryKb,
            String stderrText,
            String compileOutput,
            String message
    ) {
        this.status = status;
        this.timeMs = timeMs;
        this.memoryKb = memoryKb;
        this.stderrText = stderrText;
        this.compileOutput = compileOutput;
        this.message = message;
    }

    public boolean isAccepted() {
        return this.status == SubmissionStatus.ACCEPTED;
    }

    public boolean isCompleted() {
        return this.status != SubmissionStatus.PENDING && this.status != SubmissionStatus.JUDGING;
    }

    public String failureMessage() {
        if (compileOutput != null && !compileOutput.isBlank()) {
            return compileOutput;
        }
        if (stderrText != null && !stderrText.isBlank()) {
            return stderrText;
        }
        return message;
    }
}
