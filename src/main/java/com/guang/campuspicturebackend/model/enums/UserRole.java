package com.guang.campuspicturebackend.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * @Author L.
 * @Date 2025/12/17 13:45
 * @Description 用户角色
 * @Version 1.0
 */
@Getter
public enum UserRole {
    USER("用户", "user"),
    ADMIN("管理员", "admin");


    private final String text;
    private final String value;

    UserRole(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRole getRoleByValue(String value) {
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        for (UserRole o : UserRole.values()) {
            if (o.value.equals(value)) {
                return o;
            }
        }
        return null;
    }
}
