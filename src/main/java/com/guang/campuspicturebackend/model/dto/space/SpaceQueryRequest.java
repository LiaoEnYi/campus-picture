package com.guang.campuspicturebackend.model.dto.space;

import com.guang.campuspicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author L.
 * @Date 2026/1/1 21:36
 * @Description 空间查询请求
 * @Version 1.0
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest {
    private Long id;
    private String spaceName;
    private Integer spaceLevel;
    private Long userId;
}
