package com.guang.campuspicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guang.campuspicturebackend.model.dto.space.SpaceAddRequest;
import com.guang.campuspicturebackend.model.dto.space.SpaceQueryRequest;
import com.guang.campuspicturebackend.model.entity.Space;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.vo.SpaceVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Ocean
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-01-01 21:21:38
 */
public interface SpaceService extends IService<Space> {


    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest request);


    void validSpace(Space space, boolean add);

    void fillParamByLevel(Space space);

    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);
}
