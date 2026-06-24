package com.lyl.domain.problem;

import java.util.Arrays;
import java.util.List;

public final class ProblemAlgorithmCatalog {

    private static final List<ProblemAlgorithm> SUPPORTED = Arrays.stream(ProblemAlgorithmType.values())
            .map(ProblemAlgorithmType::toProblemAlgorithm)
            .toList();

    private ProblemAlgorithmCatalog() {
    }

    public static List<ProblemAlgorithm> supported() {
        return SUPPORTED;
    }
}
