package com.guang.campuspicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 图片
 * @TableName picture
 */
@TableName(value = "picture")
@Data
public class Picture {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
    /**
     * 创建者id
     */
    private Long userId;
    /**
     * 空间ID
     */
    private Long spaceId;
    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 审核人id
     */
    private Long reviewId;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}