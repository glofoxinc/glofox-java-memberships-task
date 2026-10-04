package com.glofox.memberships.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        boolean success,
        LocalDateTime timeStamp,
        T data,
        ApiError error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, LocalDateTime.now(), data, null);
    }

    public static <T> ApiResponse<T> failure(
            String code,
            String message) {

        return new ApiResponse<>(
                false,
                LocalDateTime.now(),
                null,
                new ApiError(code, message)
        );
    }

    public record ApiError(
            String code,
            String message
    ) {
    }
}