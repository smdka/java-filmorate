package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class GenreDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_GENRE_COUNT = 6;
    private final GenreDbStorage genreDbStorage;

    @Test
    void findAllTest() {
        List<Genre> genres = genreDbStorage.findAll();

        assertThat(genres).hasSize(EXPECTED_GENRE_COUNT);
    }

    @Test
    void findByIdTest() {
        Optional<Genre> optionalGenre = genreDbStorage.findById(1);

        assertThat(optionalGenre)
                .isPresent()
                .isEqualTo(Optional.of(new Genre(1, "Комедия")));

        optionalGenre = genreDbStorage.findById(WRONG_ID);

        assertThat(optionalGenre)
                .isNotPresent();
    }
}
