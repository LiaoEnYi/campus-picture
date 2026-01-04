package com.guang.campuspicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.mapper.SpaceMapper;
import com.guang.campuspicturebackend.model.dto.space.SpaceAddRequest;
import com.guang.campuspicturebackend.model.dto.space.SpaceQueryRequest;
import com.guang.campuspicturebackend.model.entity.Space;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.enums.SpaceLevelEnum;
import com.guang.campuspicturebackend.model.vo.SpaceVO;
import com.guang.campuspicturebackend.model.vo.UserVO;
import com.guang.campuspicturebackend.service.SpaceService;
import com.guang.campuspicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Ocean
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-01-01 21:21:38
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    private static final Map<Long, Object> USER_LOCKS = new ConcurrentHashMap<>();

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVO(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> records = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(records)) {
            return spaceVOPage;
        }
        List<SpaceVO> spaceVOS = records.stream().map(SpaceVO::objToVO).toList();
        // 关联查询用户信息
        Set<Long> userIds = records.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> users = userService.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        for (SpaceVO spaceVO : spaceVOS) {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (users.containsKey(userId)) {
                user = users.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        }
        spaceVOPage.setRecords(spaceVOS);
        return spaceVOPage;
    }


    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest request) {
        QueryWrapper<Space> wrapper = new QueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        // 构建条件
        Long id = request.getId();
        String spaceName = request.getSpaceName();
        Integer spaceLevel = request.getSpaceLevel();
        Long userId = request.getUserId();
        String orderFiled = request.getOrderFiled();
        String sortOrder = request.getSortOrder();

        wrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        wrapper.eq(StrUtil.isNotBlank(spaceName), "space_name", spaceName);

        wrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "space_level", spaceLevel);
        wrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);

        wrapper.orderBy(StrUtil.isNotEmpty(orderFiled), sortOrder.equals("desc"), orderFiled);
        return wrapper;
    }


    @Override
    public void validSpace(Space space, boolean add) {
        // 校验空间
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if (add) {
            // 新建空间
            if (StrUtil.isBlank(spaceName)) {
                throw new CustomException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (ObjUtil.isNull(spaceLevel)) {
                throw new CustomException(ErrorCode.PARAMS_ERROR, "必须指定空间级别");
            }
        }
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new CustomException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new CustomException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public void fillParamByLevel(Space space) {
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if (enumByValue == null) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        Long maxCount = space.getMaxCount();
        Long maxSize = space.getMaxSize();
        if (maxCount == null) {
            space.setMaxCount(enumByValue.getMaxCount());
        }
        if (maxSize == null) {
            space.setMaxSize(enumByValue.getMaxSize());
        }
    }

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 用户仅能创建一个私有空间
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 填充默认参数值
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (space.getSpaceName() == null) {
            space.setSpaceName("默认空间");
        }
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        fillParamByLevel(space);
        validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new CustomException(ErrorCode.NO_AUTH_ERROR);
        }
        // 限定用户只能创建一个空间
        Object key = USER_LOCKS.computeIfAbsent(userId, id -> new Object());
        synchronized (key) {
            Long spaceId = transactionTemplate.execute(status -> {
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "只能创建一个空间");
                boolean saved = this.save(space);
                ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR);
                return space.getId();
            });
            return Optional.ofNullable(spaceId).orElse(-1L);
        }
    }
}




