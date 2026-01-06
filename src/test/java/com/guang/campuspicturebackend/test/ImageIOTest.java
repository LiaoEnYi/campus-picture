package com.guang.campuspicturebackend.test;

import com.guang.campuspicturebackend.tool.ImageIOTool;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author L.
 * @Date 2026/1/4 17:28
 * @Description TODO
 * @Version 1.0
 */
public class ImageIOTest {
    @Test
    void testGetColor() throws Exception {
        Map<Integer, Integer> map = new HashMap<>();
        BufferedImage image = ImageIO.read(new File("D:\\04-coding\\testPic.png"));
        int height = image.getHeight();
        int width = image.getWidth();
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                int rgb = simplifyColor(image.getRGB(i, j));
                map.put(rgb, map.getOrDefault(rgb, 0) + 1);
            }
        }
        // 找出最多的key
        Map.Entry<Integer, Integer> entry = map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get();
        System.out.println("maxKey = " + entry.getKey());
        String color = String.format("#%06X", entry.getKey() & 0xFFFFFF);
        System.out.println("maxValue = " + entry.getValue());
        System.out.println("color = " + color);

    }

    @Test
    void testImageIOTool() {
        String colorOfPic = ImageIOTool.getColorOfPic(new File("D:\\04-coding\\testPic.png"));
        System.out.println("colorOfPic = " + colorOfPic);
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
