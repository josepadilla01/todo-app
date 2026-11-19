package com.example.todo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfiguration {
    @Bean
    public CognitoLogoutSuccessHandler cognitoLogoutSuccessHandler(
            @Value("${app.cognito.domain}") String cognitoDomain,
            @Value("${spring.security.oauth2.client.registration.cognito.client-id}") String clientId,
            @Value("${app.cognito.logout-uri}") String logoutUri) {
        return new CognitoLogoutSuccessHandler(cognitoDomain, clientId, logoutUri);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CognitoLogoutSuccessHandler cognitoLogoutSuccessHandler) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfTokenRequestHandler))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/index.html", "/assets/**", "/api/auth")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> request.getServletPath().startsWith("/api/")))
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(cognitoLogoutSuccessHandler));
        return http.build();
    }
}
