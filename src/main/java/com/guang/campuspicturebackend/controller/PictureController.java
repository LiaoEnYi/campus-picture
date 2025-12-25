package com.guang.campuspicturebackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.guang.campuspicturebackend.annotation.AuthCheck;
import com.guang.campuspicturebackend.common.BaseResponse;
import com.guang.campuspicturebackend.common.DeleteRequest;
import com.guang.campuspicturebackend.constant.RedisPrefixConstant;
import com.guang.campuspicturebackend.constant.UserConstant;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.model.dto.picture.PictureUploadRequest;
import com.guang.campuspicturebackend.model.dto.picture.*;
import com.guang.campuspicturebackend.model.entity.Picture;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.enums.ReviewStatus;
import com.guang.campuspicturebackend.model.vo.PictureTagCategory;
import com.guang.campuspicturebackend.model.vo.PictureVO;
import com.guang.campuspicturebackend.service.PictureService;
import com.guang.campuspicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author L.
 * @Date 2025/12/21 10:15
 * @Description Picture Controller
 * @Version 1.0
 */
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 构建caffeine 本地缓存
    private final Cache<String, String> caffeine = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 @RequestBody PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        // 得到当前登录用户
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return BaseResponse.success(pictureVO);
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureWithUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                        HttpServletRequest request) {
        String fileUrl = pictureUploadRequest.getFileUrl();
        // 得到当前登录用户
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return BaseResponse.success(pictureVO);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();

        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new CustomException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() < 0) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        pictureService.validPicture(picture);
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 填充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewInfo(picture, loginUser);
        // 更新数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return BaseResponse.success(picture);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(ReviewStatus.REVIEWED.getCode());
        // 使用redis缓存数据
        // 1. 构造key
        String key = String.format("%s%s", RedisPrefixConstant.PICTURE_BY_PAGE, DigestUtils.md5DigestAsHex(JSONUtil.parse(pictureQueryRequest).toString().getBytes()));
        // 两级缓存
        String cacheVal = caffeine.get(key, v -> {
            return null;
        });
        if (cacheVal != null) {
            Page<PictureVO> res = JSONUtil.toBean(cacheVal, Page.class);
            return BaseResponse.success(res);
        }
        // 查询redis
        cacheVal = stringRedisTemplate.opsForValue().get(key);
        if (cacheVal != null) {
            caffeine.put(key, cacheVal);
            Page<PictureVO> res = JSONUtil.toBean(cacheVal, Page.class);
            return BaseResponse.success(res);
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 将结果存放到redis 缓存中 并且设置随机过期时间 防止缓存雪崩
        cacheVal = JSONUtil.parse(pictureVOPage).toString();
        stringRedisTemplate.opsForValue().set(key, cacheVal, RandomUtil.randomInt(5, 11), TimeUnit.MINUTES);
        caffeine.put(key, cacheVal);
        return BaseResponse.success(pictureVOPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        Long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 填充审核参数
        pictureService.fillReviewInfo(picture, loginUser);
        // 更新数据库
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new CustomException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return BaseResponse.success(true);
    }

    @GetMapping("/category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = List.of("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = List.of("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return BaseResponse.success(pictureTagCategory);
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return BaseResponse.success(true);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest batchRequest,
                                                      HttpServletRequest servletRequest) {
        ThrowUtils.throwIf(batchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(servletRequest);
        Integer res = pictureService.uploadPictureByBatch(batchRequest, loginUser);
        return BaseResponse.success(res);
    }
}












