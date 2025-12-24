package com.guang.campuspicturebackend.manager;

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
import com.guang.campuspicturebackend.model.vo.UploadPictureResult;
import com.guang.campuspicturebackend.tool.ImageIOTool;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Author L.
 * @Date 2025/12/20 18:08
 * @Description 业务相关的文件管理
 * @Version 1.0
 */
@Service
@Slf4j
@Deprecated
public class FileManager {
    @Resource
    private MinioManager minioManager;

    @Resource
    private MinioConfig minioConfig;

    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validateFile(multipartFile);
        // 上传到minio
        String filename = multipartFile.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        String uuid = UUID.randomUUID().toString();
        String objectName = String.format("%s/%s_%s.%s", uploadPathPrefix, DateUtil.formatDate(new Date()), uuid, extension);
        String type = getMimeType(extension);
        File tempFile = null;
        try {
            // 创建零时文件
            tempFile = File.createTempFile("minio", filename);
            multipartFile.transferTo(tempFile);
            // 获得图片的尺寸
            Dimension dimOfPic = ImageIOTool.getDimOfPic(tempFile);
            if (dimOfPic == null) {
                // 如果为空设置一个默认的尺寸
                dimOfPic = new Dimension(-1, -1);
            }
            String absolutePath = tempFile.getAbsolutePath();
            String url = minioManager.upload(absolutePath, MinioConstant.BUCKET_NAME, objectName, type);
            // 填充返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
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
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到服务器失败", e);
            throw new CustomException(ErrorCode.OPERATION_ERROR);
        } finally {
            deleteTempFile(tempFile);
        }

    }

    private void deleteTempFile(File tempFile) {
        if (tempFile == null) return;
        boolean delete = tempFile.delete();
        if (!delete) {
            log.error("file delete error, filepath = {}", tempFile.getAbsolutePath());
        }
    }

    private void validateFile(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long size = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(size > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "目前只支持上传2M及以内的图片");
        // 校验文件类型
        String filename = multipartFile.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        final List<String> ALLOW_FORMAT_LIST = List.of("jpg", "png", "jpeg", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(extension), ErrorCode.PARAMS_ERROR, "目前还不支持这种格式");
    }


    public UploadPictureResult uploadPictureWithUrl(String fileUrl, String uploadPathPrefix) {
        // todo: 校验图片
        validateFile(fileUrl);
        // 上传到minio
        String filename = FileNameUtil.mainName(fileUrl);
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        String uuid = UUID.randomUUID().toString();
        String objectName = String.format("%s/%s_%s.%s", uploadPathPrefix, DateUtil.formatDate(new Date()), uuid, extension);
        String type = getMimeType(extension);
        File tempFile = null;
        try {
            // todo: 创建零时文件
            tempFile = File.createTempFile("minio", filename);
//            multipartFile.transferTo(tempFile);
            HttpUtil.downloadFile(fileUrl, tempFile);
            // 获得图片的尺寸
            Dimension dimOfPic = ImageIOTool.getDimOfPic(tempFile);
            if (dimOfPic == null) {
                // 如果为空设置一个默认的尺寸
                dimOfPic = new Dimension(-1, -1);
            }
            String absolutePath = tempFile.getAbsolutePath();
            String url = minioManager.upload(absolutePath, MinioConstant.BUCKET_NAME, objectName, type);
            // 填充返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
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
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到服务器失败", e);
            throw new CustomException(ErrorCode.OPERATION_ERROR);
        } finally {
            deleteTempFile(tempFile);
        }

    }

    private void validateFile(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // 校验URL是否有效
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 校验协议
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            throw new CustomException(ErrorCode.PARAMS_ERROR, "目前只支持 http 以及 https 协议");
        }
        // 发送HEAD请求查看目标是否有效
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                // 这里不抛出异常是因为可能有些服务器并不提供HEAD请求的方式，但是提供HEAD请求不代表没有这个资源
                return;
            }
            // 文件类型校验
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_FORMAT_LIST = List.of("image/jpg", "image/png", "image/jpeg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType.toLowerCase()), ErrorCode.PARAMS_ERROR, "目前还不支持这类格式的图片");
            }
            // 文件大小校验
            String length = response.header("Content-Length");
            if (StrUtil.isNotBlank(length)) {
                try {
                    long contentLength = Long.parseLong(length);
                    final long ONE_M = 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "目前只支持上传2M及以内的图片");
                } catch (NumberFormatException e) {
                    throw new CustomException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }

            }
        } finally {
            // 释放资源
            if (response != null) {
                response.close();
            }
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
