package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoServiceTest {
    private final TodoService todoService = new TodoService();

    @Test
    void createsUpdatesAndDeletesTodo() {
        Todo created = todoService.create(new TodoRequest("  Write tests  ", null));

        assertEquals("Write tests", created.text());
        assertFalse(created.done());

        Todo updated = todoService.update(created.id(), new TodoRequest(null, true)).orElseThrow();
        assertTrue(updated.done());

        assertEquals(created.id(), todoService.delete(created.id()).orElseThrow().id());
        assertTrue(todoService.findAll().stream().noneMatch(todo -> todo.id() == created.id()));
    }

    @Test
    void rejectsBlankTodoText() {
        assertThrows(IllegalArgumentException.class,
                () -> todoService.create(new TodoRequest("  ", null)));
    }
}