package com.unitrack.backend.common.exception;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.unitrack.backend.common.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(EmailAlreadyRegisteredException.class)
        public ResponseEntity<?> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler({ AuthenticationCredentialsNotFoundException.class, UsernameNotFoundException.class })
        public ResponseEntity<?> handleUnauthorized(RuntimeException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
                Map<String, String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                FieldError::getField,
                                                FieldError::getDefaultMessage,
                                                (first, second) -> second));
                return ResponseEntity.badRequest().body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message("Validation failed")
                                                .data(errors)
                                                .build());
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<?> handleRuntime(RuntimeException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleException(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                ApiResponse.builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .data(null)
                                                .build());
        }
}
