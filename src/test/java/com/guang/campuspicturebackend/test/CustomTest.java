package com.guang.campuspicturebackend.test;

import com.guang.campuspicturebackend.tool.ImageIOTool;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @Author L.
 * @Date 2025/12/20 18:38
 * @Description TODO
 * @Version 1.0
 */
public class CustomTest {

    @Test
    void getExtension() {
        String filename = "a.png";
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        System.out.println(extension);
    }

    @Test
    void testImageIO() {
        try {
            BufferedImage read = ImageIO.read(new File("C:\\Users\\Ocean\\Desktop\\a.png"));
            int width = read.getWidth();
            int height = read.getHeight();
            System.out.println("width = " + width);
            System.out.println("height = " + height);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestTool() {
        Dimension dimOfPic = ImageIOTool.getDimOfPic(new File("C:\\Users\\Ocean\\Desktop\\a.png"));
        double height = dimOfPic.getHeight();
        double width = dimOfPic.getWidth();
        System.out.println("width = " + width);
        System.out.println("height = " + height);
    }


    @Test
    void subString() {
        String url = "localhost:9000/picture/public/2001208499207991298/2025-12-21_a801856d-25e7-41f0-ba87-8841e97182dc.png";
        String objectName = url.substring(url.indexOf('/') + 1);
        System.out.println(objectName);
    }
}
