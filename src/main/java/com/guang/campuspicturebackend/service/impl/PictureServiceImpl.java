package com.guang.campuspicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guang.campuspicturebackend.constant.MinioConstant;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.manager.upload.PictureUploadTemplate;
import com.guang.campuspicturebackend.model.dto.picture.PictureUploadRequest;
import com.guang.campuspicturebackend.model.dto.picture.PictureQueryRequest;
import com.guang.campuspicturebackend.model.dto.picture.PictureReviewRequest;
import com.guang.campuspicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.guang.campuspicturebackend.model.entity.Picture;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.enums.ReviewStatus;
import com.guang.campuspicturebackend.model.vo.PictureVO;
import com.guang.campuspicturebackend.model.vo.UploadPictureResult;
import com.guang.campuspicturebackend.model.vo.UserVO;
import com.guang.campuspicturebackend.service.PictureService;
import com.guang.campuspicturebackend.mapper.PictureMapper;
import com.guang.campuspicturebackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ocean
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-12-20 16:33:34
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private PictureUploadTemplate filePictureUpload;

    @Resource
    private PictureUploadTemplate urlPictureUpload;


    @Override
    public PictureVO uploadPicture(Object fileResource, PictureUploadRequest request, User loginUser) {
        // 判断图片是新增还是更新
        Long pictureId = null;
        if (request != null) {
            pictureId = request.getId();
        }
        // 如果更新图片判断图片是否存在
        if (pictureId != null) {
            Picture picture = this.getById(pictureId);
            ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            ThrowUtils.throwIf(!Objects.equals(picture.getUserId(), loginUser.getId()) || !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        }
        // 上传图片 得到图片信息
        Long userId = loginUser.getId();
        String prefix = String.format("%s/%s", MinioConstant.PUBLIC_PREFIX, userId);
        PictureUploadTemplate fileManager = filePictureUpload;
        if (fileResource instanceof String) {
            fileManager = urlPictureUpload;
        }
        UploadPictureResult uploadResult = fileManager.uploadPicture(fileResource, prefix);
        // 封装入库信息
        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadResult, picture);
        if (request != null && StrUtil.isNotBlank(request.getFileName())) {
            picture.setName(request.getFileName());
        }
        picture.setUserId(userId);
        // 填充审核信息
        this.fillReviewInfo(picture, loginUser);
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean b = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "图片上传失败");
        picture = this.getById(picture.getId());
        return PictureVO.objToVO(picture);
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVO(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> records = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(records)) {
            return pictureVOPage;
        }
        List<PictureVO> pictureVOS = records.stream().map(PictureVO::objToVO).toList();
        // 关联查询用户信息
        Set<Long> userIds = records.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> users = userService.listByIds(userIds).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        for (PictureVO pictureVO : pictureVOS) {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (users.containsKey(userId)) {
                user = users.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        }
        pictureVOPage.setRecords(pictureVOS);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjUtil.isNull(pictureId), ErrorCode.PARAMS_ERROR);
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 520, ErrorCode.PARAMS_ERROR, "introduction   过长");
        }
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest request) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        // 构建条件
        Long id = request.getId();
        String name = request.getName();
        String introduction = request.getIntroduction();
        String category = request.getCategory();
        List<String> tags = request.getTags();
        Long picSize = request.getPicSize();
        Integer picWidth = request.getPicWidth();
        Integer picHeight = request.getPicHeight();
        Double picScale = request.getPicScale();
        String picFormat = request.getPicFormat();
        String searchText = request.getSearchText();
        Long userId = request.getUserId();
        Integer reviewStatus = request.getReviewStatus();
        String reviewMessage = request.getReviewMessage();
        Long reviewId = request.getReviewId();
        String orderFiled = request.getOrderFiled();
        String sortOrder = request.getSortOrder();

        if (StrUtil.isNotBlank(searchText)) {
            wrapper.and(v -> v.like("name", searchText).or().like("introduction", searchText));
        }
        wrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        wrapper.like(StrUtil.isNotBlank(name), "name", name);
        wrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        wrapper.like(StrUtil.isNotBlank(reviewMessage), "review_message", reviewMessage);
        wrapper.eq(StrUtil.isNotBlank(category), "category", category);
        wrapper.eq(StrUtil.isNotBlank(picFormat), "pic_format", picFormat);
        wrapper.eq(ObjUtil.isNotEmpty(picSize), "pic_size", picSize);
        wrapper.eq(ObjUtil.isNotEmpty(picWidth), "pic_width", picWidth);
        wrapper.eq(ObjUtil.isNotEmpty(picHeight), "pic_height", picHeight);
        wrapper.eq(ObjUtil.isNotEmpty(picScale), "pic_scale", picScale);
        wrapper.eq(ObjUtil.isNotEmpty(userId), "user_id", userId);
        wrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "review_status", reviewStatus);
        wrapper.eq(ObjUtil.isNotEmpty(reviewId), "review_id", reviewId);
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                wrapper.like("tags", "\"" + tag + "\"");
            }
        }
        wrapper.orderBy(StrUtil.isNotEmpty(orderFiled), sortOrder.equals("desc"), orderFiled);
        return wrapper;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        // 已经审核过了的不允许再次审核
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        Long id = pictureReviewRequest.getId();
        ReviewStatus enumByValue = ReviewStatus.getEnumByValue(reviewStatus);
        if (id == null || id < 0 || enumByValue == null || enumByValue.equals(ReviewStatus.REVIEWED)) {
            throw new CustomException(ErrorCode.PARAMS_ERROR);
        }
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "该图片不存在");
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new CustomException(ErrorCode.OPERATION_ERROR, "该图片已经审核过了");
        }
        // 审核图片
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, picture);
        picture.setReviewId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean res = this.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "审核图片失败");
    }

    @Override
    public void fillReviewInfo(Picture picture, User loginUser) {
        // 管理员可以直接通过审核
        if (userService.isAdmin(loginUser)) {
            picture.setReviewTime(new Date());
            picture.setReviewId(loginUser.getId());
            picture.setReviewStatus(ReviewStatus.REVIEWED.getCode());
            picture.setReviewMessage("管理员自动通过审核");
        } else {
            picture.setReviewStatus(ReviewStatus.UNREVIEWED.getCode());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest request, User loginUser) {
        String namePrefix = request.getNamePrefix();
        String searchText = request.getSearchText();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索词不能为空");
        Integer count = request.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多只允许一次性抓取三十条");
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 拼接抓取参数 使用 https://cn.bing.com/images/async?q= ?               类：img.mimg
        String requestUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        // 使用jsoup抓取网页图片
        Document doc;
        try {
            doc = Jsoup.connect(requestUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new CustomException(ErrorCode.OPERATION_ERROR, "网络出现了点状况，请等待稍后重试");
        }
        Element div = doc.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(ObjUtil.isNull(div), ErrorCode.OPERATION_ERROR, "获取元素失败");
        Elements elements = div.select("img.mimg");
        int uploadCount = 0;
        for (Element e : elements) {
            String src = e.attr("src");
            if (StrUtil.isBlank(src)) {
                log.info("当前连接为空，已经跳过：{}", src);
                continue;
            }
            // 处理图片地址
            int lastIndexOf = src.indexOf("?");
            if (lastIndexOf > -1) {
                src = src.substring(0, lastIndexOf);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                pictureUploadRequest.setFileName(namePrefix + (uploadCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(src, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id={}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e1) {
                log.error("上传文件失败",e1);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }
}




