package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
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
    public User add(@Valid @RequestBody User user) {
        useLoginIfNameIsNull(user);
        user.setId(++id);
        users.put(user.getId(), user);
        log.info("Пользователь '{}' успешно создан и ему присвоен id = {}", user.getName(), user.getId());
        return user;
    }

    private void useLoginIfNameIsNull(User user) {
        if (user.getName() == null) {
            String login = user.getLogin();
            user.setName(login);
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) throws IllegalArgumentException {
        if (!isExist(newUser)) {
            log.warn("Не удалось обновить пользователя");
            throw new IllegalArgumentException("Пользователь с id = " + newUser.getId() + " не существует");
        }
        useLoginIfNameIsNull(newUser);
        User currentUser = users.get(newUser.getId());
        currentUser.updateFrom(newUser);
        log.info("Пользователь '{}' успешно обновлен", currentUser.getName());
        return currentUser;
    }

    private boolean isExist(User newUser) {
        return users.containsKey(newUser.getId());
    }
}
