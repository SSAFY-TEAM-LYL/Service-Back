package com.lyl.application.board;

import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardPostRepository;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.presentation.board.BoardPostCreateRequest;
import com.lyl.presentation.board.BoardPostResponse;
import com.lyl.presentation.board.BoardPostSummaryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardPostRepository boardPostRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<BoardPostSummaryResponse> findPosts() {
        return boardPostRepository.findAllOrderByCreatedAtDesc().stream()
                .map(BoardPostSummaryResponse::from)
                .toList();
    }

    @Transactional
    public BoardPostResponse findPost(Long id) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        post.increaseViewCount();
        return BoardPostResponse.from(post);
    }

    @Transactional
    public BoardPostResponse createPost(BoardPostCreateRequest request, Long authorId) {
        Member author = memberRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        BoardPost post = new BoardPost(request.title(), request.content(), author);
        BoardPost savedPost = boardPostRepository.save(post);
        return BoardPostResponse.from(savedPost);
    }
}
