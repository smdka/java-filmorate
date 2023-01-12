package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre implements Comparable<Genre> {
    private int id;
    private String name;

    @Override
    public int compareTo(Genre o) {
        return Integer.compare(this.id, o.getId());
    }
}
