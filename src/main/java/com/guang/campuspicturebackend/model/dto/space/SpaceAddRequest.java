package com.guang.campuspicturebackend.model.dto.space;

import lombok.Data;

/**
 * @Author L.
 * @Date 2026/1/1 21:32
 * @Description 新增空间请求
 * @Version 1.0
 */
@Data
public class SpaceAddRequest {
    private String spaceName;
    private Integer spaceLevel;
}
