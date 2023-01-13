package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;

@Service
@Slf4j
public class MpaService {
    private final MpaDbStorage storage;

    public MpaService(MpaDbStorage storage) {
        this.storage = storage;
    }

    public Collection<Mpa> getAllMpas() {
        log.debug("Список всех рейтингов успешно отправлен");
        return storage.findAll();
    }

    public Mpa getMpaById(int id) {
        Mpa mpa = storage.findById(id)
                .orElseThrow(() -> new MpaNotFoundException(String.format("Рейтинг с id = %d не существует", id)));
        log.debug("Рейтинг с id = {} успешно отправлен", id);
        return mpa;
    }
}
