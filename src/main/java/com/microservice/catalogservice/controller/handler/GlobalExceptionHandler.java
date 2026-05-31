package com.microservice.catalogservice.controller.handler;

import com.microservice.catalogservice.application.exceptions.CloudFrontException;
import com.microservice.catalogservice.application.exceptions.PostgreSqlException;
import com.microservice.catalogservice.application.exceptions.VideoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<StandardError> handleVideoNotFoundException(VideoNotFoundException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CloudFrontException.class)
    public ResponseEntity<StandardError> handleCloudFrontException(CloudFrontException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(PostgreSqlException.class)
    public ResponseEntity<StandardError> handlePostgreSqlException(PostgreSqlException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidationErrors(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce("", (acc, error) -> acc + error + "; ");

        var response = StandardError.builder()
                .error("Error validating: " + errorMessage)
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
}
