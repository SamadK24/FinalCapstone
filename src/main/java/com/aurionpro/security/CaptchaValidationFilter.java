package com.aurionpro.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aurionpro.exceptions.InvalidCaptchaException;
import com.aurionpro.service.CaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CaptchaValidationFilter extends OncePerRequestFilter {

    private final CaptchaService captchaService;
    private final AntPathRequestMatcher loginRequestMatcher =
            new AntPathRequestMatcher("/api/auth/login", HttpMethod.POST.name());

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Modern way: Directly check method and path
        if ("POST".equalsIgnoreCase(request.getMethod()) &&
            request.getRequestURI().equals("/api/auth/login")) {
            // Your captcha logic here...
            // For testing: remove or bypass captcha logic
        }
        filterChain.doFilter(request, response);
    }

}
