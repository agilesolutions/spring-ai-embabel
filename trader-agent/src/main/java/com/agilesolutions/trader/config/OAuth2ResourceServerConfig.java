package com.agilesolutions.trader.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * OAuth2 Resource Server Configuration for Service B
 * 
 * Configures Trader Agent as an OAuth2 Resource Server that validates JWT tokens
 * issued by Keycloak. This protects the internal API endpoints from unauthorized access.
 * 
 * Configuration:
 * - JWT validation with issuer and audience checks
 * - Role-based access control via scope claims
 * - CORS support for service-to-service calls within cluster
 * - Standard HTTP security best practices
 * 
 * Token validation settings are defined in application.yaml:
 * spring:
 *   security:
 *     oauth2:
 *       resourceserver:
 *         jwt:
 *           issuer-uri: http://keycloak/realms/demo
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
public class OAuth2ResourceServerConfig {

    /**
     * Configure HTTP security for OAuth2 Resource Server
     * 
     * @param http HttpSecurity to configure
     * @return SecurityFilterChain
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("Configuring OAuth2 Resource Server security filter chain");

        http
                // CORS configuration for service-to-service communication
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Require authentication for all requests except health checks and actuator
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/health", "/health/live", "/health/ready").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                // OAuth2 Resource Server with JWT validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                        )
                )

                // Session management
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(10)
                                .expiredUrl("/error")
                        )
                )

                // Security headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "frame-ancestors 'none'; " +
                                "base-uri 'self'; " +
                                "form-action 'self'"
                        ))
                        .contentTypeOptions(contentTypeOptionsConfig -> {})
                        .xssProtection(xXssConfig -> {})
                        .cacheControl(cacheControlConfig -> {})
                )

                // Exception handling
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized access attempt: {}", authException.getMessage());
                            response.sendError(401, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Access denied: {}", accessDeniedException.getMessage());
                            response.sendError(403, "Forbidden");
                        })
                );

        return http.build();
    }

    /**
     * CORS configuration for service-to-service communication
     * 
     * @return CorsConfigurationSource for internal cluster communication
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from Service A within the cluster
        configuration.setAllowedOrigins(Arrays.asList(
                "http://trader:8080",
                "http://localhost"
        ));
        
        // Allow standard HTTP methods for internal API
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow necessary headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "X-Trace-Id",
                "X-Span-Id"
        ));
        
        // Expose headers for clients
        configuration.setExposedHeaders(Arrays.asList(
                "X-Response-Time",
                "X-Trace-Id"
        ));
        
        // Allow credentials (bearer tokens)
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

