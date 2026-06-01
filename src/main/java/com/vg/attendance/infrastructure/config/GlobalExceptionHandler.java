package com.vg.attendance.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return Mono.just(response);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Error interno del servidor: " + ex.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Mono.just(response);
    }
}
