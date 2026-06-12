package com.lyl.application.board;

import com.lyl.application.common.Cursor;
import com.lyl.application.common.CursorCodec;
import com.lyl.domain.board.BoardCategory;
import com.lyl.domain.board.BoardComment;
import com.lyl.domain.board.BoardCommentRepository;
import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardPostRepository;
import com.lyl.domain.board.exception.BoardAccessDeniedException;
import com.lyl.domain.board.exception.BoardCommentNotFoundException;
import com.lyl.domain.board.exception.BoardPostNotFoundException;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.Role;
import com.lyl.domain.member.exception.MemberNotFoundException;
import com.lyl.presentation.board.dto.BoardCommentCreateRequest;
import com.lyl.presentation.board.dto.BoardCommentResponse;
import com.lyl.presentation.board.dto.BoardCommentUpdateRequest;
import com.lyl.presentation.board.dto.BoardPostCreateRequest;
import com.lyl.presentation.board.dto.BoardPostResponse;
import com.lyl.presentation.board.dto.BoardPostSummaryResponse;
import com.lyl.presentation.board.dto.BoardPostUpdateRequest;
import com.lyl.presentation.common.CursorPageResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final BoardPostRepository boardPostRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final MemberRepository memberRepository;
    private final CursorCodec cursorCodec;

    @Transactional(readOnly = true)
    public CursorPageResponse<BoardPostSummaryResponse> findPosts(BoardCategory category, String cursor, Integer size) {
        int pageSize = normalizeSize(size);
        Cursor decodedCursor = cursorCodec.decode(cursor);
        List<BoardPost> posts = boardPostRepository.findPage(
                category,
                cursorCreatedAt(decodedCursor),
                cursorId(decodedCursor),
                pageSize + 1
        );
        boolean hasNext = posts.size() > pageSize;
        List<BoardPost> pagePosts = posts.stream()
                .limit(pageSize)
                .toList();
        String nextCursor = hasNext && !pagePosts.isEmpty()
                ? cursorCodec.encode(pagePosts.getLast().getCreatedAt(), pagePosts.getLast().getId())
                : null;
        List<BoardPostSummaryResponse> items = pagePosts.stream()
                .map(BoardPostSummaryResponse::from)
                .toList();
        return new CursorPageResponse<>(items, nextCursor, hasNext);
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
        validateNoticeWritable(request.category(), author);
        BoardPost post = new BoardPost(request.title(), request.content(), request.category(), author);
        BoardPost savedPost = boardPostRepository.save(post);
        return BoardPostResponse.from(savedPost);
    }

    @Transactional
    public BoardPostResponse updatePost(Long id, BoardPostUpdateRequest request, Long authorId) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(BoardPostNotFoundException::new);
        validatePostAuthor(post, authorId);
        Member author = memberRepository.findById(authorId)
                .orElseThrow(MemberNotFoundException::new);
        validateNoticeWritable(request.category(), author);
        post.update(request.title(), request.content(), request.category());
        return BoardPostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, Long authorId) {
        BoardPost post = boardPostRepository.findById(id)
                .orElseThrow(BoardPostNotFoundException::new);
        validatePostAuthor(post, authorId);
        boardCommentRepository.findAllByPostIdOrderByCreatedAtAsc(post.getId())
                .forEach(BoardComment::delete);
        boardPostRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<BoardCommentResponse> findComments(Long postId, String cursor, Integer size) {
        if (boardPostRepository.findById(postId).isEmpty()) {
            throw new BoardPostNotFoundException();
        }
        int pageSize = normalizeSize(size);
        Cursor decodedCursor = cursorCodec.decode(cursor);
        List<BoardComment> comments = boardCommentRepository.findPageByPostId(
                postId,
                cursorCreatedAt(decodedCursor),
                cursorId(decodedCursor),
                pageSize + 1
        );
        boolean hasNext = comments.size() > pageSize;
        List<BoardComment> pageComments = comments.stream()
                .limit(pageSize)
                .toList();
        String nextCursor = hasNext && !pageComments.isEmpty()
                ? cursorCodec.encode(pageComments.getLast().getCreatedAt(), pageComments.getLast().getId())
                : null;
        List<BoardCommentResponse> items = pageComments.stream()
                .map(BoardCommentResponse::from)
                .toList();
        return new CursorPageResponse<>(items, nextCursor, hasNext);
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
        BoardPost post = comment.getPost();
        post.decreaseCommentCount();
        boardCommentRepository.delete(comment);
    }

    private void validatePostAuthor(BoardPost post, Long authorId) {
        if (!post.isWrittenBy(authorId)) {
            throw new BoardAccessDeniedException();
        }
    }

    private void validateNoticeWritable(BoardCategory category, Member author) {
        if (category == BoardCategory.NOTICE && author.getRole() != Role.ADMIN) {
            throw new BoardAccessDeniedException();
        }
    }

    private void validateCommentAuthor(BoardComment comment, Long authorId) {
        if (!comment.isWrittenBy(authorId)) {
            throw new BoardAccessDeniedException();
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private LocalDateTime cursorCreatedAt(Cursor cursor) {
        return cursor == null ? null : cursor.createdAt();
    }

    private Long cursorId(Cursor cursor) {
        return cursor == null ? null : cursor.id();
    }
}
