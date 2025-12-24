package com.guang.campuspicturebackend.tool;

import com.guang.campuspicturebackend.exception.CustomException;
import com.guang.campuspicturebackend.exception.ErrorCode;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * @Author L.
 * @Date 2025/12/21 9:37
 * @Description 图像工具类
 * @Version 1.0
 */
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
}
