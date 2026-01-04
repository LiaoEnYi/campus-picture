package com.guang.campuspicturebackend.manager;

import com.guang.campuspicturebackend.config.MinioConfig;
import com.guang.campuspicturebackend.constant.MinioConstant;
import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import io.minio.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @Author L.
 * @Date 2025/12/20 13:46
 * @Description Minio 通用部分
 * @Version 1.0
 */
@Slf4j
@Component
public class MinioManager {
    @Resource
    private MinioClient minioClient;
    @Resource
    private MinioConfig minioConfig;

    /**
     * 上传文件到minio
     *
     * @param filePath   文件本地路径
     * @param bucket     桶名称
     * @param objectName 上传后的对象名
     * @param mimeType   数据类型
     * @return bool
     */
    public String upload(String filePath, String bucket, String objectName, String mimeType) {
        try {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .filename(filePath)
                            .contentType(mimeType)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("upload to minio fail: {}", e.getMessage());
            throw new CustomException(ErrorCode.SYSTEM_ERROR);
        }
        // 上传成功返回访问路径
        String uri = minioConfig.getUri();
        int port = minioConfig.getPort();
        String accessKey = minioConfig.getAccessKey();
        String secretKey = minioConfig.getSecretKey();
        // 域名
        String domain = minioConfig.getDomain();
        return String.format("%s:%s/%s/%s", domain, port, MinioConstant.BUCKET_NAME, objectName);
    }

    public GetObjectResponse download(String bucket, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("download resource form minio fail: {}", e.getMessage());
            throw new CustomException(ErrorCode.SYSTEM_ERROR);
        }
    }

    public void deleteResource(String bucket, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("delete resource fail:{}", e.getMessage());
            throw new CustomException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
