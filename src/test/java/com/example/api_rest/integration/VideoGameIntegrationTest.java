package com.example.api_rest.integration;

import com.example.api_rest.config.ApiConfig;
import com.example.api_rest.dto.VideoGameDTO;
import com.example.api_rest.entity.Genre;
import com.example.api_rest.entity.Role;
import com.example.api_rest.entity.VideoGame;
import com.example.api_rest.repository.VideoGameRepository;
import com.example.api_rest.service.AuthService;
import com.example.api_rest.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class VideoGameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoGameRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_PATH = ApiConfig.API_BASE_PATH + "/games";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void findAllTest_asUser_returns200() throws Exception {
        List<VideoGame> videoGameList = List.of(
                new VideoGame("Bioshock"),
                new VideoGame("Dark Souls"),
                new VideoGame("StarCraft", LocalDate.of(1998, 3, 31), "Blizzard Entertainment", Genre.STRATEGY)
        );
        repository.saveAll(videoGameList);

        ResultActions result = mockMvc.perform(get(BASE_PATH)
                .cookie(jwtCookie(Role.USER))
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(videoGameList.size()))
                .andExpect(jsonPath("$[2].name").value(videoGameList.get(2).getName()));
    }

    @Test
    void findAllTest_asAnonymous_returns401() throws Exception {
        ResultActions result = mockMvc.perform(get(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    void findByIdTest_asUser_validData_returns200() throws Exception {
        VideoGame videoGame = new VideoGame("Minecraft");

        videoGame = repository.save(videoGame);

        mockMvc.perform(get(BASE_PATH + "/" + videoGame.getId())
                        .cookie(jwtCookie(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(videoGame.getId()))
                .andExpect(jsonPath("$.name").value(videoGame.getName()));
    }

    @Test
    void findByIdTest_asUser_invalidId_returns404() throws Exception {
        long id = 99L;

        mockMvc.perform(get(BASE_PATH + "/" + id)
                        .cookie(jwtCookie(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByIdTest_asAnonymous_returns401() throws Exception {
        long id = 99L;

        mockMvc.perform(get(BASE_PATH + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerTest_asAdmin_returns201() throws Exception {
        VideoGameDTO dto = new VideoGameDTO("Age of Empires", LocalDate.now(), "", Genre.STRATEGY);

        mockMvc.perform(post(BASE_PATH)
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Age of Empires"));
    }

    @Test
    void registerTest_asAdmin_invalidData_returns400() throws Exception {
        VideoGameDTO dto = new VideoGameDTO(null, null, null, null);

        mockMvc.perform(post(BASE_PATH)
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTest_asUser_returns403() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .cookie(jwtCookie(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(post(BASE_PATH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTest_asAdmin_validData_returns2xx() throws Exception {
        VideoGame videoGame = new VideoGame("Dragon Quest",
                LocalDate.of(1986, 5, 27),
                "Enix",
                Genre.RPG);
        videoGame = repository.save(videoGame);

        VideoGameDTO dto = new VideoGameDTO(
                videoGame.getName() + " mod.",
                videoGame.getReleaseDate(),
                videoGame.getDevelopedBy(),
                videoGame.getGenre());


        mockMvc.perform(put(BASE_PATH + "/" + videoGame.getId())
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name").value(dto.name()))
                .andExpect(jsonPath("$.releaseDate").value(dto.releaseDate().toString()));
    }

    @Test
    void updateTest_asAdmin_invalidId_returns404() throws Exception {
        VideoGameDTO dto = new VideoGameDTO("name", LocalDate.now(), "", Genre.ACTION);
        mockMvc.perform(put(BASE_PATH + "/10")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTest_asAdmin_invalidData_returns400() throws Exception {
        VideoGameDTO dto = new VideoGameDTO(null, null, null, null);

        mockMvc.perform(put(BASE_PATH + "/10")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTest_asUser_returns403() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/10")
                        .cookie(jwtCookie(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTest_asAdmin_validData_returns204() throws Exception {
        VideoGame videoGame = new VideoGame("name", LocalDate.now(), "", Genre.RPG);
        videoGame = repository.save(videoGame);

        mockMvc.perform(delete(BASE_PATH + "/" + videoGame.getId())
                        .cookie(jwtCookie(Role.ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(videoGame.getId())).isEmpty();
    }

    @Test
    void deleteTest_asAdmin_invalidId_returns404() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/99")
                        .cookie(jwtCookie(Role.ADMIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTest_asUser_returns403() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/99")
                        .cookie(jwtCookie(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/99"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAllTest_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete(BASE_PATH)
                        .cookie(jwtCookie(Role.ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAllTest_asUser_returns403() throws Exception {
        mockMvc.perform(delete(BASE_PATH)
                        .cookie(jwtCookie(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAllTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(delete(BASE_PATH))
                .andExpect(status().isUnauthorized());
    }

    private Cookie jwtCookie(Role role) {
        String token = tokenService.generateToken(
                new UsernamePasswordAuthenticationToken(
                        role.name(),
                        "1234",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
        return new Cookie("auth-token", token);
    }
}
