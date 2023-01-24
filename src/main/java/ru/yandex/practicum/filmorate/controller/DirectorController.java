package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
public class DirectorController {
    DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> getAll() {
        log.debug("Получен запрос GET /directors");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director findById(@PathVariable int id) {
        log.debug("Получен запрос GET /directors/{id}");
        return directorService.findById(id);
    }
    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director, BindingResult bindingResult) {
        ifHasErrorsThrow(bindingResult);
        return directorService.addDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        directorService.deleteById(id);
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        return directorService.update(director);
    }

    private void ifHasErrorsThrow(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (FieldError e : bindingResult.getFieldErrors()) {
                log.warn("Не пройдена валидация режиссера: {} = {}", e.getField(), e.getRejectedValue());
            }
            throw new ValidationException(bindingResult.getFieldErrors().toString());
        }
    }
}
