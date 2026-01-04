package com.guang.campuspicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author L.
 * @Date 2025/12/20 17:59
 * @Description 上传文件请求
 * @Version 1.0
 */
@Data
public class PictureUploadRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 45654654L;
    /**
     * 图片 ID（用于修改图片）
     */
    private Long id;
    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 空间id
     */
    private Long spaceId;
}
