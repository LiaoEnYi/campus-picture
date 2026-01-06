package com.guang.campuspicturebackend.model.dto.picture;

import lombok.Data;

import java.util.List;

/**
 * @Author L.
 * @Date 2026/1/5 21:03
 * @Description TODO
 * @Version 1.0
 */
@Data
public class PictureEditByBatchRequest {
    private String nameRule;
    private List<Long> pictureIdList;
    private Long spaceId;
    private String category;
    private List<String> tags;
}
