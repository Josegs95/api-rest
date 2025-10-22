package com.example.api_rest.exception.handler;

import com.example.api_rest.exception.NotFoundException;
import com.example.api_rest.exception.UsernameAlreadyExistsException;
import com.example.api_rest.exception.utils.ErrorResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalApiExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    // 400 - Bad Request
    @ExceptionHandler({UsernameAlreadyExistsException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException e) {
        LOGGER.warn("⚠️ {}", e.getMessage());

        Map<String, Object> body = errorResponseFactory.buildErrorBody(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 400 - Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        Map<String, Object> body = errorResponseFactory.buildErrorBody(HttpStatus.BAD_REQUEST, "Validation error", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 401 - Unauthorized
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        Map<String, Object> body = errorResponseFactory.buildErrorBody(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 404 - Not Found
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVideoGameNotFound(NotFoundException e) {
        LOGGER.warn("⚠️ {}", e.getMessage());

        Map<String, Object> body = errorResponseFactory.buildErrorBody(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 500 - Internal Server Error (Catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleInternalServerError(Exception e) {
        LOGGER.error("An unexpected error occurred: ", e);

        String errorMessage = "An unexpected internal server error occurred. Please try again later.";
        Map<String, Object> body = errorResponseFactory.buildErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}