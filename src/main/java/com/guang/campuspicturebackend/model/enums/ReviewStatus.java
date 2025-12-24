package com.guang.campuspicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @Author L.
 * @Date 2025/12/23 16:04
 * @Description 审核状态
 * @Version 1.0
 */
@Getter
public enum ReviewStatus {
    UNREVIEWED("待审核", 0),
    REVIEWED("审核通过", 1),
    REJECTED("审核拒绝", 2);

    private final String text;
    private final int code;

    ReviewStatus(String text, int code) {
        this.text = text;
        this.code = code;
    }

    public static ReviewStatus getEnumByValue(Integer code) {
        if (ObjUtil.isNull(code)) {
            return null;
        }
        for (ReviewStatus status : ReviewStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
