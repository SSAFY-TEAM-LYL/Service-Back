package com.lyl.presentation.board;

import com.lyl.application.board.BoardService;
import com.lyl.infrastructure.security.UserPrincipal;
import com.lyl.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardPostSummaryResponse>>> findPosts() {
        return ResponseEntity.ok(ApiResponse.success(boardService.findPosts()));
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
}
