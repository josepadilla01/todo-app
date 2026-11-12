package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public final class TodoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoService.class);

    private final AtomicLong nextId = new AtomicLong(System.currentTimeMillis());
    private final List<Todo> todos = new ArrayList<>(List.of(
            new Todo(1, "Review project goals", false),
            new Todo(2, "Send follow-up email", true)
    ));

    public synchronized List<Todo> findAll() {
        return List.copyOf(todos);
    }

    public synchronized Todo create(TodoRequest request) {
        String text = request == null || request.text() == null ? "" : request.text().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Todo text is required.");
        }

        Todo todo = new Todo(nextId.incrementAndGet(), text, false);
        todos.add(todo);
        LOGGER.info("Todo created: id={}", todo.id());
        return todo;
    }

    public synchronized Optional<Todo> update(long id, TodoRequest request) {
        for (int index = 0; index < todos.size(); index++) {
            Todo current = todos.get(index);
            if (current.id() != id) {
                continue;
            }

            String text = current.text();
            if (request != null && request.text() != null && !request.text().trim().isEmpty()) {
                text = request.text().trim();
            }
            boolean done = request != null && request.done() != null ? request.done() : current.done();
            Todo updated = new Todo(id, text, done);
            todos.set(index, updated);
            LOGGER.info("Todo updated: id={}", id);
            return Optional.of(updated);
        }
        return Optional.empty();
    }

    public synchronized Optional<Todo> delete(long id) {
        for (int index = 0; index < todos.size(); index++) {
            Todo todo = todos.get(index);
            if (todo.id() == id) {
                todos.remove(index);
                LOGGER.info("Todo deleted: id={}", id);
                return Optional.of(todo);
            }
        }
        return Optional.empty();
    }
}