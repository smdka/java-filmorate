package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Список пользователей отправлен");
        return users.values();
    }

    @PostMapping
    public ResponseEntity<User> add(@Valid @RequestBody User user) {
        useLoginIfNameIsBlank(user);
        user.setId(++id);
        users.put(user.getId(), user);
        return userAddedResponse(user);
    }

    private static ResponseEntity<User> userAddedResponse(User user) {
        log.info("Пользователь '{}' успешно создан и ему присвоен id = {}", user.getName(), user.getId());
        return ResponseEntity.ok(user);
    }

    private static void useLoginIfNameIsBlank(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            String login = user.getLogin();
            user.setName(login);
        }
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User newUser) {
        useLoginIfNameIsBlank(newUser);
        return users.replace(newUser.getId(), newUser) != null
                ? userUpdatedResponse(newUser)
                : userNotFoundResponse(newUser);
    }

    private static ResponseEntity<User> userUpdatedResponse(User newUser) {
        log.info("Пользователь с id = '{}' успешно обновлен", newUser.getId());
        return ResponseEntity.ok(newUser);
    }

    private static ResponseEntity<User> userNotFoundResponse(User newUser) {
        log.warn("Пользователь с id = '{}' не существует", newUser.getId());
        return new ResponseEntity<>(newUser, HttpStatus.NOT_FOUND);
    }
}
