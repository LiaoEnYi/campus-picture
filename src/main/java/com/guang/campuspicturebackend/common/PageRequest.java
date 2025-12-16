package com.guang.campuspicturebackend.common;

import lombok.Data;

/**
 * @Author L.
 * @Date 2025/12/16 16:14
 * @Description 分页请求
 * @Version 1.0
 */
@Data
public class PageRequest {
    /**
     * 当前页
     */
    private int current = 1;
    /**
     * 页面大小
     */
    private int pageSize = 10;
    /**
     * 排序字段
     */
    private String orderFiled;
    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "desc";
}
