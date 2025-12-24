package com.guang.campuspicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author L.
 * @Date 2025/12/21 11:48
 * @Description TODO
 * @Version 1.0
 */
@Data
public class PictureTagCategory {
    private List<String> tagList;
    private List<String> categoryList;
}
