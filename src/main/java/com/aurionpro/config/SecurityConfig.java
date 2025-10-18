package com.aurionpro.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import com.aurionpro.security.CaptchaValidationFilter;
import com.aurionpro.security.CustomUserDetailsService;
import com.aurionpro.security.JwtAuthenticationFilter;
import com.aurionpro.service.CaptchaService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CaptchaService captchaService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS before auth filters
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Allow preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/validate-reset-token").permitAll()

                // Payroll management (Bank Admin only)
                .requestMatchers("/api/bank-admin/payroll/batches/**").hasRole("BANK_ADMIN")
                .requestMatchers("/api/bank-admin/payroll/disbursements/**").hasRole("BANK_ADMIN")

                // Organization APIs
                .requestMatchers("/api/organization/**")
                    .hasAnyRole("ORGANIZATION_ADMIN", "BANK_ADMIN", "EMPLOYEE")

                // Employee account APIs
                .requestMatchers("/api/bank-accounts/employee/**").hasRole("EMPLOYEE")

                // Bank Admin area
                .requestMatchers("/api/bank-admin/**").hasRole("BANK_ADMIN")
                .requestMatchers("/api/bank-admin/salary-disbursal-requests/**").hasRole("BANK_ADMIN")

                // Shared Bank Accounts APIs
                .requestMatchers("/api/bank-accounts/**")
                    .hasAnyRole("ORGANIZATION_ADMIN", "EMPLOYEE", "BANK_ADMIN")

                // Everything else requires auth
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("http://localhost:4200"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public CaptchaValidationFilter captchaValidationFilter() {
        return new CaptchaValidationFilter(captchaService);
    }
}
