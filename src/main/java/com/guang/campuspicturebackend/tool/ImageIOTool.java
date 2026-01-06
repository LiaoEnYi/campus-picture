package com.guang.campuspicturebackend.tool;

import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author L.
 * @Date 2025/12/21 9:37
 * @Description 图像工具类
 * @Version 1.0
 */
@Slf4j
public class ImageIOTool {
    public static Dimension getDimOfPic(File file) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            if (iis == null) {
                return null;
            }
            for (Iterator<ImageReader> it = ImageIO.getImageReaders(iis); it.hasNext(); ) {
                ImageReader r = it.next();
                try {
                    r.setInput(iis, true, true);
                    int width = r.getWidth(0);
                    int height = r.getHeight(0);
                    return new Dimension(width, height);
                } finally {
                    r.dispose();
                }
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.OPERATION_ERROR);
        }
        return null;
    }

    // 由于此方法比较耗时，所以异步执行
    public static String getColorOfPic(File file) {
        Map<Integer, Integer> map = new HashMap<>();
        try {
            BufferedImage image = ImageIO.read(file);
            int height = image.getHeight();
            int width = image.getWidth();
            int step = 3;
            for (int i = 0; i < width; i += step) {
                for (int j = 0; j < height; j += step) {
                    int rgb = simplifyColor(image.getRGB(i, j));
                    map.put(rgb, map.getOrDefault(rgb, 0) + 1);
                }
            }
            // 找出最多的key
            Integer key = map.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();
            return String.format("#%06X", key & 0xFFFFFF);
        } catch (IOException e) {
            log.error("获取图片主色调失败：{}", e.getMessage());
        }
        return null;
    }

    private static int simplifyColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        // 将颜色值压缩到32个等级
        r = (r / 32) * 32;
        g = (g / 32) * 32;
        b = (b / 32) * 32;
        return (r << 16) | (g << 8) | b;
    }
}
