package com.guang.campuspicturebackend.manager.upload;

import com.guang.campuspicturebackend.exception.ErrorCode;
import com.guang.campuspicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @Author L.
 * @Date 2025/12/23 18:03
 * @Description 本地图片上传
 * @Version 1.0
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {

    @Override
    void processPicture(Object fileSource, File tempFile) throws IOException {
        MultipartFile multipartFile = (MultipartFile) fileSource;
        multipartFile.transferTo(tempFile);
    }

    @Override
    String getFileName(Object fileSource) {
        MultipartFile multipartFile = (MultipartFile) fileSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    String validateFile(Object fileSource) {
        MultipartFile multipartFile = (MultipartFile) fileSource;
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
        return null;
    }
}
