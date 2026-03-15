package com.hochbichler.userservice;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final List<User> USERS = List.of(
        new User("USR-001", "Alice", "alice@example.com", "ADMIN"),
        new User("USR-002", "Bob", "bob@example.com", "USER"),
        new User("USR-003", "Charlie", "charlie@example.com", "USER")
    );

    @GetMapping
    public List<User> getAllUsers() {
        return USERS;
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return USERS.stream()
            .filter(user -> user.id().equals(id))
            .findFirst();
    }
}
