package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDdStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class FilmDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_FILMS_COUNT = 3;
    private final FilmDbStorage filmDdStorage;
    private final UserDdStorage userDdStorage;

    @Test
    void testFindFilmById() {
        Optional<Film> film = filmDdStorage.findById(1);

        assertThat(film)
                .isPresent()
                .hasValueSatisfying(Film ->
                        assertThat(Film).hasFieldOrPropertyWithValue("id", 1));

        film = filmDdStorage.findById(WRONG_ID);

        assertThat(film).isNotPresent();
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

        List<Film> films = new ArrayList<>(filmDdStorage.findAll());

        assertThat(films).hasSize(EXPECTED_FILMS_COUNT - 1);

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
        film.setGenres(new TreeSet<>(Set.of(new Genre(1, "Комедия"))));
        Film savedFilm = filmDdStorage.save(film);

        assertThat(savedFilm).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(film);

        List<Film> films = new ArrayList<>(filmDdStorage.findAll());

        assertThat(films).hasSize(EXPECTED_FILMS_COUNT + 1);
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
        film.setGenres(new TreeSet<>(Set.of(new Genre(1, "Комедия"))));

        filmDdStorage.update(film);
        Optional<Film> updatedFilm = filmDdStorage.findById(1);

        assertThat(updatedFilm)
                .isPresent()
                .isEqualTo(Optional.of(film));

        film.setId(WRONG_ID);

        updatedFilm = filmDdStorage.update(film);

        assertThat(updatedFilm).isNotPresent();
    }

    @Test
    void testLimitInMostPopularFilms() {
        int n = 2;
        filmDdStorage.addLike(1, 1);
        filmDdStorage.addLike(1, 2);
        filmDdStorage.addLike(1, 3);
        filmDdStorage.addLike(1, 4);
        Film expectedFilm = filmDdStorage.findById(1).get();
        Collection<Film> topNMostPopular = filmDdStorage.findTopNMostPopular(n, Optional.empty(), Optional.empty());
        assertThat(topNMostPopular).hasSize(2);
        assertEquals(expectedFilm, topNMostPopular.stream().findFirst().get());
    }

    @Test
    void testMostPopularByGenreFilms() {
        int n = 3;
        Film expectedFilm = filmDdStorage.findById(2).get();
        Collection<Film> topNMostPopular = filmDdStorage.findTopNMostPopular(n, Optional.of(6), Optional.empty());
        assertThat(topNMostPopular).hasSize(2);
        assertEquals(expectedFilm, topNMostPopular.stream().findFirst().get());
    }

    @Test
    void testMostPopularByYearFilms() {
        int n = 5;
        Film expectedFilm = filmDdStorage.findById(1).get();
        Collection<Film> topNMostPopular = filmDdStorage.findTopNMostPopular(n, Optional.empty(), Optional.of(1982));
        assertThat(topNMostPopular).hasSize(1);
        assertEquals(expectedFilm, topNMostPopular.stream().findFirst().get());
    }

    @Test
    void testAddLikeFromUser() {
        Film film = filmDdStorage.findById(3).get();
        int likesCount = film.getLikesCount();

        assertThat(filmDdStorage.addLike(3, 2)).isTrue();

        film = filmDdStorage.findById(3).get();

        assertEquals(likesCount + 1, film.getLikesCount());
    }

    @Test
    void testDeleteLikeFromUser() {
        Film film = filmDdStorage.findById(3).get();
        int likesCount = film.getLikesCount();

        assertThat(filmDdStorage.deleteLike(3, 1)).isTrue();

        film = filmDdStorage.findById(3).get();

        assertEquals(likesCount - 1, film.getLikesCount());

        assertThat(filmDdStorage.deleteLike(WRONG_ID, 2)).isFalse();
    }

    @Test
    void feedWithLikeTest(){
        Collection<Feed> feeds = userDdStorage.getFeeds(2);
        assertThat(feeds).hasSize(0);

        filmDdStorage.addLike(1, 2);
        feeds = userDdStorage.getFeeds(2);

        assertThat(feeds).hasSize(1);

        filmDdStorage.deleteLike(1, 2);
        feeds = userDdStorage.getFeeds(2);

        assertThat(feeds).hasSize(2);
        }
        
    @Test
    void testFindCommonFilms() {
        Collection<Film> commonFilms = filmDdStorage.findCommonFilms(1, 3);

        assertThat(commonFilms).hasSize(2);

        assertThat(commonFilms)
                .anyMatch(film -> film.getId() == 3)
                .anyMatch(film -> film.getId() == 2);
    }

    @Test
    void commonFilmsWithWrongUserIdShouldBeEmpty() {
        Collection<Film> commonFilms = filmDdStorage.findCommonFilms(WRONG_ID, 3);

        assertThat(commonFilms).isEmpty();

        commonFilms = filmDdStorage.findCommonFilms(1, WRONG_ID);

        assertThat(commonFilms).isEmpty();
    }

    @Test
    void getFilmsByDirectorTest() {
        List<Film> filmListSortByYear = new ArrayList<>(filmDdStorage.getFilmsByDirector(2, "year"));
        assertEquals("Terminator", filmListSortByYear.get(0).getName());

        List<Film> filmListSortByLikes = new ArrayList<>(filmDdStorage.getFilmsByDirector(2, "likes"));
        assertEquals("Snatch", filmListSortByLikes.get(0).getName());
    }
}
