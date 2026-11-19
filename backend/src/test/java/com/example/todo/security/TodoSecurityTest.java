package com.example.todo.security;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoRequest;
import com.example.todo.service.TodoService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:security-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.security.oauth2.client.registration.cognito.client-id=test-client",
        "app.cognito.domain=https://example.auth.us-east-1.amazoncognito.com",
        "app.cognito.logout-uri=http://localhost:5000/"
})
@AutoConfigureMockMvc
@Import(TodoSecurityTest.OAuthClientConfiguration.class)
@Transactional
class TodoSecurityTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private TodoService todoService;

    @Test
    void requiresLoginAndCsrfForTodoRequests() throws Exception {
        mvc.perform(get("/api/todos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/assets/test-asset.js")).andExpect(status().isOk());
        mvc.perform(post("/api/todos")
                .with(oidcLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"task\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyOwnerCanReadUpdateOrDeleteTodo() throws Exception {
        Todo alice = todoService.create("alice", new TodoRequest("private", null));
        Cookie token = csrfCookie("bob");

        mvc.perform(get("/api/todos").with(oidcLogin().idToken(idToken -> idToken.subject("bob"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put("/api/todos/{id}", alice.getId())
                .with(oidcLogin().idToken(idToken -> idToken.subject("bob")))
                .cookie(token)
                .header("X-XSRF-TOKEN", token.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"done\":true}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/todos/{id}", alice.getId())
                .with(oidcLogin().idToken(idToken -> idToken.subject("bob")))
                .cookie(token)
                .header("X-XSRF-TOKEN", token.getValue()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/todos").with(oidcLogin().idToken(idToken -> idToken.subject("alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("private"))
                .andExpect(jsonPath("$[0].done").value(false));
    }

    @Test
    void browserCanUseAuthEndpointCsrfCookieToCreateOwnedTodo() throws Exception {
        Cookie token = csrfCookie("alice");

        mvc.perform(post("/api/todos")
                .with(oidcLogin().idToken(idToken -> idToken.subject("alice")))
                .cookie(token)
                .header("X-XSRF-TOKEN", token.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"created in browser\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("created in browser"))
                .andExpect(jsonPath("$.ownerId").doesNotExist());
        mvc.perform(get("/api/todos").with(oidcLogin().idToken(idToken -> idToken.subject("bob"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Cookie csrfCookie(String userId) throws Exception {
        MvcResult auth = mvc.perform(get("/api/auth")
                .with(oidcLogin().idToken(token -> token.subject(userId))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie token = auth.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(token);
        return token;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class OAuthClientConfiguration {
        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration.withRegistrationId("cognito")
                    .clientId("test-client")
                    .clientSecret("test-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid")
                    .authorizationUri("https://example.com/oauth2/authorize")
                    .tokenUri("https://example.com/oauth2/token")
                    .jwkSetUri("https://example.com/.well-known/jwks.json")
                    .build();
            return new InMemoryClientRegistrationRepository(registration);
        }
    }
}
