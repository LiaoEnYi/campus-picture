package com.guang.campuspicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author L.
 * @Date 2025/12/17 13:57
 * @Description 用户注册请求
 * @Version 1.0
 */
@Data
public class UserRegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 465465465L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
