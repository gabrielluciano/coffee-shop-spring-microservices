package com.gabrielluciano.authorizationserver.error;

import com.gabrielluciano.authorizationserver.exception.UserRegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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

    @ExceptionHandler(UserRegistrationException.class)
    protected ResponseEntity<ErrorResponse> handleUserRegistrationException(
            UserRegistrationException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .error("Error completing the registration. Please try again later.")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now(ZoneOffset.UTC).toString())
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error(convertFieldErrorsToString(ex.getBindingResult().getFieldErrors()))
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(servletWebRequest.getRequest().getRequestURI())
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

    private String convertFieldErrorsToString(List<FieldError> fieldErrors) {
        Function<FieldError, String> converter = fieldError ->
                String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage());
        String delimiter = ", ";

        return "Constraint Violation(s): " + fieldErrors.stream()
                .map(converter).collect(Collectors.joining(delimiter));
    }
}
