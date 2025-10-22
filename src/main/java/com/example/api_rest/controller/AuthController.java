package com.example.api_rest.controller;

import com.example.api_rest.config.ApiConfig;
import com.example.api_rest.dto.DeleteUserDTO;
import com.example.api_rest.dto.EditUserDTO;
import com.example.api_rest.dto.LoginUserDTO;
import com.example.api_rest.dto.RegisterUserDTO;
import com.example.api_rest.entity.User;
import com.example.api_rest.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = ApiConfig.API_BASE_PATH + "/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;
    @Value("${app.jwt.cookie-http-only}")
    private boolean httpOnly;
    @Value("${app.jwt.cookie-secure}")
    private boolean secure;
    @Value("${app.jwt.cookie-same-site}")
    private String sameSite;
    @Value("${app.jwt.cookie-expiration-time}")
    private int expirationTime;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserDTO dto) {
        User user = authService.register(dto);
        LOGGER.info("✅ User with username {} registered successfully", user.getUsername());

        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUserDTO loginRequest, HttpServletResponse response) {
        final String token = authService.login(loginRequest);
        final Cookie cookie = createAuthCookie(token);
        response.addCookie(cookie);

        return ResponseEntity.ok().body(Map.of(
                "message", "Login successful",
                "token", token));
    }

    @PutMapping("/edit")
    public ResponseEntity<User> edit(@Valid @RequestBody EditUserDTO dto) {
        User user = authService.edit(dto);
        LOGGER.info("✅ User with username {} edited successfully", user.getUsername());

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody DeleteUserDTO dto) {
        authService.delete(dto);
        return ResponseEntity.noContent().build();
    }

    private Cookie createAuthCookie(String token) {
        final Cookie cookie = new Cookie(cookieName, token);
        cookie.setPath(ApiConfig.API_BASE_PATH);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", sameSite);
        cookie.setMaxAge(expirationTime * 60);

        return cookie;
    }
}
