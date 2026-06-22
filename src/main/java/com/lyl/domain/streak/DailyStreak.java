package com.lyl.domain.streak;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "daily_streaks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_streak_member_streak_date",
                columnNames = {"member_id", "streak_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStreak extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "streak_date", nullable = false)
    private LocalDate streakDate;

    @Column(name = "submission_count", nullable = false, columnDefinition = "integer default 1")
    private int submissionCount = 1;

    public DailyStreak(Member member, LocalDate streakDate) {
        this.member = member;
        this.streakDate = streakDate;
    }

    public void increaseSubmissionCount() {
        this.submissionCount++;
    }
}
