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
        Todo created = todoService.create("user-a", new TodoRequest("  Write tests  ", null));

        assertEquals("Write tests", created.text());
        assertFalse(created.done());

        Todo updated = todoService.update("user-a", created.id(), new TodoRequest(null, true)).orElseThrow();
        assertTrue(updated.done());

        assertEquals(created.id(), todoService.delete("user-a", created.id()).orElseThrow().id());
        assertTrue(todoService.findAll("user-a").stream().noneMatch(todo -> todo.id() == created.id()));
    }

    @Test
    void rejectsBlankTodoText() {
        assertThrows(IllegalArgumentException.class,
                () -> todoService.create("user-a", new TodoRequest("  ", null)));
    }

    @Test
    void keepsTodosPrivateBetweenUsers() {
        Todo created = todoService.create("user-a", new TodoRequest("Private task", null));

        assertTrue(todoService.findAll("user-b").isEmpty());
        assertTrue(todoService.update("user-b", created.id(), new TodoRequest(null, true)).isEmpty());
        assertTrue(todoService.delete("user-b", created.id()).isEmpty());
        assertFalse(todoService.findAll("user-a").get(0).done());
    }
}