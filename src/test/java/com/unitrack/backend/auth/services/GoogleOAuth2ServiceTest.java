package com.unitrack.backend.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unitrack.backend.auth.dto.OAuth2UserInfo;
import com.unitrack.backend.common.exception.OAuth2Exception;
import com.unitrack.backend.user.enums.OAuth2Provider;

import okhttp3.OkHttpClient;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2ServiceTest {

    @Mock
    private OkHttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GoogleOAuth2Service googleOAuth2Service;

    @BeforeEach
    void setUp() {
        // Inyectar valores de configuración si es necesario
    }

    @Test
    void getUserInfo_WithValidCode_ShouldReturnOAuth2UserInfo() {
        // Arrange
        String code = "valid-code";
        String redirectUri = "http://localhost:8080/api/auth/oauth2/callback";

        // Act & Assert - Este test requeriría mockear las llamadas HTTP
        // Por ahora es un placeholder para la estructura
    }

    @Test
    void getUserInfo_WithInvalidCode_ShouldThrowOAuth2Exception() {
        // Arrange
        String code = "invalid-code";
        String redirectUri = "http://localhost:8080/api/auth/oauth2/callback";

        // Act & Assert
        assertThrows(OAuth2Exception.class, () -> 
            googleOAuth2Service.getUserInfo(code, redirectUri)
        );
    }
}

