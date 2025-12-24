package com.guang.campuspicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author L.
 * @Date 2025/12/24 14:26
 * @Description 批量抓取图片请求
 * @Version 1.0
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {
    /**
     * 搜索词
     */
    private String searchText;
    /**
     * 抓取的数量
     */
    private Integer count;
    /**
     * 图片统一前缀名称
     */
    private String namePrefix;
}
