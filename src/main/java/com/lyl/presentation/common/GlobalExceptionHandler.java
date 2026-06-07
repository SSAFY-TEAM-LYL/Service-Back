package com.lyl.presentation.common;

import com.lyl.common.exception.BusinessException;
import com.lyl.common.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return problemDetail(errorCode, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException e) {
        return problemDetail(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException e) {
        return problemDetail(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e) {
        return problemDetail(ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        String message = errors.isEmpty()
                ? ErrorCode.VALIDATION_ERROR.message()
                : String.join(", ", errors);
        ProblemDetail problemDetail = createProblemDetail(ErrorCode.VALIDATION_ERROR, message);
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.status()).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return problemDetail(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);
        return problemDetail(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ProblemDetail> problemDetail(ErrorCode errorCode) {
        return problemDetail(errorCode, errorCode.message());
    }

    private ResponseEntity<ProblemDetail> problemDetail(ErrorCode errorCode, String message) {
        ProblemDetail problemDetail = createProblemDetail(errorCode, message);
        return ResponseEntity.status(errorCode.status()).body(problemDetail);
    }

    private ProblemDetail createProblemDetail(ErrorCode errorCode, String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.status(), message);
        problemDetail.setType(errorCode.type());
        problemDetail.setTitle(errorCode.name());
        problemDetail.setProperty("code", errorCode.name());
        problemDetail.setProperty("message", message);
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return problemDetail;
    }
}
