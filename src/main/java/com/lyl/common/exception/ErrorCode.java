package com.lyl.common.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "duplicate-email", "이미 가입된 이메일입니다."),
    DELETED_MEMBER(HttpStatus.CONFLICT, "deleted-member", "탈퇴처리한 회원입니다. 계정 복구 후 이용해주세요."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "member-not-found", "사용자를 찾을 수 없습니다."),
    OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "oauth-email-required", "OAuth 계정에서 이메일을 확인할 수 없습니다."),
    OAUTH_ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "oauth-account-conflict", "이미 같은 이메일로 가입된 계정이 있습니다."),
    OAUTH_LOGIN_CODE_INVALID(HttpStatus.UNAUTHORIZED, "oauth-login-code-invalid", "OAuth 로그인 코드가 유효하지 않습니다."),
    OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "oauth-login-failed", "OAuth 로그인에 실패했습니다."),
    BOARD_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "board-post-not-found", "게시글을 찾을 수 없습니다."),
    BOARD_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "board-comment-not-found", "댓글을 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "invalid-credentials", "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "access-denied", "접근 권한이 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "validation-error", "요청 값이 올바르지 않습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad-request", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal-server-error", "서버 오류가 발생했습니다.");

    private static final String TYPE_PREFIX = "urn:lyl:error:";

    private final HttpStatus status;
    private final String type;
    private final String message;

    ErrorCode(HttpStatus status, String type, String message) {
        this.status = status;
        this.type = type;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public URI type() {
        return URI.create(TYPE_PREFIX + type);
    }

    public String message() {
        return message;
    }
}
