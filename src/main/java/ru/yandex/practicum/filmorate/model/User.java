package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    private final Set<Integer> friends = new HashSet<>();

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный email")
    private String email;

    @NotBlank(message = "Login обязателен")
    @Pattern(regexp = "^\\S*$")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть позже текущей")
    private LocalDate birthday;

    public void addFriend(User user) {
        friends.add(user.getId());
    }

    public void deleteFriend(User user) {
        friends.remove(user.getId());
    }

    public int getFriendsCount() {
        return friends.size();
    }
}
