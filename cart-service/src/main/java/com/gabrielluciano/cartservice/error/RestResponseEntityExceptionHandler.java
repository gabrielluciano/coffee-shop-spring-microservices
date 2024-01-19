package com.gabrielluciano.cartservice.error;

import com.gabrielluciano.cartservice.exception.CartNotFoundException;
import com.gabrielluciano.cartservice.exception.ProductNotFoundException;
import com.gabrielluciano.cartservice.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
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

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class, CartNotFoundException.class})
    protected ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

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
}
