package com.guang.campuspicturebackend.exception;

import lombok.Getter;

/**
 * @Author L.
 * @Date 2025/12/16 15:50
 * @Description 自定义异常
 * @Version 1.0
 */
@Getter
public class CustomException extends RuntimeException{
    private final int code;

    public CustomException(int code) {
        super();
        this.code = code;
    }
    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }
    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
