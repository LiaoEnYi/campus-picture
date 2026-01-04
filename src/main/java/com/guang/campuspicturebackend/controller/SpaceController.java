package com.guang.campuspicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guang.campuspicturebackend.annotation.AuthCheck;
import com.guang.campuspicturebackend.common.BaseResponse;
import com.guang.campuspicturebackend.common.DeleteRequest;
import com.guang.campuspicturebackend.constant.UserConstant;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.model.dto.space.*;
import com.guang.campuspicturebackend.model.dto.space.SpaceUpdateRequest;
import com.guang.campuspicturebackend.model.entity.Space;
import com.guang.campuspicturebackend.model.entity.User;

import com.guang.campuspicturebackend.model.enums.SpaceLevelEnum;
import com.guang.campuspicturebackend.model.vo.SpaceLevel;
import com.guang.campuspicturebackend.model.vo.SpaceVO;
import com.guang.campuspicturebackend.service.SpaceService;
import com.guang.campuspicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author L.
 * @Date 2026/1/3 20:20
 * @Description space空间controller
 * @Version 1.0
 */
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<SpaceVO> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        spaceService.addSpace(spaceAddRequest, loginUser);
        return null;
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() < 0) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        spaceService.fillParamByLevel(space);
        spaceService.validSpace(space, false);
        // 判断空间是否存在
        Long spaceId = space.getId();
        Space oldSpace = spaceService.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        boolean res = spaceService.updateById(space);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success(true);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();

        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new CustomException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success(true);
    }


    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        return BaseResponse.success(space);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Space> spacePage = spaceService.page(new Page<>(current, size), spaceService.getQueryWrapper(spaceQueryRequest));
        Page<SpaceVO> spaceVOPage = spaceService.getSpaceVOPage(spacePage, request);
        return BaseResponse.success(spaceVOPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        space.setEditTime(new Date());
        spaceService.validSpace(space, false);
        User loginUser = userService.getLoginUser(request);
        Long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新数据库
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new CustomException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success(true);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()).map(v -> new SpaceLevel(
                v.getValue(),
                v.getText(),
                v.getMaxCount(),
                v.getMaxSize()
        )).collect(Collectors.toList());
        return BaseResponse.success(spaceLevelList);
    }
}
