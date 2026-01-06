package com.guang.campuspicturebackend.model.dto.picture;

import lombok.Data;

/**
 * @Author L.
 * @Date 2026/1/5 20:55
 * @Description SearchPictureByColorRequest
 * @Version 1.0
 */
@Data
public class SearchPictureByColorRequest {
    private String picColor;
    private Long spaceId;
}
