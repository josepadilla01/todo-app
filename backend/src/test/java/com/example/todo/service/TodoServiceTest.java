package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoRepository;
import com.example.todo.model.TodoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    void createsUpdatesAndDeletesTodo() {
        Todo created = new Todo("user-a", "Write tests", false);
        created.setId(1L);
        when(todoRepository.save(any(Todo.class))).thenReturn(created);
        when(todoRepository.findByIdAndOwnerId(1L, "user-a")).thenReturn(Optional.of(created));

        Todo result = todoService.create("user-a", new TodoRequest("  Write tests  ", null));
        assertEquals("Write tests", result.getText());
        assertFalse(result.isDone());

        Todo updated = todoService.update("user-a", 1L, new TodoRequest(null, true)).orElseThrow();
        assertTrue(updated.isDone());
        assertEquals(created, todoService.delete("user-a", 1L).orElseThrow());
        verify(todoRepository).delete(created);
    }

    @Test
    void rejectsBlankTodoTextAndMissingOwner() {
        assertThrows(IllegalArgumentException.class,
                () -> todoService.create("user-a", new TodoRequest("  ", null)));
        assertThrows(IllegalArgumentException.class, () -> todoService.findAll(""));
        assertThrows(IllegalArgumentException.class, () -> todoService.delete(null, 1L));
    }
}
