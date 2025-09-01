package com.jio.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.jio.services.TokenBlacklistService;
import com.jio.services.UserService;
import com.jio.utils.JwtUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtils jwtUtils, UserService userService,
			TokenBlacklistService tokenBlacklistService) throws Exception {

		return http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
					.requestMatchers("/api/authenticate", "/api/refresh-token", "/api/session-logout").permitAll()
					.anyRequest().authenticated())
				.addFilterBefore(authTokeFilter(jwtUtils, userService,tokenBlacklistService), UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public AuthTokeFilter authTokeFilter(JwtUtils jwtUtils, UserService userService,
			TokenBlacklistService tokenBlacklistService) {
//		return new AuthTokeFilter(jwtUtils, userService, tokenBlacklistService);
		return new AuthTokeFilter(jwtUtils, userService);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	AuthenticationProvider authenticationProvider() {
//		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//		provider.setUserDetailsService(userService);
//		provider.setPasswordEncoder(passwordEncoder());
//		return provider;
//	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*")); 
		configuration.setAllowedMethods(Arrays.asList("HEAD", "OPTIONS", "POST", "GET", "PUT", "DELETE", "PATCH"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-type", "x-auth-token"));
		configuration.setExposedHeaders(Arrays.asList("x-auth-token"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
