package com.lyl.application.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lyl.domain.board.BoardComment;
import com.lyl.domain.board.BoardCommentRepository;
import com.lyl.domain.board.BoardCategory;
import com.lyl.domain.board.BoardPost;
import com.lyl.domain.board.BoardPostRepository;
import com.lyl.domain.board.exception.BoardAccessDeniedException;
import com.lyl.domain.board.exception.BoardCommentNotFoundException;
import com.lyl.domain.board.exception.BoardPostNotFoundException;
import com.lyl.domain.member.Member;
import com.lyl.domain.member.MemberRepository;
import com.lyl.domain.member.Role;
import com.lyl.presentation.board.dto.BoardCommentCreateRequest;
import com.lyl.presentation.board.dto.BoardCommentResponse;
import com.lyl.presentation.board.dto.BoardCommentUpdateRequest;
import com.lyl.presentation.board.dto.BoardPostCreateRequest;
import com.lyl.presentation.board.dto.BoardPostResponse;
import com.lyl.presentation.board.dto.BoardPostSummaryResponse;
import com.lyl.presentation.board.dto.BoardPostUpdateRequest;
import com.lyl.presentation.common.CursorPageResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardPostRepository boardPostRepository;

    @Autowired
    private BoardCommentRepository boardCommentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void updatePostUpdatesTitleAndContentWhenAuthorMatches() {
        Member author = saveMember("post-update-author@example.com", "postAuthor");
        BoardPost post = boardPostRepository.save(new BoardPost("old title", "old content", author));

        BoardPostResponse response = boardService.updatePost(
                post.getId(),
                new BoardPostUpdateRequest(BoardCategory.QUESTION, "new title", "new content"),
                author.getId()
        );

        assertThat(response.category()).isEqualTo(BoardCategory.QUESTION);
        assertThat(response.title()).isEqualTo("new title");
        assertThat(response.content()).isEqualTo("new content");
    }

    @Test
    void updatePostThrowsAccessDeniedWhenAuthorDoesNotMatch() {
        Member author = saveMember("post-owner@example.com", "postOwner");
        Member other = saveMember("post-other@example.com", "postOther");
        BoardPost post = boardPostRepository.save(new BoardPost("title", "content", author));

        assertThatThrownBy(() -> boardService.updatePost(
                post.getId(),
                new BoardPostUpdateRequest(BoardCategory.FREE, "new title", "new content"),
                other.getId()
        )).isInstanceOf(BoardAccessDeniedException.class);
    }

    @Test
    void createPostThrowsAccessDeniedWhenUserCreatesNotice() {
        Member author = saveMember("notice-user@example.com", "noticeUser");

        assertThatThrownBy(() -> boardService.createPost(
                new BoardPostCreateRequest(BoardCategory.NOTICE, "notice", "content"),
                author.getId()
        )).isInstanceOf(BoardAccessDeniedException.class);
    }

    @Test
    void createPostAllowsAdminToCreateNotice() {
        Member admin = saveMember("notice-admin@example.com", "noticeAdmin", Role.ADMIN);

        BoardPostResponse response = boardService.createPost(
                new BoardPostCreateRequest(BoardCategory.NOTICE, "notice", "content"),
                admin.getId()
        );

        assertThat(response.category()).isEqualTo(BoardCategory.NOTICE);
    }

    @Test
    void findPostsFiltersByCategoryWhenCategoryExists() {
        Member author = saveMember("category-filter-author@example.com", "categoryAuthor");
        boardPostRepository.save(new BoardPost("free", "content", BoardCategory.FREE, author));
        boardPostRepository.save(new BoardPost("question", "content", BoardCategory.QUESTION, author));

        assertThat(boardService.findPosts(BoardCategory.QUESTION, null, 10).items())
                .isNotEmpty()
                .allMatch(post -> post.category() == BoardCategory.QUESTION);
    }

    @Test
    void findPostsReturnsNextPageByCursor() {
        Member author = saveMember("post-cursor-author@example.com", "postCursorAuthor");
        boardPostRepository.save(new BoardPost("first", "content", BoardCategory.FREE, author));
        boardPostRepository.save(new BoardPost("second", "content", BoardCategory.FREE, author));
        boardPostRepository.save(new BoardPost("third", "content", BoardCategory.FREE, author));

        CursorPageResponse<BoardPostSummaryResponse> firstPage = boardService.findPosts(BoardCategory.FREE, null, 2);
        CursorPageResponse<BoardPostSummaryResponse> secondPage = boardService.findPosts(
                BoardCategory.FREE,
                firstPage.nextCursor(),
                2
        );

        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.items()).hasSize(2);
        assertThat(secondPage.items())
                .extracting(BoardPostSummaryResponse::id)
                .doesNotContainAnyElementsOf(firstPage.items().stream()
                        .map(BoardPostSummaryResponse::id)
                        .toList());
    }

    @Test
    void deletePostRemovesPostWhenAuthorMatches() {
        Member author = saveMember("post-delete-author@example.com", "postDeleteAuthor");
        BoardPost post = boardPostRepository.save(new BoardPost("title", "content", author));

        boardService.deletePost(post.getId(), author.getId());

        BoardPost deletedPost = entityManager.find(BoardPost.class, post.getId());
        assertThat(deletedPost.getDeletedAt()).isNotNull();
        assertThatThrownBy(() -> boardService.findPost(post.getId()))
                .isInstanceOf(BoardPostNotFoundException.class);
    }

    @Test
    void createCommentIncreasesPostCommentCount() {
        Member author = saveMember("comment-post-author@example.com", "commentPostAuthor");
        Member commenter = saveMember("commenter@example.com", "commenter");
        BoardPost post = boardPostRepository.save(new BoardPost("title", "content", author));

        BoardCommentResponse response = boardService.createComment(
                post.getId(),
                new BoardCommentCreateRequest("hello"),
                commenter.getId()
        );

        BoardPostResponse postResponse = boardService.findPost(post.getId());
        assertThat(response.content()).isEqualTo("hello");
        assertThat(postResponse.comments()).isEqualTo(1);
    }

    @Test
    void findCommentsReturnsNextPageByCursor() {
        Member author = saveMember("comment-cursor-author@example.com", "commentCursorAuthor");
        BoardPost post = boardPostRepository.save(new BoardPost("title", "content", author));
        BoardComment first = boardCommentRepository.save(new BoardComment("first", post, author));
        BoardComment second = boardCommentRepository.save(new BoardComment("second", post, author));
        BoardComment third = boardCommentRepository.save(new BoardComment("third", post, author));

        CursorPageResponse<BoardCommentResponse> firstPage = boardService.findComments(post.getId(), null, 2);
        CursorPageResponse<BoardCommentResponse> secondPage = boardService.findComments(
                post.getId(),
                firstPage.nextCursor(),
                2
        );

        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.items())
                .extracting(BoardCommentResponse::id)
                .contains(first.getId(), second.getId())
                .doesNotContain(third.getId());
        assertThat(secondPage.items())
                .extracting(BoardCommentResponse::id)
                .contains(third.getId())
                .doesNotContain(first.getId(), second.getId());
    }

    @Test
    void updateCommentUpdatesContentWhenAuthorMatches() {
        Member author = saveMember("comment-update-author@example.com", "commentUpdateAuthor");
        BoardPost post = boardPostRepository.save(new BoardPost("title", "content", author));
        BoardComment comment = boardCommentRepository.save(new BoardComment("old comment", post, author));

        BoardCommentResponse response = boardService.updateComment(
                comment.getId(),
                new BoardCommentUpdateRequest("new comment"),
                author.getId()
        );

        assertThat(response.content()).isEqualTo("new comment");
    }

    @Test
    void deleteCommentDecreasesPostCommentCount() {
        Member author = saveMember("comment-delete-author@example.com", "commentDeleteAuthor");
        BoardPost post = boardPostRepository.save(new BoardPost("title", "content", author));
        BoardCommentResponse comment = boardService.createComment(
                post.getId(),
                new BoardCommentCreateRequest("comment"),
                author.getId()
        );

        boardService.deleteComment(comment.id(), author.getId());

        entityManager.clear();

        BoardPostResponse postResponse = boardService.findPost(post.getId());
        assertThat(postResponse.comments()).isZero();
        assertThatThrownBy(() -> boardService.updateComment(
                comment.id(),
                new BoardCommentUpdateRequest("new comment"),
                author.getId()
        )).isInstanceOf(BoardCommentNotFoundException.class);
    }

    private Member saveMember(String email, String nickname) {
        return saveMember(email, nickname, Role.USER);
    }

    private Member saveMember(String email, String nickname, Role role) {
        return memberRepository.save(new Member(email, nickname, "encoded-password", role));
    }
}
