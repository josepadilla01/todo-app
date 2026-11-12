package com.example.todo.controller;

import com.example.todo.model.ApiError;
import com.example.todo.model.LoginRequest;
import com.example.todo.model.LoginResponse;
import com.example.todo.model.Todo;
import com.example.todo.model.TodoRequest;
import com.example.todo.service.AuthService;
import com.example.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public final class ApiController {
    private final AuthService authService;
    private final TodoService todoService;

    public ApiController(AuthService authService, TodoService todoService) {
        this.authService = authService;
        this.todoService = todoService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody(required = false) LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(response.success() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @GetMapping("/todos")
    public List<Todo> findAllTodos() {
        return todoService.findAll();
    }

    @PostMapping("/todos")
    public ResponseEntity<Todo> createTodo(@RequestBody(required = false) TodoRequest request) {
        Todo todo = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(todo);
    }

    @PutMapping("/todos/{id}")
    public ResponseEntity<?> updateTodo(
            @PathVariable long id,
            @RequestBody(required = false) TodoRequest request) {
        return todoService.update(id, request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError("Todo not found.")));
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable long id) {
        return todoService.delete(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError("Todo not found.")));
    }
}