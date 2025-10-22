package com.example.api_rest.exception.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ErrorResponseFactory {

    private final ObjectMapper objectMapper;

    public ErrorResponseFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Método para los Handlers de Seguridad y Filtros.
     * Escribe la respuesta de error directamente en el HttpServletResponse.
     */
    public void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());

        Map<String, Object> body = buildErrorBody(status, message);

        try (OutputStream out = response.getOutputStream()) {
            objectMapper.writeValue(out, body);
            out.flush();
        }
    }

    /**
     * Método para el @ControllerAdvice.
     * Construye y devuelve el cuerpo del error para ser usado en un ResponseEntity.
     */
    public Map<String, Object> buildErrorBody(HttpStatus status, String message, List<String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", (message == null || message.isBlank()) ? "No message available" : message);
        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }
        body.put("timestamp", LocalDateTime.now().toString());

        return body;
    }

    public Map<String, Object> buildErrorBody(HttpStatus status, String message) {
        return buildErrorBody(status, message, null);
    }
}
