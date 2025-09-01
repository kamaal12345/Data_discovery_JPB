package com.jio.utils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.jio.dto.UserDetailsImpl;
import com.jio.entity.Roles;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;

@Service
public class JwtUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60 * 1000; // 1 minute = 60 * 1000 ms
	public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

	@Value("${application.security.jwt.secret-key}")
	private String secretKey;

	@Value("${application.security.jwt.refresh-secret-key}")
	private String refreshSecretKey;

	static final String CLAIM_KEY_ID = "userId";
	static final String CLAIM_KEY_USER_NAME = "userName";
	static final String CLAIM_FIRST_NAME = "firstName";
	static final String CLAIM_LAST_NAME = "lastName";
	static final String CLAIM_ROLE_VALUE = "roleValue";
	static final String CLAIM_ROLE_NAME = "roleName";
	static final String CLAIM_KEY_CREATED = "createdOn";
	static final String CLAIM_KEY_EXPIRIED = "expiriedOn";
	static final String CLAIM_USER_GENDER = "gender";
	static final String CLAIM_USER_DESIGNATION = "designation";

	private SecretKey getRefreshKey() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}

	private SecretKey getKey() {
		try {
			// Use SHA-256 to hash the secret and ensure it's 32 bytes
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}

	public String generateRefreshToken(UserDetailsImpl userDetails) {
	    long now = System.currentTimeMillis();
	    Date issuedAt = new Date(now);
	    Date expiration = new Date(now + REFRESH_TOKEN_VALIDITY);

	    return Jwts.builder()
	            .setSubject(userDetails.getUsername())
	            .setIssuedAt(issuedAt)
	            .setExpiration(expiration)
	            .claim("type", "refresh")
	            .signWith(getRefreshKey())
	            .compact();
	}

	public boolean validateRefreshToken(String token) {
	    try {
	        Jwts.parser().verifyWith(getRefreshKey()).build().parseSignedClaims(token);
	        return true;
	    } catch (JwtException | IllegalArgumentException e) {
	        System.out.println("Invalid refresh token: " + e.getMessage());
	    }
	    return false;
	}

	@PostConstruct
	public void initSecretKey() {
		if (secretKey == null || secretKey.isEmpty()) {
			try {
				KeyGenerator keyGen = KeyGenerator.getInstance("HmacSha256");
				SecretKey skey = keyGen.generateKey();
				secretKey = Base64.getEncoder().encodeToString(skey.getEncoded());
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("Unable to generate secret key", e);
			}
		}
	}

	// Generate JWT token with 1-minute validity
//	public String generateToken(UserDetailsImpl userDetails) {
//		Map<String, Object> claims = new HashMap<>();
//		claims.put(CLAIM_KEY_ID, userDetails.getUserId());
//		claims.put(CLAIM_KEY_USER_NAME, userDetails.getUsername());
//		claims.put(CLAIM_KEY_CREATED, new Date());
//		claims.put(CLAIM_KEY_EXPIRIED, new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY));
//		claims.put(CLAIM_FIRST_NAME, userDetails.getFirstName());
//		claims.put(CLAIM_LAST_NAME, userDetails.getLastName());
//		claims.put(CLAIM_USER_GENDER, userDetails.getGender());
//		claims.put(CLAIM_USER_DESIGNATION, userDetails.getDesignation());
//		Set<Roles> roles = userDetails.getRoles();
//
//		if (roles != null && !roles.isEmpty()) {
//			List<Integer> roleValues = roles.stream().map(Roles::getRoleValue).collect(Collectors.toList());
//
//			List<String> roleNames = roles.stream().map(Roles::getRoleName).collect(Collectors.toList());
//
//			claims.put(CLAIM_ROLE_VALUE, roleValues);
//			claims.put(CLAIM_ROLE_NAME, roleNames);
//		}
//
//		return Jwts.builder().subject(userDetails.getUsername()).issuedAt(new Date())
//				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY)).claims(claims).signWith(getKey())
//				.compact();
//	}

	public String generateToken(UserDetailsImpl userDetails) {
		long now = System.currentTimeMillis();
		Date issuedAt = new Date(now);
		Date expiration = new Date(now + JWT_TOKEN_VALIDITY);

		Map<String, Object> customClaims = new HashMap<>();
		customClaims.put(CLAIM_KEY_ID, userDetails.getUserId());
		customClaims.put(CLAIM_KEY_USER_NAME, userDetails.getUsername());
		customClaims.put(CLAIM_KEY_CREATED, issuedAt);
		customClaims.put(CLAIM_KEY_EXPIRIED, expiration);
		customClaims.put(CLAIM_FIRST_NAME, userDetails.getFirstName());
		customClaims.put(CLAIM_LAST_NAME, userDetails.getLastName());
		customClaims.put(CLAIM_USER_GENDER, userDetails.getGender());
		customClaims.put(CLAIM_USER_DESIGNATION, userDetails.getDesignation());

		Set<Roles> roles = userDetails.getRoles();
		if (roles != null && !roles.isEmpty()) {
			customClaims.put(CLAIM_ROLE_VALUE, roles.stream().map(Roles::getRoleValue).collect(Collectors.toList()));
			customClaims.put(CLAIM_ROLE_NAME, roles.stream().map(Roles::getRoleName).collect(Collectors.toList()));
		}

		return Jwts.builder().claims().subject(userDetails.getUsername()).issuedAt(issuedAt).expiration(expiration)
				.add(customClaims).and().signWith(getKey()).compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	public String extractClaim(String token, String claimName) {
	    Claims claims = extractAllClaims(token);
	    Object value = claims.get(claimName);
	    return value != null ? value.toString() : null;
	}


	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public boolean validateJwtToken(String token) {
		try {
			Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
			return true;
		} catch (SecurityException e) {
			System.out.println("Invalid JWT signature: " + e.getMessage());
		} catch (MalformedJwtException e) {
			System.out.println("Invalid JWT token: " + e.getMessage());
		} catch (ExpiredJwtException e) {
			System.out.println("JWT token is expired: " + e.getMessage());
		} catch (UnsupportedJwtException e) {
			System.out.println("JWT token is unsupported: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println("JWT claims string is empty: " + e.getMessage());
		} catch (JwtException e) {
			System.out.println("JWT processing failed: " + e.getMessage());
		}
		return false;
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

//	public long getRemainingExpiration(String token) {
//		Date expiration = extractExpiration(token);
//		long now = System.currentTimeMillis();
//		return Math.max((expiration.getTime() - now) / 1000, 0);
//	}

	public long getRemainingExpirationSeconds(String token) {
		Date expiration = extractExpiration(token);
		long now = System.currentTimeMillis();
		return Math.max((expiration.getTime() - now) / 1000, 0);
	}

	public long getRemainingExpirationMillis(String token) {
		return getRemainingExpirationSeconds(token) * 1000;
	}

}
