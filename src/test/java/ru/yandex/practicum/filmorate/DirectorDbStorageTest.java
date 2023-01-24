package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class DirectorDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_DIRECTOR_COUNT = 2;
    private final DirectorDbStorage directorDbStorage;

    @Test
    void findAllTest() {
        Collection<Director> directors = directorDbStorage.getAll();

        assertThat(directors).hasSize(EXPECTED_DIRECTOR_COUNT);
    }

    @Test
    void findByIdTest() {
        Optional<Director> optionalDirector = directorDbStorage.findById(2);

        assertThat(optionalDirector)
                .isPresent()
                .isEqualTo(Optional.of(new Director(2, "Coppola")));

        optionalDirector = directorDbStorage.findById(WRONG_ID);

        assertThat(optionalDirector)
                .isNotPresent();
    }
}