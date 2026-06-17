package com.lyl.domain.submission;

import com.lyl.domain.common.BaseEntity;
import com.lyl.domain.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "submission_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public SubmissionReview(Submission submission, Member author, String content) {
        this.submission = submission;
        this.author = author;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isWrittenBy(Long memberId) {
        return author.getId().equals(memberId);
    }
}
