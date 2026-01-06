package com.guang.campuspicturebackend.model.dto.picture;

import com.guang.campuspicturebackend.api.aliyun.model.CreateOutPaintingTaskRequest;
import lombok.Data;

/**
 * @Author L.
 * @Date 2026/1/6 14:22
 * @Description TODO
 * @Version 1.0
 */
@Data
public class CreatePictureOutPaintingTaskRequest {
    private Long pictureId;

    private CreateOutPaintingTaskRequest.Parameters parameters;
}
