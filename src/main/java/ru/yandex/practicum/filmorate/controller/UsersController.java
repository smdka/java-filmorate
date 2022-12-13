package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Получен запрос GET /users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        log.debug("Получен запрос GET /users/" + id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.debug("Получен запрос GET /users/" + id + "/friends");
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.debug("Получен запрос GET /users/" + id + "/friends/common/" + otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User add(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.debug("Получен запрос POST /users/");
        checkForErrors(bindingResult);
        return userService.add(user);
    }

    private static void checkForErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (FieldError e : bindingResult.getFieldErrors()) {
                log.warn("Не пройдена валидация пользователя: " + e.getField() + " = " + e.getRejectedValue());
            }
            throw new ValidationException(bindingResult.getFieldErrors().toString());
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /users/");
        checkForErrors(bindingResult);
        return userService.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public Map<User, User> addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.debug("Получен запрос PUT /users/" + id + "/friends/" + friendId);
        return userService.addFriendsToEachOther(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.debug("Получен запрос DELETE /users/" + id + "/friends/" + friendId);
        return userService.deleteFriend(id, friendId);
    }
}
