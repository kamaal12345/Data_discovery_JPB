package com.jio.utils;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;


@Component
public class RedisHealthChecker {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void checkConnection() {
        try {
            redisTemplate.opsForValue().set("health_check", "OK", Duration.ofSeconds(5));
            String result = redisTemplate.opsForValue().get("health_check");
            System.out.println("✅ Redis is connected: " + result);
        } catch (Exception e) {
            System.err.println("❌ Redis connection failed: " + e.getMessage());
        }
    }
}
