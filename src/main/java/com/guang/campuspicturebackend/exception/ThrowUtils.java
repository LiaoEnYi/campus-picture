package com.guang.campuspicturebackend.exception;

/**
 * @Author L.
 * @Date 2025/12/16 15:52
 * @Description 异常工具类
 * @Version 1.0
 */
public class ThrowUtils {
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new CustomException(errorCode));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new CustomException(errorCode, message));
    }
}
