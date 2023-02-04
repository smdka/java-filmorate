package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utilities.recommendations.Matrix;
import ru.yandex.practicum.filmorate.utilities.recommendations.Recommender;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class RecommenderTest {
    private static final int WRONG_ID = 9999;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void getRecommendationsForUserWithWrongIdShouldReturnEmptyList() throws Exception {
        MvcResult result = mockMvc.perform(get("/users/"+ WRONG_ID +"/recommendations")).andReturn();
        List<Film> recommendations = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(List.of(), recommendations);
    }

    @Test
    void getRecommendationsForUserWithIds123() throws Exception {
        MvcResult result = mockMvc.perform(get("/users/"+ 1 +"/recommendations")).andReturn();
        List<Film> recommendations = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, recommendations.get(0).getId());
        result = mockMvc.perform(get("/users/"+ 2 +"/recommendations")).andReturn();
        recommendations = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, recommendations.get(0).getId());
        assertEquals(3, recommendations.get(1).getId());
        result = mockMvc.perform(get("/users/"+ 3 +"/recommendations")).andReturn();
        recommendations = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(List.of(), recommendations);
    }

    @Test
    void getRecommendationsForUser5OnTestMatrix() {
        Recommender recommender = new Recommender(generateTestMatrix(), false);
        List<Integer> recommendations = recommender.getRecommendations(5,Optional.empty());
        assertEquals(List.of(2,1), recommendations);
    }

    private Matrix generateTestMatrix() {
        Matrix testMatrix = new Matrix();
        testMatrix.writeValue(1, 1, Optional.of((double) 1));
        testMatrix.writeValue(1, 3, Optional.of((double) 3));
        testMatrix.writeValue(1, 6, Optional.of((double) 5));
        testMatrix.writeValue(1, 9, Optional.of((double) 5));
        testMatrix.writeValue(1, 11, Optional.of((double) 4));
        testMatrix.writeValue(2, 3, Optional.of((double) 5));
        testMatrix.writeValue(2, 4, Optional.of((double) 4));
        testMatrix.writeValue(2, 7, Optional.of((double) 4));
        testMatrix.writeValue(2, 10, Optional.of((double) 2));
        testMatrix.writeValue(2, 11, Optional.of((double) 1));
        testMatrix.writeValue(2, 12, Optional.of((double) 3));
        testMatrix.writeValue(3, 1, Optional.of((double) 2));
        testMatrix.writeValue(3, 2, Optional.of((double) 4));
        testMatrix.writeValue(3, 4, Optional.of((double) 1));
        testMatrix.writeValue(3, 5, Optional.of((double) 2));
        testMatrix.writeValue(3, 7, Optional.of((double) 3));
        testMatrix.writeValue(3, 9, Optional.of((double) 4));
        testMatrix.writeValue(3, 10, Optional.of((double) 3));
        testMatrix.writeValue(3, 11, Optional.of((double) 5));
        testMatrix.writeValue(4, 2, Optional.of((double) 2));
        testMatrix.writeValue(4, 3, Optional.of((double) 4));
        testMatrix.writeValue(4, 5, Optional.of((double) 5));
        testMatrix.writeValue(4, 8, Optional.of((double) 4));
        testMatrix.writeValue(4, 11, Optional.of((double) 2));
        testMatrix.writeValue(5, 3, Optional.of((double) 4));
        testMatrix.writeValue(5, 4, Optional.of((double) 3));
        testMatrix.writeValue(5, 5, Optional.of((double) 4));
        testMatrix.writeValue(5, 6, Optional.of((double) 2));
        testMatrix.writeValue(5, 11, Optional.of((double) 2));
        testMatrix.writeValue(5, 12, Optional.of((double) 5));
        testMatrix.writeValue(6, 1, Optional.of((double) 1));
        testMatrix.writeValue(6, 3, Optional.of((double) 3));
        testMatrix.writeValue(6, 5, Optional.of((double) 3));
        testMatrix.writeValue(6, 8, Optional.of((double) 2));
        testMatrix.writeValue(6, 11, Optional.of((double) 4));
        return testMatrix;
    }
}
