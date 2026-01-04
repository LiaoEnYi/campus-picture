package com.guang.campuspicturebackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author L.
 * @Date 2026/1/4 15:54
 * @Description 空间级别
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    private int value;
    private String text;
    private long maxCount;
    private long maxSize;
}
