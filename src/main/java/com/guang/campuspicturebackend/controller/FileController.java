package com.guang.campuspicturebackend.controller;

import com.guang.campuspicturebackend.common.BaseResponse;
import com.guang.campuspicturebackend.constant.MinioConstant;
import com.guang.campuspicturebackend.manager.MinioManager;
import io.minio.GetObjectResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @Author L.
 * @Date 2025/12/20 16:48
 * @Description 文件操作controller
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    private MinioManager minioManager;

    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 创建临时目录
        String filename = multipartFile.getOriginalFilename();

        String filepath = String.format("%s/%s", "test", filename);
        File file = null;
        String accessPath = null;
        try {
            file = File.createTempFile("minio", filename);
            multipartFile.transferTo(file);
            String absolutePath = file.getAbsolutePath();
            filename = MinioConstant.PUBLIC_PREFIX + "/" + filename;
            accessPath = minioManager.upload(absolutePath, MinioConstant.BUCKET_NAME, filename, MediaType.IMAGE_PNG_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return BaseResponse.success(accessPath);
    }

    @PostMapping("/download")
    public void downloadFile(String filename, HttpServletResponse response) {
        try (
                GetObjectResponse res = minioManager.download(MinioConstant.BUCKET_NAME, filename);
                ServletOutputStream out = response.getOutputStream()
        ) {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            out.write(res.readAllBytes());
            out.flush();
        } catch (Exception e) {
            log.error("file download failure");
        }
    }
}
