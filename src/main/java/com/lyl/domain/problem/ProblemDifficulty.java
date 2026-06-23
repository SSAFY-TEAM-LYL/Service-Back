package com.lyl.domain.problem;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record ProblemDifficulty(
        String label,
        String tier,
        String roman,
        int xp
) {

    private static final Map<String, Integer> TIER_OFFSETS = Map.of(
            "BRONZE", 0,
            "SILVER", 1,
            "GOLD", 2,
            "PLATINUM", 3,
            "DIAMOND", 4
    );

    private static final Map<String, Integer> ROMAN_OFFSETS = Map.of(
            "V", 0,
            "IV", 1,
            "III", 2,
            "II", 3,
            "I", 4
    );

    public static Optional<ProblemDifficulty> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String[] parts = value.trim().split("\\s+");
        if (parts.length != 2) {
            return Optional.empty();
        }
        String tier = parts[0].toUpperCase(Locale.ROOT);
        String roman = parts[1].toUpperCase(Locale.ROOT);
        Integer tierOffset = TIER_OFFSETS.get(tier);
        Integer romanOffset = ROMAN_OFFSETS.get(roman);
        if (tierOffset == null || romanOffset == null) {
            return Optional.empty();
        }
        int xp = 10 + ((tierOffset * 5) + romanOffset) * 5;
        return Optional.of(new ProblemDifficulty(value.trim(), tier, roman, xp));
    }
}
