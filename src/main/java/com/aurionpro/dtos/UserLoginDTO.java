package com.aurionpro.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDTO {

    @NotBlank(message = "Username or Email is mandatory")
    @Size(min = 4, max = 50, message = "Username or Email must be between 4 and 50 characters")
    @Pattern(
        regexp = "^[^\\s]+$", 
        message = "Username or Email must not contain spaces"
    )
    private String usernameOrEmail;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
//    @Pattern(
//        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
//        message = "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
//    )
//    @Pattern(
//        regexp = "^[^\\s]+$", 
//        message = "Password must not contain spaces"
//    )
    private String password;

    @NotBlank(message = "CAPTCHA token is mandatory")
    @Size(max = 500, message = "CAPTCHA token is too long")
    private String captchaToken;
}

