package com.guang.campuspicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author L.
 * @Date 2025/12/21 10:42
 * @Description 图片修改请求
 * @Version 1.0
 */
@Data
public class PictureEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 图片名
     */
    private String name;
    /**
     * 简介
     */
    private String introduction;

    /**
     * 类别
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
}
