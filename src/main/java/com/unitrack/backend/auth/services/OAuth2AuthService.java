package com.unitrack.backend.auth.services;

import org.springframework.stereotype.Service;

import com.unitrack.backend.auth.dto.AuthResponse;
import com.unitrack.backend.auth.dto.OAuth2CallbackRequest;
import com.unitrack.backend.auth.dto.OAuth2UserInfo;
import com.unitrack.backend.common.exception.OAuth2Exception;
import com.unitrack.backend.security.jwt.JwtService;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.OAuth2Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthService {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final MicrosoftOAuth2Service microsoftOAuth2Service;
    private final OAuth2UserService oAuth2UserService;
    private final JwtService jwtService;

    private final long JWT_EXPIRATION = 900000; // 15 minutos

    /**
     * Maneja el callback de OAuth2
     * Intercambia el código por información del usuario y emite JWT
     */
    public AuthResponse handleOAuth2Callback(OAuth2CallbackRequest request) {
        try {
            // Validar provider
            OAuth2Provider provider = parseProvider(request.getProvider());

            // Obtener información del usuario del proveedor
            OAuth2UserInfo userInfo = fetchUserInfoFromProvider(provider, request.getCode(), request.getRedirectUri());

            // Buscar o crear usuario local
            User user = oAuth2UserService.getOrCreateUser(userInfo);

            // Generar JWT
            return buildAuthResponse(user);
        } catch (Exception e) {
            log.error("OAuth2 callback failed: {}", e.getMessage(), e);
            throw new OAuth2Exception("Authentication failed", e);
        }
    }

    /**
     * Obtiene información del usuario del proveedor correspondiente
     */
    private OAuth2UserInfo fetchUserInfoFromProvider(OAuth2Provider provider, String code, String redirectUri) {
        return switch (provider) {
            case GOOGLE -> googleOAuth2Service.getUserInfo(code, redirectUri);
            case MICROSOFT -> microsoftOAuth2Service.getUserInfo(code, redirectUri);
            default -> throw new OAuth2Exception("Unsupported OAuth2 provider: " + provider);
        };
    }

    /**
     * Convierte string de provider a enum
     */
    private OAuth2Provider parseProvider(String providerString) {
        try {
            return OAuth2Provider.valueOf(providerString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OAuth2Exception("Invalid OAuth2 provider: " + providerString);
        }
    }

    /**
     * Construye la respuesta de autenticación con JWT
     */
    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(JWT_EXPIRATION)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

