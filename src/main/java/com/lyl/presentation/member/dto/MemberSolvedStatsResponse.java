package com.lyl.presentation.member.dto;

import java.util.List;

public record MemberSolvedStatsResponse(
        int totalSolvedCount,
        List<DifficultyStatResponse> difficulties,
        List<AlgorithmStatResponse> algorithms
) {

    public record DifficultyStatResponse(
            String tier,
            String label,
            int solvedCount,
            double percent,
            List<DifficultyLevelStatResponse> levels
    ) {
    }

    public record DifficultyLevelStatResponse(
            String level,
            String label,
            int solvedCount
    ) {
    }

    public record AlgorithmStatResponse(
            String code,
            String label,
            int solvedCount,
            double percent
    ) {
    }
}
