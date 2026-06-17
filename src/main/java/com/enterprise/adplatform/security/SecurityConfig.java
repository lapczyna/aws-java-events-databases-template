package com.enterprise.adplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security placeholder — prepares structure for JWT/OAuth2/Cognito integration.
 *
 * TODO: Replace permitAll() with JWT or OAuth2 resource server configuration.
 * Example future integration points:
 *   - AWS Cognito as OAuth2 identity provider
 *   - Spring Security OAuth2 Resource Server with JWT validation
 *   - Role-based access control (RBAC) per endpoint
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/actuator/info",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                // TODO: Replace with proper role-based auth when Cognito/JWT is wired
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
