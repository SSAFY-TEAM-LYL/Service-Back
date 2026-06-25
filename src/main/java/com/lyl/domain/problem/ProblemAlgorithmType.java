package com.lyl.domain.problem;

import java.util.Arrays;
import java.util.Optional;

public enum ProblemAlgorithmType {

    ARITHMETIC("arithmetic", "Arithmetic"),
    BASIC_IO("basic_io", "Basic I/O"),
    CONDITIONAL("conditional", "Conditional"),
    LOOP_ACCUMULATE("loop_accumulate", "Loop & Accumulate"),
    DIJKSTRA("dijkstra", "Dijkstra"),
    LIS("lis", "LIS"),
    SEGTREE("segtree", "Segment Tree"),
    TWO_SUM("two_sum", "Two Sum"),
    BFS("bfs", "BFS"),
    BINARY_SEARCH("binary_search", "Binary Search"),
    UNION_FIND("union_find", "Union-Find"),
    TOPOSORT("toposort", "Toposort"),
    KNAPSACK("knapsack", "Knapsack"),
    SORT("sort", "Sort"),
    STRING_MATCH("string_match", "String Match"),
    MAX_FLOW("max_flow", "Max Flow"),
    SIEVE("sieve", "Sieve of Eratosthenes"),
    BELLMAN_FORD("bellman_ford", "Bellman-Ford"),
    FLOYD_WARSHALL("floyd_warshall", "Floyd-Warshall"),
    KRUSKAL_MST("kruskal_mst", "Kruskal MST"),
    HEAP("heap", "Heap"),
    FENWICK("fenwick", "Fenwick Tree"),
    COIN_CHANGE("coin_change", "Coin Change");

    private final String code;
    private final String label;

    ProblemAlgorithmType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public ProblemAlgorithm toProblemAlgorithm() {
        return new ProblemAlgorithm(code, label);
    }

    public static Optional<ProblemAlgorithmType> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalizedCode = code.trim();
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(normalizedCode))
                .findFirst();
    }
}
