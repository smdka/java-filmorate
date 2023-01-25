package ru.yandex.practicum.filmorate.utilities;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlArrayConverter {
     public <T> List<T> convert(Array array) throws SQLException {
         ResultSet rs = array.getResultSet();
         rs.next();
         if (rs.getObject("VALUE") == null) {
             return null;
         } else {
             List<Object> elements = new ArrayList<>();
             elements.add(rs.getObject("VALUE"));
             while (rs.next()) {
                 elements.add(rs.getObject("VALUE"));
             }
             return (List<T>)elements;
         }

    }
}
