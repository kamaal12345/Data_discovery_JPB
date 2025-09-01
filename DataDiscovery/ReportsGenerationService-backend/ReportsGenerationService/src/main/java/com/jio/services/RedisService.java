package com.jio.services;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	public String getUserToken(String username) {
		return redisTemplate.opsForValue().get("user:" + username + ":token");
	}

	public void saveUserToken(String username, String token, long expirationMillis) {
		redisTemplate.opsForValue().set("user:" + username + ":token", token, Duration.ofMillis(expirationMillis));
	}

	public void deleteUserToken(String username) {
		redisTemplate.delete("user:" + username + ":token");
	}
}
