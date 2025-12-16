package com.guang.campuspicturebackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author L.
 * @Date 2025/12/16 15:23
 * @Description 测试接口
 * @Version 1.0
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/hello")
    public String test(){
        System.out.println(1 / 0);
        return "hello world";
    }
}
