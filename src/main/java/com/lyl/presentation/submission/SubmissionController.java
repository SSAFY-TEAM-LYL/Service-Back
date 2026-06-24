package com.lyl.presentation.submission;

import com.lyl.application.submission.SubmissionService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.common.CursorPageResponse;
import com.lyl.presentation.submission.dto.SubmissionCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionResponse;
import com.lyl.presentation.submission.dto.SubmissionReviewCreateRequest;
import com.lyl.presentation.submission.dto.SubmissionReviewResponse;
import com.lyl.presentation.submission.dto.SubmissionReviewUpdateRequest;
import com.lyl.presentation.submission.dto.SubmissionUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @PathVariable Long submissionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.findSubmission(submissionId)
        ));
    }

    @PutMapping("/api/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> updateSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.updateSubmission(submissionId, request, userPrincipal.getId())
        ));
    }

    @DeleteMapping("/api/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubmission(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        submissionService.deleteSubmission(submissionId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/api/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<CursorPageResponse<SubmissionResponse>>> findProblemSubmissions(
            @PathVariable String problemId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "false") boolean mine,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long memberId = mine ? userPrincipal.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.findProblemSubmissions(problemId, memberId, cursor, size)
        ));
    }

    @GetMapping("/api/submissions/{submissionId}/reviews")
    public ResponseEntity<ApiResponse<CursorPageResponse<SubmissionReviewResponse>>> findSubmissionReviews(
            @PathVariable Long submissionId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.findSubmissionReviews(submissionId, cursor, size)
        ));
    }

    @PostMapping("/api/submissions/{submissionId}/reviews")
    public ResponseEntity<ApiResponse<SubmissionReviewResponse>> createSubmissionReview(
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionReviewCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        SubmissionReviewResponse response = submissionService.createSubmissionReview(
                submissionId,
                request,
                userPrincipal.getId()
        );
        return ResponseEntity.created(URI.create("/api/submission-reviews/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @PutMapping("/api/submission-reviews/{reviewId}")
    public ResponseEntity<ApiResponse<SubmissionReviewResponse>> updateSubmissionReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody SubmissionReviewUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                submissionService.updateSubmissionReview(reviewId, request, userPrincipal.getId())
        ));
    }

    @DeleteMapping("/api/submission-reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubmissionReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        submissionService.deleteSubmissionReview(reviewId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
