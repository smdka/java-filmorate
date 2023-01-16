package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class MpaDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_MPA_COUNT = 5;
    private final MpaDbStorage mpaDbStorage;

    @Test
    void findAllTest() {
        Collection<Mpa> mpas = mpaDbStorage.findAll();

        assertThat(mpas).hasSize(EXPECTED_MPA_COUNT);
    }

    @Test
    void findByIdTest() {
        Optional<Mpa> optionalMpa = mpaDbStorage.findById(1);

        assertThat(optionalMpa)
                .isPresent()
                .isEqualTo(Optional.of(new Mpa(1, "G")));

        optionalMpa = mpaDbStorage.findById(WRONG_ID);

        assertThat(optionalMpa)
                .isNotPresent();
    }
}
