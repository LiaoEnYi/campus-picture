package com.guang.campuspicturebackend.test;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Author L.
 * @Date 2025/12/25 16:18
 * @Description TODO
 * @Version 1.0
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedis() {
        stringRedisTemplate.opsForValue().set("test", "hello");
        String val = stringRedisTemplate.opsForValue().get("test");
        System.out.println("val = " + val);
    }
}
