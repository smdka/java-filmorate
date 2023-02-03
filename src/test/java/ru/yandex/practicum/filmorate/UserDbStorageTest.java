package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class UserDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_USERS_COUNT = 5;
    private static final LocalDate BIRTHDAY = LocalDate.of(1989, 5, 1);
    private final UserDbStorage userDbStorage;

    @Test
    void testFindUserById() {
        Optional<User> userOptional = userDbStorage.findById(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1));

        userOptional = userDbStorage.findById(WRONG_ID);

        assertThat(userOptional).isNotPresent();
    }

    @Test
    void testFindAllUsers() {
        List<User> users = new ArrayList<>(userDbStorage.findAll());

        assertThat(users).hasSize(EXPECTED_USERS_COUNT);
    }

    @Test
    void testDeleteUser() {
        boolean isDeleted = userDbStorage.deleteById(1);

        assertThat(isDeleted).isTrue();

        List<User> users = new ArrayList<>(userDbStorage.findAll());

        assertThat(users).hasSize(EXPECTED_USERS_COUNT - 1);

        isDeleted = userDbStorage.deleteById(WRONG_ID);

        assertThat(isDeleted).isFalse();
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setLogin("My_login");
        user.setBirthday(BIRTHDAY);
        user.setEmail("my_email@yandex.ru");
        user.setName("EKWENSU OCHA");
        User savedUser = userDbStorage.save(user);

        assertThat(savedUser)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);

        List<User> users = new ArrayList<>(userDbStorage.findAll());

        assertThat(users).hasSize(EXPECTED_USERS_COUNT + 1);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setId(1);
        user.setLogin("My_login");
        user.setBirthday(BIRTHDAY);
        user.setEmail("my_email@yandex.ru");
        user.setName("EKWENSU OCHA");

        userDbStorage.update(user);
        Optional<User> updatedUser = userDbStorage.findById(1);

        assertThat(updatedUser)
                .isPresent()
                .isEqualTo(Optional.of(user));

        user.setId(WRONG_ID);

        updatedUser = userDbStorage.update(user);

        assertThat(updatedUser).isNotPresent();
    }

    @Test
    void testGetFriendsById() {
        userDbStorage.addFriend(1, 2);
        Collection<User> friends = userDbStorage.findFriendsById(1);

        assertThat(friends).hasSize(1);

        userDbStorage.removeFriend(1, 2);
        friends = userDbStorage.findFriendsById(1);

        assertThat(friends).isEmpty();
    }

    @Test
    void deletedUserShouldBeRemovedFromFriends() {
        userDbStorage.addFriend(1, 2);
        Collection<User> friends = userDbStorage.findFriendsById(1);

        assertThat(friends).hasSize(1);

        userDbStorage.deleteById(2);
        friends = userDbStorage.findFriendsById(1);

        assertThat(friends).isEmpty();
    }
}
