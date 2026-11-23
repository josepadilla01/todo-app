package com.example.todo.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CognitoLogoutSuccessHandlerTest {
    @Test
    void redirectsToCognitoLogoutWithClientAndAllowedReturnUrl() throws Exception {
        CognitoLogoutSuccessHandler handler = new CognitoLogoutSuccessHandler(
                "https://example.auth.us-east-1.amazoncognito.com",
                "client-id",
                "https://app.example.com/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onLogoutSuccess(new MockHttpServletRequest(), response, null);

        assertEquals(
                "https://example.auth.us-east-1.amazoncognito.com/logout"
                        + "?client_id=client-id&logout_uri=https%3A%2F%2Fapp.example.com%2F",
                response.getRedirectedUrl());
    }

    @Test
    void rejectsNonHttpsCognitoDomain() {
        assertThrows(IllegalArgumentException.class, () -> new CognitoLogoutSuccessHandler(
                "http://example.auth.us-east-1.amazoncognito.com",
                "client-id",
                "http://localhost:5000/"));
    }
}
