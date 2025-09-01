package com.jio.services;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

	private final RedisTemplate<String, String> redisTemplate;

	public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void blacklistToken(String token, long expirySeconds) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.set(token, "BLACKLISTED", Duration.ofSeconds(expirySeconds));
	}

	public boolean isTokenBlacklisted(String token) {
		System.err.println("token black listed");
		return redisTemplate.hasKey(token);
	}
}
