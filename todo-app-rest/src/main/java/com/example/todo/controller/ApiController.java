package com.example.todo.controller;

import com.example.todo.model.ApiError;
import com.example.todo.model.Todo;
import com.example.todo.model.TodoRequest;
import com.example.todo.service.TodoService;
import java.util.List;
import java.security.Principal;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public final class ApiController {
    private final TodoService todoService;

    public ApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/auth")
    public ResponseEntity<AuthStatus> auth(Authentication authentication, CsrfToken csrfToken) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        String username = authenticated ? displayName(authentication) : null;
        AuthStatus status = new AuthStatus(
                authenticated,
                username,
                csrfToken.getToken(),
                csrfToken.getParameterName());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(status);
    }

    @GetMapping("/todos")
    public List<Todo> findAllTodos(Principal principal) {
        return todoService.findAll(principal.getName());
    }

    @PostMapping("/todos")
    public ResponseEntity<Todo> createTodo(
            @RequestBody(required = false) TodoRequest request,
            Principal principal) {
        Todo todo = todoService.create(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(todo);
    }

    @PutMapping("/todos/{id}")
    public ResponseEntity<?> updateTodo(
            @PathVariable long id,
            @RequestBody(required = false) TodoRequest request,
            Principal principal) {
        return todoService.update(principal.getName(), id, request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError("Todo not found.")));
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable long id, Principal principal) {
        return todoService.delete(principal.getName(), id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError("Todo not found.")));
    }

    private String displayName(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String name = oidcUser.getAttribute("name");
            if (name != null && !name.isBlank()) {
                return name;
            }
            String email = oidcUser.getAttribute("email");
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return authentication.getName();
    }

    public record AuthStatus(
            boolean authenticated,
            String username,
            String csrfToken,
            String csrfParameterName) {
    }
}