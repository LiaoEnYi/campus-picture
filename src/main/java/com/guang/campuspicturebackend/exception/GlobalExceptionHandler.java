package com.guang.campuspicturebackend.exception;

import com.guang.campuspicturebackend.common.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author L.
 * @Date 2025/12/16 15:37
 * @Description 全局异常处理
 * @Version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(exception = CustomException.class)
    public BaseResponse<?> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getMessage());
        return BaseResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(exception = RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage());
        return BaseResponse.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}