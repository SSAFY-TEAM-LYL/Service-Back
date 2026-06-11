package com.lyl.application.board;

import com.lyl.domain.board.BoardComment;
import com.lyl.domain.board.BoardCommentRepository;
import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardPostRepository;
import com.lyl.domain.board.exception.BoardAccessDeniedException;
import com.lyl.domain.board.exception.BoardCommentNotFoundException;
import com.lyl.domain.board.exception.BoardPostNotFoundException;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.presentation.board.dto.BoardCommentCreateRequest;
import com.lyl.presentation.board.dto.BoardCommentResponse;
import com.lyl.presentation.board.dto.BoardCommentUpdateRequest;
import com.lyl.presentation.board.dto.BoardPostCreateRequest;
import com.lyl.presentation.board.dto.BoardPostResponse;
import com.lyl.presentation.board.dto.BoardPostSummaryResponse;
import com.lyl.presentation.board.dto.BoardPostUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
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
                .orElseThrow(BoardPostNotFoundException::new);
        post.increaseViewCount();
        return BoardPostResponse.from(post);
    }

    @Transactional
    public BoardPostResponse createPost(BoardPostCreateRequest request, Long authorId) {
        Member author = memberRepository.findById(authorId)
                .orElseThrow(MemberNotFoundException::new);
        BoardPost post = new BoardPost(request.title(), request.content(), author);
        BoardPost savedPost = boardPostRepository.save(post);
        return BoardPostResponse.from(savedPost);
    }

    @Transactional
    public BoardPostResponse updatePost(Long id, BoardPostUpdateRequest request, Long authorId) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(BoardPostNotFoundException::new);
        validatePostAuthor(post, authorId);
        post.update(request.title(), request.content());
        return BoardPostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, Long authorId) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(BoardPostNotFoundException::new);
        validatePostAuthor(post, authorId);
        boardPostRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<BoardCommentResponse> findComments(Long postId) {
        if (boardPostRepository.findById(postId).isEmpty()) {
            throw new BoardPostNotFoundException();
        }
        return boardCommentRepository.findAllByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(BoardCommentResponse::from)
                .toList();
    }

    @Transactional
    public BoardCommentResponse createComment(Long postId, BoardCommentCreateRequest request, Long authorId) {
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(BoardPostNotFoundException::new);
        Member author = memberRepository.findById(authorId)
                .orElseThrow(MemberNotFoundException::new);
        BoardComment comment = new BoardComment(request.content(), post, author);
        BoardComment savedComment = boardCommentRepository.save(comment);
        post.increaseCommentCount();
        return BoardCommentResponse.from(savedComment);
    }

    @Transactional
    public BoardCommentResponse updateComment(Long commentId, BoardCommentUpdateRequest request, Long authorId) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(BoardCommentNotFoundException::new);
        validateCommentAuthor(comment, authorId);
        comment.update(request.content());
        return BoardCommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long authorId) {
        BoardComment comment = boardCommentRepository.findById(commentId)
                .orElseThrow(BoardCommentNotFoundException::new);
        validateCommentAuthor(comment, authorId);
        comment.getPost().decreaseCommentCount();
        boardCommentRepository.delete(comment);
    }

    private void validatePostAuthor(BoardPost post, Long authorId) {
        if (!post.isWrittenBy(authorId)) {
            throw new BoardAccessDeniedException();
        }
    }

    private void validateCommentAuthor(BoardComment comment, Long authorId) {
        if (!comment.isWrittenBy(authorId)) {
            throw new BoardAccessDeniedException();
        }
    }
}
