package com.guang.campuspicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @Author L.
 * @Date 2025/12/23 18:09
 * @Description URL 网络图片上传
 * @Version 1.0
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    @Override
    void processPicture(Object fileSource, File tempFile) throws IOException {
        String fileUrl = (String) fileSource;
        HttpUtil.downloadFile(fileUrl, tempFile);
    }

    @Override
    String getFileName(Object fileSource) {
        String fileUrl = (String) fileSource;
        return FileUtil.mainName(fileUrl) + "." + fileUrl.substring(fileUrl.lastIndexOf(".") + 1);
    }

    @Override
    String validateFile(Object fileSource) {
        String contentType = null;
        String fileUrl = (String) fileSource;
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
                return null;
            }
            // 文件类型校验
            contentType = response.header("Content-Type");
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
        if (contentType != null) {
            contentType = contentType.substring(contentType.lastIndexOf("/") + 1);
        }
        return contentType;
    }
}
