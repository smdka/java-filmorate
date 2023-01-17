package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private int id;
    private Set<Integer> friendIds = new HashSet<>();

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный email")
    private String email;

    @NotBlank(message = "Login обязателен")
    @Pattern(regexp = "^\\S*$")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть позже текущей")
    private LocalDate birthday;

    public void addFriend(int friendId) {
        friendIds.add(friendId);
    }

    public void deleteFriend(int friendId) {
        friendIds.remove(friendId);
    }

    public int getFriendsCount() {
        return friendIds.size();
    }

    public Set<Integer> getFriendIds() {
        return Collections.unmodifiableSet(friendIds);
    }
}
