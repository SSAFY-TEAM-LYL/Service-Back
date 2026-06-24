package com.lyl.presentation.problem;

import com.lyl.application.problem.ProblemQueryService;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemAlgorithmResponse;
import com.lyl.presentation.problem.dto.ProblemDetailResponse;
import com.lyl.presentation.problem.dto.ProblemServiceSummaryResponse;
import com.lyl.presentation.problem.dto.ProblemSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemQueryService problemQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<ProblemSummaryResponse>>> findProblems(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String algorithm,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(ApiResponse.success(problemQueryService.findProblems(
                cursor,
                size,
                difficulty,
                algorithm,
                query
        )));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ProblemServiceSummaryResponse>> findSummary() {
        return ResponseEntity.ok(ApiResponse.success(problemQueryService.findSummary()));
    }

    @GetMapping("/algorithms")
    public ResponseEntity<ApiResponse<List<ProblemAlgorithmResponse>>> findAlgorithms() {
        return ResponseEntity.ok(ApiResponse.success(problemQueryService.findAlgorithms()));
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<ProblemDetailResponse>> findProblem(@PathVariable String problemId) {
        return ResponseEntity.ok(ApiResponse.success(problemQueryService.findProblem(problemId)));
    }
}
