package com.guang.campuspicturebackend.manager;

import io.minio.GetObjectResponse;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author L.
 * @Date 2026/1/4 14:35
 * @Description minioTest
 * @Version 1.0
 */
@SpringBootTest
class MinioManagerTest {
    @Resource
    private MinioManager minioManager;

    @Test
    void testUpload() {
    }

    @Test
    void testDownload() {
        try {
            GetObjectResponse response = minioManager.download("picture", "public/2001208499207991298/2025-12-21_a801856d-25e7-41f0-ba87-8841e97182dc.png");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("D:\\04-coding\\a.png")));
//            response.transferTo(bos);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = response.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            response.close();
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDelete() {
        minioManager.deleteResource("picture", "public/2001208499207991298/2025-12-21_a801856d-25e7-41f0-ba87-8841e97182dc.png");
    }
}