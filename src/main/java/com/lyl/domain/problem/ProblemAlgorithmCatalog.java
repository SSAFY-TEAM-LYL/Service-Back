package com.lyl.domain.problem;

import java.util.List;

public final class ProblemAlgorithmCatalog {

    private static final List<ProblemAlgorithm> SUPPORTED = List.of(
            new ProblemAlgorithm("dijkstra", "Dijkstra"),
            new ProblemAlgorithm("bfs", "BFS"),
            new ProblemAlgorithm("topological_sort", "Topological Sort"),
            new ProblemAlgorithm("bellman_ford", "Bellman-Ford"),
            new ProblemAlgorithm("floyd_warshall", "Floyd-Warshall"),
            new ProblemAlgorithm("kruskal_mst", "Kruskal MST"),
            new ProblemAlgorithm("max_flow", "Max Flow"),
            new ProblemAlgorithm("binary_search", "Binary Search"),
            new ProblemAlgorithm("lis", "LIS"),
            new ProblemAlgorithm("two_sum", "Two Sum"),
            new ProblemAlgorithm("sort_cluster", "Sort cluster"),
            new ProblemAlgorithm("string_match_cluster", "String Match cluster"),
            new ProblemAlgorithm("segtree", "Segment Tree"),
            new ProblemAlgorithm("heap", "Heap (Min-PQ)"),
            new ProblemAlgorithm("fenwick", "Fenwick Tree (BIT)"),
            new ProblemAlgorithm("union_find", "Union-Find"),
            new ProblemAlgorithm("knapsack_01", "Knapsack 0/1"),
            new ProblemAlgorithm("coin_change", "Coin Change"),
            new ProblemAlgorithm("sieve", "Sieve of Eratosthenes")
    );

    private ProblemAlgorithmCatalog() {
    }

    public static List<ProblemAlgorithm> supported() {
        return SUPPORTED;
    }
}
