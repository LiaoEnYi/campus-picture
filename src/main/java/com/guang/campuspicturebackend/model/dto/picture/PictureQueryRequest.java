package com.guang.campuspicturebackend.model.dto.picture;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.guang.campuspicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author L.
 * @Date 2025/12/21 10:43
 * @Description 图片分页请求
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
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
     * 搜索词（同时搜索名称，简介等）
     */
    private String searchText;
    /**
     * 创建者id
     */
    private Long userId;
    /**
     * 空间 id
     */
    private Long spaceId;
    /**
     * 由于公共图库是没有id的，如果是查询公共图库就要查询space_id字段为null
     */
    private boolean nullSpaceId;
    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
    /**
     * 开始编辑时间（范围查询）
     */
    private Date startEditTime;
    /**
     * 结束编辑时间（范围查询）
     */
    private Date endEditTime;

    /**
     * 审核人id
     */
    private Long reviewId;
}
