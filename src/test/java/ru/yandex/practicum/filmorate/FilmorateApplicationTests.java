package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controller.FilmsController;
import ru.yandex.practicum.filmorate.controller.UsersController;
import ru.yandex.practicum.filmorate.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private FilmsController filmsController;
    @Autowired
    private UsersController usersController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void contextLoads() {
        assertThat(filmsController).isNotNull();
        assertThat(usersController).isNotNull();
    }

    @Test
    public void emptyRequestShouldReturnBadRequest() throws Exception {
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

    @Test
    public void	blankUserNameShouldBeReplacedWithLogin() throws Exception {
        User user = new User();
        user.setLogin("login");
        user.setName("");
        user.setEmail("pepe_the_frog@yandex.ru");
        user.setBirthday(LocalDate.now());

        MvcResult result = this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user).getBytes(StandardCharsets.UTF_8))
                        .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertThat(result).isNotNull();
        String userJson = result.getResponse().getContentAsString();
        assertThat(userJson).isNotEmpty();
        User actualUser = mapper.readValue(userJson, User.class);
        assertThat(user.getLogin()).isEqualTo(actualUser.getName());

        user.setName(null);

        result = this.mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(user).getBytes(StandardCharsets.UTF_8))
                        .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        assertThat(result).isNotNull();
        userJson = result.getResponse().getContentAsString();
        assertThat(userJson).isNotEmpty();
        actualUser = mapper.readValue(userJson, User.class);
        assertThat(user.getLogin()).isEqualTo(actualUser.getName());
    }
}
