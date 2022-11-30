package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private int id;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный email")
    private String email;

    @NotBlank(message = "Login обязателен")
    @Pattern(regexp = "^\\S*$")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть позже текущей")
    private LocalDate birthday;

    public void updateFrom(User newUser) {
        this.email = newUser.getEmail();
        this.login = newUser.getLogin();
        this.name = newUser.getName();
        this.birthday = newUser.birthday;
    }
}