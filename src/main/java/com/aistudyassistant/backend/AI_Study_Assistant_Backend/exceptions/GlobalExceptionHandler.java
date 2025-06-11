package com.aistudyassistant.backend.AI_Study_Assistant_Backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", statusCode.value());
        errorBody.put("error", (status != null) ? status.getReasonPhrase() : "Unknown Error");
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", ex.getReason());

        return new ResponseEntity<>(errorBody, statusCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", "Validation failed");
        errorBody.put("fieldErrors", fieldErrors);

        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", "Invalid JSON format. Please check your request body format");

        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", ex.getMessage());

        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", "Data integrity violation - duplicate or invalid data");

        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", "Authentication failed");

        return new ResponseEntity<>(errorBody, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", ZonedDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("path", request.getRequestURI());
        errorBody.put("message", "An unexpected error occurred");

        // Log the actual exception for debugging (consider using proper logging)
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return new ResponseEntity<>(errorBody, status);
    }
}