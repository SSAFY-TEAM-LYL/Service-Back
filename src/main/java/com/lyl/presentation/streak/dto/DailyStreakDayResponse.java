package com.lyl.presentation.streak.dto;

import java.time.LocalDate;

public record DailyStreakDayResponse(
        LocalDate date,
        boolean submitted,
        int submissionCount
) {
}
