package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class FilmDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_FILMS_COUNT = 3;
    private final FilmDbStorage filmDdStorage;

    @Test
    void testFindFilmById() {
        Optional<Film> filmOptional = filmDdStorage.findById(1);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(Film ->
                        assertThat(Film).hasFieldOrPropertyWithValue("id", 1));

        filmOptional = filmDdStorage.findById(WRONG_ID);

        assertThat(filmOptional).isNotPresent();
    }

    @Test
    void testFindAllFilms() {
        List<Film> films = new ArrayList<>(filmDdStorage.findAll());

        assertThat(films).hasSize(EXPECTED_FILMS_COUNT);
    }

    @Test
    void testDeleteFilm() {
        boolean isDeleted = filmDdStorage.deleteById(1);

        assertThat(isDeleted).isTrue();

        List<Film> Films = new ArrayList<>(filmDdStorage.findAll());

        assertThat(Films).hasSize(EXPECTED_FILMS_COUNT - 1);

        isDeleted = filmDdStorage.deleteById(WRONG_ID);

        assertThat(isDeleted).isFalse();
    }

    @Test
    void testSaveFilm() {
        Film film = new Film();
        film.setName("My film");
        film.setDescription("Awesome movie");
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        film.setReleaseDate(LocalDate.of(2022, 1, 11));
        film.setGenres(Set.of(new Genre(1, "Комедия")));
        Film savedFilm = filmDdStorage.save(film);

        assertThat(savedFilm).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(film);

        List<Film> Films = new ArrayList<>(filmDdStorage.findAll());

        assertThat(Films).hasSize(EXPECTED_FILMS_COUNT + 1);
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setId(1);
        film.setName("My film");
        film.setDescription("Awesome movie");
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        film.setReleaseDate(LocalDate.of(2022, 1, 11));
        film.setGenres(Set.of(new Genre(1, "Комедия")));

        filmDdStorage.update(film);
        Optional<Film> updatedFilm = filmDdStorage.findById(1);

        assertThat(updatedFilm)
                .isPresent()
                .isEqualTo(Optional.of(film));

        film.setId(WRONG_ID);

        updatedFilm = filmDdStorage.update(film);

        assertThat(updatedFilm).isNotPresent();
    }
}
