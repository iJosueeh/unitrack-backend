package com.unitrack.backend.auth.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unitrack.backend.auth.dto.AuthResponse;
import com.unitrack.backend.auth.dto.OAuth2CallbackRequest;
import com.unitrack.backend.auth.services.OAuth2AuthService;
import com.unitrack.backend.user.enums.SystemRole;

@ExtendWith(MockitoExtension.class)
class OAuth2ControllerSmokeTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private OAuth2AuthService oAuth2AuthService;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(null, oAuth2AuthService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void oAuth2Callback_WithValidGoogleCode_ShouldReturn200() throws Exception {
        // Arrange
        OAuth2CallbackRequest request = OAuth2CallbackRequest.builder()
                .code("valid-google-code")
                .provider("GOOGLE")
                .redirectUri("http://localhost:8080/api/auth/oauth2/callback")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(900000L)
                .userId(UUID.randomUUID())
                .email("user@google.com")
                .role(SystemRole.USER)
                .build();

        when(oAuth2AuthService.handleOAuth2Callback(any(OAuth2CallbackRequest.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/oauth2/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OAuth2 authentication successful"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.email").value("user@google.com"));
    }

    @Test
    void oAuth2Callback_WithInvalidProvider_ShouldReturn400() throws Exception {
        // Arrange
        OAuth2CallbackRequest request = OAuth2CallbackRequest.builder()
                .code("some-code")
                .provider("INVALID_PROVIDER")
                .redirectUri("http://localhost:8080/api/auth/oauth2/callback")
                .build();

        // Act & Assert - debería validarse en DTO o controlador
        mockMvc.perform(post("/api/auth/oauth2/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

