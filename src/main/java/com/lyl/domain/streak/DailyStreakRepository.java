package com.lyl.domain.streak;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStreakRepository {

    DailyStreak save(DailyStreak dailyStreak);

    Optional<DailyStreak> findByMemberIdAndStreakDate(Long memberId, LocalDate streakDate);

    List<DailyStreak> findByMemberIdFrom(Long memberId, LocalDate fromDate);

    List<DailyStreak> findAllByMemberId(Long memberId);
}
