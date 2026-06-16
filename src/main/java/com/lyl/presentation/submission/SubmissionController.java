package com.lyl.presentation.submission;

import com.lyl.application.submission.SubmissionService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.submission.dto.SubmissionCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/api/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> createSubmission(
            @PathVariable String problemId,
            @Valid @RequestBody SubmissionCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        SubmissionResponse response = submissionService.createSubmission(problemId, request, userPrincipal.getId());
        return ResponseEntity.created(URI.create("/api/submissions/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> findSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.findSubmission(submissionId, userPrincipal.getId())
        ));
    }

    @GetMapping("/api/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<CursorPageResponse<SubmissionResponse>>> findProblemSubmissions(
            @PathVariable String problemId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.findProblemSubmissions(problemId, userPrincipal.getId(), cursor, size)
        ));
    }
}
