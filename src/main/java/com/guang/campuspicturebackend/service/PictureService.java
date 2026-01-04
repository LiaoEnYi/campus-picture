package com.guang.campuspicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guang.campuspicturebackend.common.DeleteRequest;
import com.guang.campuspicturebackend.model.dto.picture.*;
import com.guang.campuspicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guang.campuspicturebackend.model.entity.User;
import com.guang.campuspicturebackend.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Ocean
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-12-20 16:33:34
 */
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     * @param fileResource
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object fileResource, PictureUploadRequest request, User loginUser);

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest request);

    /**
     * 图片审核
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewInfo(Picture picture, User loginUser);

    Integer uploadPictureByBatch(PictureUploadByBatchRequest request, User loginUser);

    void checkPictureAuth(Picture picture, User loginUser);

    void deletePicture(DeleteRequest deleteRequest, User loginUser);

    void clearPictureFile(Picture picture);

    void editPicture(PictureEditRequest pictureEditRequest, HttpServletRequest request);
}
