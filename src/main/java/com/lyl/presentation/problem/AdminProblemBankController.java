package com.lyl.presentation.problem;

import com.lyl.application.problem.ProblemPublicationService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.common.PageResponse;
import com.lyl.presentation.problem.dto.AdminProblemBankProblemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/problem-bank/problems")
@RequiredArgsConstructor
public class AdminProblemBankController {

    private final ProblemPublicationService problemPublicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminProblemBankProblemResponse>>> findProblemBankProblems(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                problemPublicationService.findProblemBankProblems(page, size, userPrincipal.getId())
        ));
    }
}
