package com.gabrielluciano.userservice.error;

import com.gabrielluciano.userservice.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .error(ExceptionUtils.getRootCause(ex).getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now(ZoneOffset.UTC).toString())
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error(convertConstraintViolationsToString(ex.getConstraintViolations()))
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now(ZoneOffset.UTC).toString())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error(ex.getMessage())
                        .status(HttpStatus.NOT_FOUND.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now(ZoneOffset.UTC).toString())
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;

        return ResponseEntity.status(statusCode)
                .body(ErrorResponse.builder()
                        .error(ex.getMessage())
                        .status(statusCode.value())
                        .path(servletWebRequest.getRequest().getRequestURI())
                        .timestamp(LocalDateTime.now(ZoneOffset.UTC).toString())
                        .build());
    }

    private String convertConstraintViolationsToString(Set<ConstraintViolation<?>> violations) {
        Function<ConstraintViolation<?>, String> converter = violation ->
                String.format("%s: %s", violation.getPropertyPath(), violation.getMessage());
        String delimiter = ", ";

        return "Constraint Violation(s): " + violations.stream()
                .map(converter).collect(Collectors.joining(delimiter));
    }
}
