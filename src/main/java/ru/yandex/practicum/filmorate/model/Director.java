package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director implements Comparable<Director> {
    private int id;
    @NotBlank(message = "Имя фильма обязательно")
    private String name;

    @Override
    public int compareTo(Director o) {
        return Integer.compare(this.id, o.getId());
    }
}
