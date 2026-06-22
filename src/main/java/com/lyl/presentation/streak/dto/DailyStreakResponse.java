package com.lyl.presentation.streak.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyStreakResponse(
        int currentStreak,
        int longestStreak,
        int totalActiveDays,
        boolean todaySubmitted,
        String zoneId,
        LocalDate startDate,
        LocalDate endDate,
        List<DailyStreakDayResponse> days
) {
}
