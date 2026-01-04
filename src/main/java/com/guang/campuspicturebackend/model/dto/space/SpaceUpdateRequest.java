package com.guang.campuspicturebackend.model.dto.space;

import lombok.Data;

/**
 * @Author L.
 * @Date 2026/1/1 21:34
 * @Description 空间更新请求（目前仅管理员可用）
 * @Version 1.0
 */
@Data
public class SpaceUpdateRequest {
    private Long id;
    private String spaceName;
    private Integer spaceLevel;
    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;
}
