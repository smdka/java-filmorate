package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final GenreService service;

    public GenreController(GenreService service) {
        this.service = service;
    }


    @GetMapping
    public Collection<Genre> findAll() {
        log.debug("Получен запрос GET /genres");
        return service.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getMpaById(@PathVariable int id) {
        log.debug("Получен запрос GET /genres/{}", id);
        return service.getGenreById(id);
    }
}
