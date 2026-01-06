package com.guang.campuspicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.guang.campuspicturebackend.config.MinioConfig;
import com.guang.campuspicturebackend.constant.MinioConstant;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import com.guang.campuspicturebackend.manager.MinioManager;
import com.guang.campuspicturebackend.model.vo.UploadPictureResult;
import com.guang.campuspicturebackend.tool.ImageIOTool;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author L.
 * @Date 2025/12/20 18:08
 * @Description 使用模板方法优化图片的上传
 * @Version 1.0
 */

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private MinioManager minioManager;

    @Resource
    private MinioConfig minioConfig;


    public UploadPictureResult uploadPicture(Object fileSource, String uploadPathPrefix) {
        // todo: 校验图片
        String fix = validateFile(fileSource);
        // 上传到minio
        // todo: 得到文件名
        String filename = getFileName(fileSource);
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if (fix != null) {
            extension = fix;
        }
        String uuid = UUID.randomUUID().toString();
        String objectName = String.format("%s/%s_%s.%s", uploadPathPrefix, DateUtil.formatDate(new Date()), uuid, extension);
        String type = getMimeType(extension);
        File tempFile = null;
        try {
            // todo: 创建零时文件
            tempFile = File.createTempFile("minio", filename);
            processPicture(fileSource, tempFile);
            // 获得图片的尺寸

            String absolutePath = tempFile.getAbsolutePath();

            String url = minioManager.upload(absolutePath, MinioConstant.BUCKET_NAME, objectName, type);

            return getUploadPictureResult(filename, url, tempFile, type);
        } catch (Exception e) {
            log.error("图片上传到服务器失败", e);
            throw new CustomException(ErrorCode.OPERATION_ERROR);
        } finally {
            deleteTempFile(tempFile);
        }

    }

    @NotNull
    private static UploadPictureResult getUploadPictureResult(String filename, String url, File tempFile, String type) {
        // 填充返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        Dimension dimOfPic = ImageIOTool.getDimOfPic(tempFile);
        if (dimOfPic == null) {
            // 如果为空设置一个默认的尺寸
            dimOfPic = new Dimension(-1, -1);
        }
        uploadPictureResult.setName(filename);
        double height = dimOfPic.getHeight();
        double width = dimOfPic.getWidth();
        double picScale = NumberUtil.round(width / height, 2).doubleValue();
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setUrl(url);
        uploadPictureResult.setPicHeight((int) height);
        uploadPictureResult.setPicWidth((int) width);
        uploadPictureResult.setPicSize(FileUtil.size(tempFile));
        uploadPictureResult.setPicFormat(type);
        // 获得图片主色调
        String color = ImageIOTool.getColorOfPic(tempFile);
        uploadPictureResult.setPicColor(color);
        return uploadPictureResult;
    }

    abstract void processPicture(Object fileSource, File tempFile) throws IOException;

    abstract String getFileName(Object fileSource);

    abstract String validateFile(Object fileSource);

    private void deleteTempFile(File tempFile) {
        if (tempFile == null) return;
        boolean delete = tempFile.delete();
        if (!delete) {
            log.error("file delete error, filepath = {}", tempFile.getAbsolutePath());
        }
    }

    /**
     * 根据文件名返回 MimeType
     * @param extension 扩展名
     * @return String
     */
    public String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
}
