package com.lyl.presentation.board;

import com.lyl.application.board.BoardService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class BoardCommentController {

    private final BoardService boardService;

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<BoardCommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody BoardCommentUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        BoardCommentResponse response = boardService.updateComment(commentId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        boardService.deleteComment(commentId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
