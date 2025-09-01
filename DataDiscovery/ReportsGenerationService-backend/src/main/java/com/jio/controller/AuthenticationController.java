package com.jio.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jio.customexception.BusinessException;
import com.jio.dto.AuthenticationRequest;
import com.jio.dto.AuthenticationResponse;
import com.jio.dto.UserDetailsImpl;
import com.jio.entity.User;
import com.jio.repository.UserRepository;
import com.jio.services.UserService;
import com.jio.utils.JwtUtils;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

//	@Autowired
//	private TokenBlacklistService tokenBlacklistService;
//
//	@Autowired
//	private RedisService redisService;

//	@PostMapping(path = "/authenticate")
//	public ResponseEntity<?> userAuthentication(@RequestBody AuthenticationRequest authenticationRequest)
//			throws Exception, BusinessException {
//
//		try {
//			// Step 1: Authenticate user
//			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
//
//			// Step 2: Load user details
//			final UserDetailsImpl userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
//
//			// Step 3: Validate password (for extra layer)
//			if (!bCryptPasswordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
//				throw new Exception("Incorrect Password");
//			}
//
//			// Step 4: Check and update login status
//			User user = userRepository.findByUsername(authenticationRequest.getUsername())
//					.orElseThrow(() -> new Exception("User not found"));
//
//	        String previousToken = redisService.getUserToken(authenticationRequest.getUsername());
//
//	        // If token is null (first-time login), set previousToken to null explicitly
//	        if (previousToken == null) {
//	            previousToken = null;
//	        }
//	        
//	        if (Boolean.TRUE.equals(user.getLoggedIn()) && previousToken != null) {
//	            return ResponseEntity.status(409)
//	                    .body(Map.of("errorCode", "801", "message", "User is already logged in", "previousToken", previousToken));
//	        }
//
//			// Step 5: Generate token
//			final String jwt = jwtUtils.generateToken(userDetails);
//
//			// After generating JWT token (Step 5)
//			long expirationMillis = jwtUtils.getRemainingExpirationMillis(jwt);
//
//			redisService.saveUserToken(authenticationRequest.getUsername(), jwt, expirationMillis);
//
//			// Step 6: Only now mark user as logged in
//			user.setLoggedIn(true);
//			userRepository.save(user);
//
//			// Step 7: Return token
//			return ResponseEntity.ok(new AuthenticationResponse(jwt));
//
//		} catch (BadCredentialsException e) {
//			throw new Exception("Incorrect Username or Password", e);
//		}
//	}
//
//	@PostMapping("/logout")
//	public ResponseEntity<?> logout(HttpServletRequest request) throws Exception {
//		String token = request.getHeader("Authorization");
//
//		if (!StringUtils.hasText(token)) {
//			return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
//		}
//
//		// Step 1: Extract username from token
//		String username = jwtUtils.extractUsername(token);
//
//		// Step 2: Mark the user as logged out
//		userService.logOutUserByUsername(username);
//
//		// Step 3: Blacklist the token
//		long expiry = jwtUtils.getRemainingExpirationSeconds(token);
//		tokenBlacklistService.blacklistToken(token, expiry);
//
//		// Step 4: Return success response
//		return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
//	}
//
//
//	@PostMapping("/session-logout")
//	public ResponseEntity<?> logout(@RequestBody AuthenticationRequest request, HttpServletRequest httpRequest)
//			throws BusinessException {
//		String token = httpRequest.getHeader("Authorization");
//
//		if (!StringUtils.hasText(token)) {
//			throw new BusinessException("804", "Authorization token is missing");
//		}
//
//		String storedToken = redisService.getUserToken(request.getUsername());
//		if (storedToken == null || !storedToken.equals(token)) {
//			throw new BusinessException("805", "Token mismatch - cannot logout session");
//		}
//
//		User user = userRepository.findByUsername(request.getUsername())
//				.orElseThrow(() -> new BusinessException("802", "User not found"));
//
//		if (!user.getLoggedIn()) {
//			throw new BusinessException("803", "User is not logged in");
//		}
//
//		user.setLoggedIn(false);
//		userRepository.save(user);
//
//		long expiry = jwtUtils.getRemainingExpirationSeconds(token);
//		tokenBlacklistService.blacklistToken(token, expiry);
//
//		redisService.deleteUserToken(request.getUsername());
//
//		return ResponseEntity.ok(Map.of("message", "User logged out and token blacklisted"));
//	}

	// with out redius

	@PostMapping(path = "/authenticate")
	public ResponseEntity<?> userAuthentication(@RequestBody AuthenticationRequest authenticationRequest)
			throws Exception, BusinessException {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (BadCredentialsException e) {
			throw new Exception("Incorrect Username or Password");
		}
		final UserDetailsImpl userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());
		if (!bCryptPasswordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
			throw new Exception("Incorrect Password");
		}
		User user = userRepository.findUserByUsername(authenticationRequest.getUsername()).get();
		Boolean logIn = user.getLoggedIn();
		if (logIn == true) {
			throw new BusinessException("801", "User is already logged in");
		} else {
			user.setLoggedIn(true);
			userRepository.save(user);
		}

	    final String jwt = jwtUtils.generateToken(userDetails);
	    final String refreshToken = jwtUtils.generateRefreshToken(userDetails);
	    
	    AuthenticationResponse response = new AuthenticationResponse(jwt, refreshToken);

	    return ResponseEntity.ok(response);
	}
	
	@PostMapping("/refresh-token")
	public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
	    String refreshToken = request.get("refreshToken");

	    if (!jwtUtils.validateRefreshToken(refreshToken)) {
	        return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
	    }

	    String username = jwtUtils.extractUsername(refreshToken);
	    UserDetailsImpl userDetails = userService.loadUserByUsername(username);

	    String newAccessToken = jwtUtils.generateToken(userDetails);

	    return ResponseEntity.ok(new AuthenticationResponse(newAccessToken, refreshToken));
	}


	@PostMapping("/logout")
	public ResponseEntity<Map<String, Boolean>> logout(@RequestBody AuthenticationRequest authenticationRequest)
			throws Exception {
		String logOutUser = userService.logOutUser(authenticationRequest);

		Map<String, Boolean> response = new HashMap<>();
		response.put(logOutUser, Boolean.TRUE);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/session-logout")
	public ResponseEntity<Map<String, Boolean>> sessionLogout(@RequestBody AuthenticationRequest authenticationRequest)
			throws Exception {
		String logOutUser = userService.sessionLogOutUser(authenticationRequest);

		Map<String, Boolean> response = new HashMap<>();
		response.put(logOutUser, Boolean.TRUE);
		return ResponseEntity.ok(response);
	}
}