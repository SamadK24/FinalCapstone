package com.aurionpro.config;

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

import com.aurionpro.security.CaptchaValidationFilter;
import com.aurionpro.security.CustomUserDetailsService;
import com.aurionpro.security.JwtAuthenticationFilter;
import com.aurionpro.service.CaptchaService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true) // ensure @PreAuthorize is enforced
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
        http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authz -> authz
              // Public auth
              .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

              // Bank admin area: review/kyc/execute only
              .requestMatchers("/api/bank-admin/**").hasRole("BANK_ADMIN")

              // Organization-scoped APIs (no BANK_ADMIN by default)
              // If needed, you can further narrow critical mutating endpoints to ORG_ADMIN only:
              // .requestMatchers(HttpMethod.POST, "/api/organization/**").hasRole("ORGANIZATION_ADMIN")
              // .requestMatchers(HttpMethod.PUT,  "/api/organization/**").hasRole("ORGANIZATION_ADMIN")
              // .requestMatchers(HttpMethod.DELETE,"/api/organization/**").hasRole("ORGANIZATION_ADMIN")
              .requestMatchers("/api/organization/**").hasAnyRole("ORGANIZATION_ADMIN", "EMPLOYEE")

              // Bank accounts (org-owned and employee-owned)
              .requestMatchers("/api/bank-accounts/**").hasAnyRole("ORGANIZATION_ADMIN", "EMPLOYEE")

              // Everything else requires auth
              .anyRequest().authenticated()
          )
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public CaptchaValidationFilter captchaValidationFilter() {
        return new CaptchaValidationFilter(captchaService);
    }
}
