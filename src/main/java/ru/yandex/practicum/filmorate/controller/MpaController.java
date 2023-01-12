package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService service;


    public MpaController(MpaService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<Mpa> findAll() {
        log.debug("Получен запрос GET /mpa");
        return service.getAllMpas();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        log.debug("Получен запрос GET /mpa/" + id);
        return service.getMpaById(id);
    }
}
