package com.guang.campuspicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Ocean
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.guang.campuspicturebackend.mapper")
@SpringBootApplication
public class CampusPictureBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusPictureBackendApplication.class, args);
    }

}
