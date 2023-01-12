package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDate;
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

    public void addFriend(User user) {
        friendIds.add(user.getId());
    }

    public void deleteFriend(User user) {
        friendIds.remove(user.getId());
    }

    public int getFriendsCount() {
        return friendIds.size();
    }

    public Set<Integer> getFriendIds() {
        return new HashSet<>(friendIds);
    }
}
