package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmsController;
import ru.yandex.practicum.filmorate.controller.UsersController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private FilmsController filmController;
    @Autowired
    private UsersController userController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(filmController).isNotNull();
        assertThat(userController).isNotNull();
    }

    @Test
    void emptyRequestShouldReturnBadRequest() throws Exception {
        this.mockMvc.perform(post("/users"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/users"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(post("/films"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(put("/films"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
