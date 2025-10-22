package com.example.api_rest.controller;

import com.example.api_rest.config.ApiConfig;
import com.example.api_rest.config.SecurityConfig;
import com.example.api_rest.dto.VideoGameDTO;
import com.example.api_rest.entity.Genre;
import com.example.api_rest.entity.VideoGame;
import com.example.api_rest.exception.VideoGameNotFoundException;
import com.example.api_rest.exception.handler.CustomSecurityExceptionHandler;
import com.example.api_rest.exception.utils.ErrorResponseFactory;
import com.example.api_rest.filter.JwtAuthenticationFilter;
import com.example.api_rest.service.AuthService;
import com.example.api_rest.service.impl.VideoGameServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoGameController.class)
@Import({SecurityConfig.class, CustomSecurityExceptionHandler.class, ErrorResponseFactory.class})
public class VideoGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    VideoGameServiceImpl videoGameService;

    @MockitoBean
    private AuthService authService;

    // ============= SecurityConfig dependencies ===========

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    // =====================================================

    private static final String BASE_PATH = ApiConfig.API_BASE_PATH + "/games";

    @BeforeEach
    void setUp() {
        when(authService.validateToken(anyString())).thenReturn(Boolean.TRUE);
        when(authService.getUserFromToken(anyString())).thenReturn("mockUser");
        when(authService.getAuthoritiesFromToken("user-token")).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authService.getAuthoritiesFromToken("admin-token")).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void findAllTest_asUser_returns200() throws Exception {
        List<VideoGame> expectedList = List.of(
                new VideoGame("Bioshock"),
                new VideoGame(2L, "Dark Souls"),
                new VideoGame("StarCraft", LocalDate.of(1998, 3, 31), "Blizzard Entertainment", Genre.STRATEGY)
        );

        when(videoGameService.findAll())
                .thenReturn(expectedList);

        mockMvc.perform(get(BASE_PATH)
                        .cookie(jwtCookie("user-token"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedList.size()))
                .andExpect(jsonPath("$[0].name").value(expectedList.getFirst().getName()))
                .andExpect(jsonPath("$[1].id").value(expectedList.get(1).getId()))
                .andExpect(jsonPath("$[2].developedBy").value(expectedList.get(2).getDevelopedBy()));
    }

    @Test
    void findAllTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findByIdTest_asUser_validData_returns200() throws Exception {
        VideoGame videoGame = new VideoGame(5L, "Minecraft");

        when(videoGameService.findById(videoGame.getId()))
                .thenReturn(videoGame);

        mockMvc.perform(get(BASE_PATH + "/" + videoGame.getId())
                        .cookie(jwtCookie("user-token"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(videoGame.getId()))
                .andExpect(jsonPath("$.name").value(videoGame.getName()));
    }

    @Test
    void findByIdTest_asUser_invalidId_returns404() throws Exception {
        when(videoGameService.findById(any(Long.class)))
                .thenThrow(VideoGameNotFoundException.class);

        mockMvc.perform(get(BASE_PATH + "/45")
                        .cookie(jwtCookie("user-token"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByIdTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/45"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerTest_asAdmin_returns201() throws Exception {
        VideoGameDTO dto = new VideoGameDTO("Age of Empires", LocalDate.now(), "", Genre.STRATEGY);
        VideoGame mockSavedVideoGame = new VideoGame(99L, "Age of Empires");
        when(videoGameService.register(dto))
                .thenReturn(mockSavedVideoGame);

        mockMvc.perform(post(BASE_PATH)
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString(BASE_PATH + "/99")))
                .andExpect(jsonPath("$.name").value("Age of Empires"));
    }

    @Test
    void registerTest_asAdmin_invalidData_returns400() throws Exception {
        VideoGameDTO dto = new VideoGameDTO(null, null, null, null);

        mockMvc.perform(post(BASE_PATH)
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTest_asUser_returns403() throws Exception {
        mockMvc.perform(post(BASE_PATH)
                        .cookie(jwtCookie("user-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(post(BASE_PATH))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTest_asAdmin_validData_returns2xx() throws Exception {
        VideoGameDTO dto = new VideoGameDTO(
                "Dragon Quest",
                LocalDate.of(1986, 5, 27),
                "Enix",
                Genre.RPG);
        VideoGame updatedVideoGame = new VideoGame("Dragon Quest mod.");
        when(videoGameService.update(any(Long.class), any(VideoGameDTO.class)))
                .thenReturn(updatedVideoGame);

        mockMvc.perform(put(BASE_PATH + "/10")
                    .cookie(jwtCookie("admin-token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name").value("Dragon Quest mod."));
    }

    @Test
    void updateTest_asAdmin_invalidId_returns404() throws Exception {
        VideoGameDTO dto = new VideoGameDTO("mockName", LocalDate.now(), "", Genre.ACTION);
        when(videoGameService.update(any(Long.class), any(VideoGameDTO.class)))
                .thenThrow(VideoGameNotFoundException.class);
        mockMvc.perform(put(BASE_PATH + "/10")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTest_asAdmin_invalidData_returns400() throws Exception {
        VideoGameDTO dto = new VideoGameDTO(null, null, null, null);

        mockMvc.perform(put(BASE_PATH + "/10")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTest_asUser_returns403() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/10")
                    .cookie(jwtCookie("user-token")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(videoGameService);
    }

    @Test
    void updateTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/10"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(videoGameService);
    }

    @Test
    void deleteTest_asAdmin_validData_returns204() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/20")
                    .cookie(jwtCookie("admin-token")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTest_asAdmin_invalidId_returns404() throws Exception {
        doThrow(VideoGameNotFoundException.class)
                .when(videoGameService)
                        .delete(any(Long.class));
        mockMvc.perform(delete(BASE_PATH + "/20")
                        .cookie(jwtCookie("admin-token")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTest_asUser_returns403() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/20")
                        .cookie(jwtCookie("user-token")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(videoGameService);
    }

    @Test
    void deleteTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(delete(BASE_PATH + "/20"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(videoGameService);
    }

    @Test
    void deleteAllTest_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete(BASE_PATH)
                        .cookie(jwtCookie("admin-token")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAllTest_asUser_returns403() throws Exception {
        mockMvc.perform(delete(BASE_PATH)
                        .cookie(jwtCookie("user-token")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(videoGameService);
    }

    @Test
    void deleteAllTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(delete(BASE_PATH))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(videoGameService);
    }

    private MockCookie jwtCookie(String tokenValue) {
        return new MockCookie("auth-token", tokenValue);
    }
}
