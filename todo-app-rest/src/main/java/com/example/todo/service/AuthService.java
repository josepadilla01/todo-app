package com.example.todo.service;

import com.example.todo.model.LoginRequest;
import com.example.todo.model.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public final class AuthService {
    private final String username;
    private final String password;

    public AuthService(
            @Value("${TODO_APP_USERNAME}") String username,
            @Value("${TODO_APP_PASSWORD}") String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("TODO_APP_USERNAME and TODO_APP_PASSWORD must not be blank.");
        }
        this.username = username;
        this.password = password;
    }

    public LoginResponse login(LoginRequest request) {
        boolean valid = request != null
                && username.equals(request.username())
                && password.equals(request.password());
        if (valid) {
            return new LoginResponse(true, "Login successful.");
        }
        return new LoginResponse(false, "Invalid username or password.");
    }
}