package com.example.api_rest.controller;

import com.example.api_rest.config.ApiConfig;
import com.example.api_rest.config.SecurityConfig;
import com.example.api_rest.dto.DeleteUserDTO;
import com.example.api_rest.dto.EditUserDTO;
import com.example.api_rest.dto.LoginUserDTO;
import com.example.api_rest.dto.RegisterUserDTO;
import com.example.api_rest.entity.Role;
import com.example.api_rest.entity.User;
import com.example.api_rest.exception.UserNotFoundException;
import com.example.api_rest.exception.UsernameAlreadyExistsException;
import com.example.api_rest.exception.handler.CustomSecurityExceptionHandler;
import com.example.api_rest.exception.utils.ErrorResponseFactory;
import com.example.api_rest.filter.JwtAuthenticationFilter;
import com.example.api_rest.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CustomSecurityExceptionHandler.class, ErrorResponseFactory.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private static final String BASE_PATH = ApiConfig.API_BASE_PATH + "/auth";

    @BeforeEach
    void setUp() {
        when(authService.validateToken(anyString())).thenReturn(Boolean.TRUE);
        when(authService.getAuthoritiesFromToken("admin-token")).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authService.getAuthoritiesFromToken("user-token")).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void registerTest_asAdmin_withValidData_returns201() throws Exception{
        RegisterUserDTO mockUser = new RegisterUserDTO("mockUser", "1234", null);
        User newUser = new User(23L, "mockUser", "1234");
        when(authService.register(mockUser))
                .thenReturn(newUser);

        mockMvc.perform(post(BASE_PATH + "/register")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newUser.getId()))
                .andExpect(jsonPath("$.username").value(newUser.getUsername()));
    }

    @Test
    void registerTest_asAdmin_withDuplicatedData_returns400() throws Exception{
        RegisterUserDTO mockUser = new RegisterUserDTO("mockUser", "1234", null);
        when(authService.register(mockUser))
                .thenThrow(UsernameAlreadyExistsException.class);

        mockMvc.perform(post(BASE_PATH + "/register")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTest_asUser_returns403() throws Exception{
        mockMvc.perform(post(BASE_PATH + "/register")
                        .cookie(jwtCookie("user-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerTest_asAnonymous_returns401() throws Exception{
        mockMvc.perform(post(BASE_PATH + "/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginTest_validData_returns2xx() throws Exception {
        LoginUserDTO dto = new LoginUserDTO("mockUser", "1234");
        when(authService.login(dto)).thenReturn("mock-token");

        mockMvc.perform(post(BASE_PATH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(cookie().exists("auth-token"));
    }

    @Test
    void loginTest_invalidData_returns401() throws Exception {
        LoginUserDTO dto = new LoginUserDTO("mockUser", "1234");
        when(authService.login(dto))
                .thenThrow(BadCredentialsException.class);

        mockMvc.perform(post(BASE_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void editTest_asAdmin_returns2xx() throws Exception {
        EditUserDTO dto = new EditUserDTO(
                "mockUser",
                "1234",
                null,
                Role.ADMIN
        );
        User newUser = new User(7L, dto.username(), dto.password());
        newUser.setRole(Role.ADMIN);
        when(authService.edit(dto))
                .thenReturn(newUser);

        mockMvc.perform(put(BASE_PATH + "/edit")
                    .cookie(jwtCookie("admin-token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(dto.username()))
                .andExpect(jsonPath("$.role").value(dto.role().name()));
    }

    @Test
    void editTest_asAdmin_invalidData_returns404() throws Exception {
        EditUserDTO dto = new EditUserDTO(
                "mockUser",
                "1234",
                null,
                Role.ADMIN
        );
        when(authService.edit(dto))
                .thenThrow(UserNotFoundException.class);

        mockMvc.perform(put(BASE_PATH + "/edit")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void editTest_asUser_returns403() throws Exception{
        mockMvc.perform(put(BASE_PATH + "/edit")
                        .cookie(jwtCookie("user-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    void editTest_asAnonymous_returns401() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/edit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTest_asAdmin_validData_returns2xx() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(2L, "Juanito");

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTest_asAdmin_invalidData_returns400() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(null, null);
        doThrow(IllegalArgumentException.class)
                .when(authService)
                .delete(dto);

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTest_asAdmin_userNotFound_returns404() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(5L, "Manuela");
        doThrow(UserNotFoundException.class)
                .when(authService)
                .delete(dto);

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie("admin-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTest_asUser_returns403() throws Exception {
        DeleteUserDTO dto = new DeleteUserDTO(2L, "Juanito");

        mockMvc.perform(delete(BASE_PATH + "/delete")
                        .cookie(jwtCookie("user-token"))
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

    private Cookie jwtCookie(String tokenValue) {
        return new Cookie("auth-token", tokenValue);
    }
}
