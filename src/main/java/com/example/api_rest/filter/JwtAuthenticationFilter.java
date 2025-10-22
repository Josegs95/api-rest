package com.example.api_rest.filter;

import com.example.api_rest.config.SecurityConfig;
import com.example.api_rest.exception.utils.ErrorResponseFactory;
import com.example.api_rest.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AuthService authService;
    private final ErrorResponseFactory errorResponseFactory;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    public JwtAuthenticationFilter(AuthService authService, ErrorResponseFactory errorResponseFactory) {
        this.authService = authService;
        this.errorResponseFactory = errorResponseFactory;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String requestUri = request.getRequestURI();

        return  requestUri.startsWith("/docs") ||
                requestUri.contains("swagger") ||
                requestUri.equals(SecurityConfig.API_BASE_AUTH + "/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final Optional<String> token = getJwtFromRequest(request);

        if (token.isEmpty() || !authService.validateToken(token.get())) {
            errorResponseFactory.writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        String username = authService.getUserFromToken(token.get());
        List<GrantedAuthority> authorities = authService.getAuthoritiesFromToken(token.get());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return (Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst());
    }
}
