package com.example.api_rest.integration;

import com.example.api_rest.config.ApiConfig;
import com.example.api_rest.dto.DeleteUserDTO;
import com.example.api_rest.dto.EditUserDTO;
import com.example.api_rest.dto.LoginUserDTO;
import com.example.api_rest.dto.RegisterUserDTO;
import com.example.api_rest.entity.Role;
import com.example.api_rest.entity.User;
import com.example.api_rest.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_PATH = ApiConfig.API_BASE_PATH + "/auth";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void registerTest_asAdmin_withValidData_returns201() throws Exception{
        RegisterUserDTO dto = new RegisterUserDTO("newUser", "1234", "example@gmail.com");

        mockMvc.perform(post(BASE_PATH + "/register")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(dto.username()))
                .andExpect(jsonPath("$.email").value(dto.email()));
    }

    @Test
    void registerTest_asAdmin_withDuplicatedData_returns400() throws Exception{
        User user = new User("user", "1234");
        repository.save(user);

        RegisterUserDTO dto = new RegisterUserDTO(user.getUsername(), "321", null);

        mockMvc.perform(post(BASE_PATH + "/register")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTest_asUser_returns403() throws Exception{
        mockMvc.perform(post(BASE_PATH + "/register")
                        .cookie(jwtCookie(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerTest_asAnonymous_returns401() throws Exception{
        mockMvc.perform(post(BASE_PATH + "/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginTest_validData_returns2xx() throws Exception {
        User user = new User(
                "user",
                passwordEncoder.encode("1234"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER);
        repository.save(user);

        LoginUserDTO dto = new LoginUserDTO(user.getUsername(), "1234");

        mockMvc.perform(post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(cookie().exists("auth-token"));
    }

    @Test
    void loginTest_invalidData_returns401() throws Exception {
        LoginUserDTO dto = new LoginUserDTO("user", "1234");

        mockMvc.perform(post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void editTest_asAdmin_returns2xx() throws Exception {
        User user = new User(
                "user",
                passwordEncoder.encode("1234"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER);
        repository.save(user);

        EditUserDTO dto = new EditUserDTO(
                "user",
                null,
                null,
                Role.ADMIN
        );

        mockMvc.perform(put(BASE_PATH + "/edit")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.password").value(user.getPassword()))
                .andExpect(jsonPath("$.role").value(dto.role().name()));
    }

    @Test
    void editTest_asAdmin_invalidData_returns404() throws Exception {
        User user = new User(
                "user",
                passwordEncoder.encode("1234"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER);
        repository.save(user);

        EditUserDTO dto = new EditUserDTO(
                "otherUser",
                "1234",
                null,
                Role.ADMIN
        );

        mockMvc.perform(put(BASE_PATH + "/edit")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void editTest_asUser_returns403() throws Exception{
        mockMvc.perform(put(BASE_PATH + "/edit")
                        .cookie(jwtCookie(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void editTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/edit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTest_asAdmin_validData_returns204() throws Exception {
        User user = new User(
                "user",
                passwordEncoder.encode("1234"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER);
        repository.save(user);

        DeleteUserDTO dto = new DeleteUserDTO(null, "user");

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        assertThat(repository.findByUsername("user")).isEmpty();
    }

    @Test
    void deleteTest_asAdmin_invalidData_returns400() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(null, null);

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTest_asAdmin_userNotFound_returns404() throws Exception {
        User user = new User(
                "user",
                passwordEncoder.encode("1234"),
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                Role.USER);
        repository.save(user);

        DeleteUserDTO dto = new DeleteUserDTO(null, "otherUser");

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTest_asUser_returns403() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(2L, "Juanito");

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTest_asAnonymous_returns401() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(2L, "Juanito");

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
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
