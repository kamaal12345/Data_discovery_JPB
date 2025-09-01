package com.jio.configuration;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jio.services.UserService;
import com.jio.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokeFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final UserService userService;
//	private final TokenBlacklistService tokenBlacklistService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String jwt = parseJwt(request);

			if (jwt != null) {
//				System.err.println("Token Checking");
//				if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
//					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//					response.getWriter().write("Token has been blacklisted (user is logged out)");
//					return;
//				}

				String username = jwtUtils.extractUsername(jwt);
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = userService.loadUserByUsername(username);
					if (jwtUtils.validateToken(jwt, userDetails)) {
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Cannot set user authentication: " + e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		if (headerAuth == null || headerAuth.isEmpty()) {
			return null;
		}
		if (headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7);
		}
		// No Bearer prefix, so treat whole header as token
		return headerAuth;
	}

}
