package com.guang.campuspicturebackend.model.enums;

import lombok.Getter;

/**
 * @Author L.
 * @Date 2026/1/1 21:43
 * @Description 空间级别枚举类
 * @Version 1.0
 */
@Getter
public enum SpaceLevelEnum {
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);
    private final String text;
    private final int value;
    private final long maxCount;
    private final long maxSize;

    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    public static SpaceLevelEnum getEnumByValue(Integer level) {
        if (level == null || level < 0) {
            return null;
        }
        for (SpaceLevelEnum v : SpaceLevelEnum.values()) {
            if (v.value == level) {
                return v;
            }
        }
        return null;
    }
}
