package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoRepository;
import com.example.todo.model.TodoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public final class TodoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoService.class);

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> findAll(String userId) {
        requireUserId(userId);
        return todoRepository.findAllByOwnerId(userId);
    }

    public Todo create(String userId, TodoRequest request) {
        requireUserId(userId);
        String text = request == null || request.text() == null ? "" : request.text().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Todo text is required.");
        }

        Todo todo = new Todo(userId, text, false);
        Todo saved = todoRepository.save(todo);
        LOGGER.info("Todo created: id={}", saved.getId());
        return saved;
    }

    public Optional<Todo> update(String userId, long id, TodoRequest request) {
        requireUserId(userId);
        return todoRepository.findByIdAndOwnerId(id, userId).map(current -> {
            String text = current.getText();
            if (request != null && request.text() != null && !request.text().trim().isEmpty()) {
                text = request.text().trim();
            }
            boolean done = request != null && request.done() != null ? request.done() : current.isDone();

            current.setText(text);
            current.setDone(done);
            Todo updated = todoRepository.save(current);
            LOGGER.info("Todo updated: id={}", id);
            return updated;
        });
    }

    public Optional<Todo> delete(String userId, long id) {
        requireUserId(userId);
        return todoRepository.findByIdAndOwnerId(id, userId).map(todo -> {
            todoRepository.delete(todo);
            LOGGER.info("Todo deleted: id={}", id);
            return todo;
        });
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
    }
}