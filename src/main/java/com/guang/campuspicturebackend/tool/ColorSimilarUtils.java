package com.guang.campuspicturebackend.tool;

import java.awt.*;

/**
 * @Author L.
 * @Date 2026/1/5 20:35
 * @Description 颜色相似度计算
 * @Version 1.0
 */
public class ColorSimilarUtils {
    public static double calculateSimilarity(Color color1, Color color2) {
        int color1Red = color1.getRed();
        int color1Blue = color1.getBlue();
        int color1Green = color1.getGreen();

        int color2Red = color1.getRed();
        int color2Blue = color2.getBlue();
        int color2Green = color2.getGreen();
        // 使用欧氏距离计算相似度
        double distance = Math.sqrt(Math.pow(color1Red - color2Red, 2) + Math.pow(color1Blue - color2Blue, 2) + Math.pow(color1Green - color2Green, 2));
        return 1 - distance / Math.sqrt(3 * Math.pow(255, 2));
    }

    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        Color color1 = Color.decode(hexColor1);
        Color color2 = Color.decode(hexColor2);
        return calculateSimilarity(color1, color2);
    }
}
