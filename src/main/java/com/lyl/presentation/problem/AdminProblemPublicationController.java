package com.lyl.presentation.problem;

import com.lyl.application.problem.ProblemPublicationService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.problem.dto.ProblemPublicationCreateRequest;
import com.lyl.presentation.problem.dto.ProblemPublicationResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/problem-publications")
@RequiredArgsConstructor
public class AdminProblemPublicationController {

    private final ProblemPublicationService problemPublicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<ProblemPublicationResponse>>> findPublished(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                problemPublicationService.findPublished(cursor, size, userPrincipal.getId())
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProblemPublicationResponse>> publish(
            @Valid @RequestBody ProblemPublicationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ProblemPublicationResponse response = problemPublicationService.publish(request, userPrincipal.getId());
        return ResponseEntity.created(URI.create("/api/admin/problem-publications/" + response.problemId()))
                .body(ApiResponse.success(response));
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<ApiResponse<Void>> unpublish(
            @PathVariable String problemId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        problemPublicationService.unpublish(problemId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
