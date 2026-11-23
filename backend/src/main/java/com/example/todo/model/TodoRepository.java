package com.example.todo.model;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByOwnerId(String ownerId);

    Optional<Todo> findByIdAndOwnerId(Long id, String ownerId);
}
