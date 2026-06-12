package com.lyl.presentation.board;

import com.lyl.domain.board.BoardCategory;
import com.lyl.application.board.BoardService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.board.dto.*;
import com.lyl.presentation.common.ApiResponse;
import com.lyl.presentation.common.CursorPageResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<BoardPostSummaryResponse>>> findPosts(
            @RequestParam(required = false) BoardCategory category,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.success(boardService.findPosts(category, cursor, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostResponse>> findPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(boardService.findPost(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoardPostResponse>> createPost(
            @Valid @RequestBody BoardPostCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        BoardPostResponse response = boardService.createPost(request, userPrincipal.getId());
        return ResponseEntity.created(URI.create("/api/boards/" + response.id()))
                .body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody BoardPostUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        BoardPostResponse response = boardService.updatePost(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        boardService.deletePost(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CursorPageResponse<BoardCommentResponse>>> findComments(
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.success(boardService.findComments(postId, cursor, size)));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<BoardCommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody BoardCommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        BoardCommentResponse response = boardService.createComment(postId, request, userPrincipal.getId());
        return ResponseEntity.created(URI.create("/api/comments/" + response.id()))
                .body(ApiResponse.success(response));
    }
}
