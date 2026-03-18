package com.unitrack.backend.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unitrack.backend.auth.dto.AuthResponse;
import com.unitrack.backend.auth.dto.LoginRequest;
import com.unitrack.backend.auth.dto.OAuth2CallbackRequest;
import com.unitrack.backend.auth.dto.RegisterRequest;
import com.unitrack.backend.auth.services.AuthService;
import com.unitrack.backend.auth.services.OAuth2AuthService;
import com.unitrack.backend.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2AuthService oAuth2AuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(authResponse)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authResponse)
                .build());
    }

    @PostMapping("/oauth2/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> oAuth2Callback(
            @Valid @RequestBody OAuth2CallbackRequest request) {
        AuthResponse authResponse = oAuth2AuthService.handleOAuth2Callback(request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("OAuth2 authentication successful")
                .data(authResponse)
                .build());
    }
}
