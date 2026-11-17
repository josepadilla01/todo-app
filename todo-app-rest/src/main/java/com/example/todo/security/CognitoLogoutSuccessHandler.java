package com.example.todo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

public final class CognitoLogoutSuccessHandler implements LogoutSuccessHandler {
    private final String cognitoDomain;
    private final String clientId;
    private final String logoutUri;

    public CognitoLogoutSuccessHandler(String cognitoDomain, String clientId, String logoutUri) {
        URI domain = URI.create(cognitoDomain);
        if (!"https".equalsIgnoreCase(domain.getScheme()) || domain.getHost() == null) {
            throw new IllegalArgumentException("COGNITO_DOMAIN must be an HTTPS URL.");
        }
        this.cognitoDomain = cognitoDomain;
        this.clientId = clientId;
        this.logoutUri = logoutUri;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String targetUrl = UriComponentsBuilder.fromUriString(cognitoDomain)
                .replacePath("/logout")
                .queryParam("client_id", clientId)
                .queryParam("logout_uri", "{logoutUri}")
                .encode(StandardCharsets.UTF_8)
                .buildAndExpand(logoutUri)
                .toUriString();
        response.sendRedirect(targetUrl);
    }
}
