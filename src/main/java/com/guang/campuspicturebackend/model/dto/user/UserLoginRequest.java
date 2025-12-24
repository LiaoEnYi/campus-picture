package com.guang.campuspicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author L.
 * @Date 2025/12/17 16:33
 * @Description 用户登录请求
 * @Version 1.0
 */
@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 465465465L;
    private String userAccount;
    private String userPassword;
}
