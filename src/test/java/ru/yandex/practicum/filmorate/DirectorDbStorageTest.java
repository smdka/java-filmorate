package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.*;

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
        Collection<Director> directors = directorDbStorage.findAll();

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

    @Test
    void saveDirectorTest() {
        Director director = new Director();
        director.setName("Tarantino");

        Director savedDirector = directorDbStorage.save(director);

        assertThat(savedDirector).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(director);

        List<Director> directors = new ArrayList<>(directorDbStorage.findAll());

        assertThat(directors).hasSize(EXPECTED_DIRECTOR_COUNT + 1);
    }

    @Test
    void deleteById() {
        boolean isDeleted = directorDbStorage.deleteById(1);

        assertThat(isDeleted).isTrue();

        List<Director> directors = new ArrayList<>(directorDbStorage.findAll());

        assertThat(directors).hasSize(EXPECTED_DIRECTOR_COUNT - 1);

        isDeleted = directorDbStorage.deleteById(WRONG_ID);

        assertThat(isDeleted).isFalse();
    }

    @Test
    void update() {
        Director director = new Director();
        director.setId(1);
        director.setName("Spielberg");

        directorDbStorage.update(director);
        Optional<Director> updatedFilm = directorDbStorage.findById(1);

        assertThat(updatedFilm)
                .isPresent()
                .isEqualTo(Optional.of(director));

        director.setId(WRONG_ID);

        updatedFilm = directorDbStorage.update(director);

        assertThat(updatedFilm).isNotPresent();
    }
}