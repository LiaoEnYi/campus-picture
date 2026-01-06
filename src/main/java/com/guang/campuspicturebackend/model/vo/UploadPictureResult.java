package com.guang.campuspicturebackend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author L.
 * @Date 2025/12/20 18:14
 * @Description 上传图片后返回的信息
 * @Version 1.0
 */
@Data
public class UploadPictureResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 45654654L;

    /**
     * url地址
     */
    private String url;

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
    private String tags;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片类型
     */
    private String picFormat;
    /**
     * 图片主色调
     */
    private String picColor;
}
