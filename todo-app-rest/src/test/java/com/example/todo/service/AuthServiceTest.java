package com.example.todo.service;

import com.example.todo.model.LoginRequest;
import com.example.todo.model.LoginResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private final AuthService authService = new AuthService("test-user", "test-password");

    @Test
    void acceptsConfiguredCredentials() {
        LoginResponse response = authService.login(new LoginRequest("test-user", "test-password"));

        assertTrue(response.success());
    }

    @Test
    void rejectsInvalidCredentials() {
        LoginResponse response = authService.login(new LoginRequest("test-user", "wrong"));

        assertFalse(response.success());
    }

    @Test
    void rejectsBlankCredentialsAtStartup() {
        assertThrows(IllegalArgumentException.class, () -> new AuthService("", "test-password"));
        assertThrows(IllegalArgumentException.class, () -> new AuthService("test-user", " "));
    }
}