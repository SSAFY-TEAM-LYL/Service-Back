package com.lyl.presentation.common;

public record ApiResponse<T>(
        String code,
        String message,
        T data
) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공했습니다.";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS_CODE, message, data);
    }
}
