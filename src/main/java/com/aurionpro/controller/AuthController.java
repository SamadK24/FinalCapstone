package com.aurionpro.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ApiResponseDTO;
import com.aurionpro.dtos.JwtResponseDTO;
import com.aurionpro.dtos.OrganizationRegistrationDTO;
import com.aurionpro.dtos.UserLoginDTO;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.User;
import com.aurionpro.service.CaptchaService;
import com.aurionpro.service.OrganizationService;
import com.aurionpro.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final OrganizationService organizationService;
    private final UserService userService; // For login
    private final ModelMapper modelMapper;
    private final CaptchaService captchaService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> registerOrganization(@Valid @RequestBody OrganizationRegistrationDTO registrationDTO) {
        organizationService.registerOrganization(registrationDTO);
        return ResponseEntity.ok(new ApiResponseDTO(true, "Organization registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        String jwt = userService.authenticateUser(loginDTO);

//        boolean captchaValid = captchaService.verifyCaptcha(loginDTO.getCaptchaToken());
//        if (!captchaValid) {
//            throw new RuntimeException("Captcha verification failed");
//        }
//        
        
        User user = userService.findByUsernameOrEmail(loginDTO.getUsernameOrEmail());

        JwtResponseDTO response = JwtResponseDTO.builder()
                .token(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .expiresIn(86400L)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsernameOrEmail(userDetails.getUsername());
        Organization org = organizationService.getOrganizationForAdmin(user.getId());

        Map<String, Object> out = new HashMap<>();
        out.put("userId", user.getId());
        out.put("username", user.getUsername());
        out.put("email", user.getEmail());
        out.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        if (org != null) {
            out.put("organizationId", org.getId());
            out.put("organizationName", org.getName());
            out.put("organizationStatus", org.getStatus().name());  // Add status here as string
        }
        return ResponseEntity.ok(out);
    }

}
