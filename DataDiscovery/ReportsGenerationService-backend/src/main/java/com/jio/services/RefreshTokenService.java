package com.jio.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

	private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

	public void storeRefreshToken(String token, String username) {
		refreshTokenStore.put(token, username);
	}

	public boolean isTokenValid(String token) {
		return refreshTokenStore.containsKey(token);
	}

	public String getUsernameFromToken(String token) {
		return refreshTokenStore.get(token);
	}

	public void deleteRefreshToken(String token) {
		refreshTokenStore.remove(token);
	}
}
