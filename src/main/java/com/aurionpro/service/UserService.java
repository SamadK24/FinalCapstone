package com.aurionpro.service;

import com.aurionpro.dtos.UserLoginDTO;
import com.aurionpro.entity.User;

public interface UserService {

    /**
     * Authenticate a user and return a JWT token.
     */
    String authenticateUser(UserLoginDTO dto);

    /**
     * Find a user by username or email.
     */
    User findByUsernameOrEmail(String usernameOrEmail);
}
