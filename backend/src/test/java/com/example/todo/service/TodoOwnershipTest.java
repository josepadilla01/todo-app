package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(TodoService.class)
class TodoOwnershipTest {
    @Autowired
    private TodoService todoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void usersCannotReadUpdateOrDeleteEachOthersTodos() {
        Todo alice = todoService.create("alice", new TodoRequest("Alice's task", null));
        Todo bob = todoService.create("bob", new TodoRequest("Bob's task", null));

        assertEquals(alice.getId(), todoService.findAll("alice").get(0).getId());
        assertEquals(1, todoService.findAll("alice").size());
        assertEquals(bob.getId(), todoService.findAll("bob").get(0).getId());
        assertEquals(1, todoService.findAll("bob").size());
        assertTrue(todoService.update("bob", alice.getId(), new TodoRequest(null, true)).isEmpty());
        assertTrue(todoService.delete("bob", alice.getId()).isEmpty());
        assertFalse(todoService.findAll("alice").get(0).isDone());
        assertTrue(todoService.update("alice", alice.getId(), new TodoRequest(null, true)).orElseThrow().isDone());
        assertTrue(todoService.delete("alice", alice.getId()).isPresent());
        assertTrue(todoService.findAll("alice").isEmpty());
        assertEquals(1, todoService.findAll("bob").size());
    }

    @Test
    void existingUnownedRowsAreNotVisibleToAnyUser() {
        jdbcTemplate.update("insert into todo (text, done) values (?, ?)", "old task", false);

        assertTrue(todoService.findAll("alice").isEmpty());
        assertTrue(todoService.findAll("bob").isEmpty());
    }
}
