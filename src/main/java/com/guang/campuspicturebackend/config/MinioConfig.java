package com.guang.campuspicturebackend.config;

import io.minio.MinioClient;
import lombok.Data;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author L.
 * @Date 2025/12/20 13:37
 * @Description 实例化Minio
 * @Version 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio.client")
public class MinioConfig {

    private String uri;
    private int port;
    private String accessKey;
    private String secretKey;
    private String domain;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(uri, port, false)
                .credentials(accessKey, secretKey)
                .build();
    }
}
