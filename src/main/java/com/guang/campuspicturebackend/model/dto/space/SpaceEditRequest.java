package com.guang.campuspicturebackend.model.dto.space;

import lombok.Data;

/**
 * @Author L.
 * @Date 2026/1/1 21:33
 * @Description 编辑空间请求
 * @Version 1.0
 */
@Data
public class SpaceEditRequest {
    private Long id;
    private String spaceName;
}
