package ru.yandex.practicum.filmorate.utilities.recommendations;

import java.util.*;

public class Matrix {
    private final Map<Integer, Map<Integer, Optional<Double>>> rows = new HashMap<>();
    private final Map<Integer, Map<Integer, Optional<Double>>> columns = new HashMap<>();

    public Optional<Double> getValue(Integer row, Integer column) {
        if (rows.get(row) != null && rows.get(row).get(column) != null) {
            return rows.get(row).get(column);
        } else {
            return null;
        }
    }
    public Map<Integer, Optional<Double>> getRow(Integer row) {
        return rows.get(row);
    }
    public Map<Integer, Optional<Double>> getColumn(Integer column) {
        return columns.get(column);
    }

    public Matrix writeValue(Integer row, Integer column, Optional<Double> value) {
        if (row > 0 && column > 0) {
            rows.putIfAbsent(row, new HashMap<>());
            columns.putIfAbsent(column, new HashMap<>());
            rows.get(row).put(column, value);
            columns.get(column).put(row, value);
            expand();
        }
        return this;
    }

    public List<Integer> getRowIndexes() {return new ArrayList<>(rows.keySet());}
    public List<Integer> getColumnIndexes() {return new ArrayList<>(columns.keySet());}

    public boolean isEmpty(){
        return rows.isEmpty() && columns.isEmpty();
    }

    private void expand() {
        for (Integer rowI : getRowIndexes()) {
            for (Integer colI : getColumnIndexes()) {
                rows.get(rowI).putIfAbsent(colI, Optional.empty());
                columns.get(colI).putIfAbsent(rowI, Optional.empty());
            }
        }
    }
}
