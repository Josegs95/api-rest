package com.example.api_rest.exception.handler;

import com.example.api_rest.exception.utils.ErrorResponseFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Autowired
    public ErrorResponseFactory errorResponseFactory;

    // AuthenticationEntryPoint: se activa cuando un usuario no autenticado intenta acceder a un recurso protegido (devuelve 401)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        errorResponseFactory.writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Valid authentication token is required");
    }

    // AccessDeniedHandler: se activa cuando un usuario autenticado no tiene los permisos necesarios (devuelve 403)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        errorResponseFactory.writeErrorResponse(response, HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
    }
}
