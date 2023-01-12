package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDdStorage;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final UserDdStorage userDdStorage;

    @Test
    void testFindUserById() {
        Optional<User> userOptional = userDdStorage.findById(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1));

        userOptional = userDdStorage.findById(WRONG_ID);

        assertThat(userOptional).isNotPresent();
    }

    @Test
    void testFindAllUsers() {
        List<User> users = new ArrayList<>(userDdStorage.findAll());

        assertThat(users).hasSize(EXPECTED_USERS_COUNT);
    }

    @Test
    void testDeleteUser() {
        boolean isDeleted = userDdStorage.deleteById(1);

        assertThat(isDeleted).isTrue();

        List<User> users = new ArrayList<>(userDdStorage.findAll());

        assertThat(users).hasSize(EXPECTED_USERS_COUNT - 1);

        isDeleted = userDdStorage.deleteById(WRONG_ID);

        assertThat(isDeleted).isFalse();
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setLogin("My_login");
        user.setBirthday(BIRTHDAY);
        user.setEmail("my_email@yandex.ru");
        user.setName("EKWENSU OCHA");
        User savedUser = userDdStorage.save(user);

        assertThat(savedUser)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);

        List<User> users = new ArrayList<>(userDdStorage.findAll());

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

        userDdStorage.update(user);
        Optional<User> updatedUser = userDdStorage.findById(1);

        assertThat(updatedUser)
                .isPresent()
                .isEqualTo(Optional.of(user));

        user.setId(WRONG_ID);

        updatedUser = userDdStorage.update(user);

        assertThat(updatedUser).isNotPresent();
    }
}
