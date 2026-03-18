package com.unitrack.backend.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unitrack.backend.auth.dto.OAuth2UserInfo;
import com.unitrack.backend.auth.dto.GoogleTokenResponse;
import com.unitrack.backend.common.exception.OAuth2Exception;
import com.unitrack.backend.user.enums.OAuth2Provider;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleOAuth2Service implements OAuth2ProviderService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v1/userinfo";

    @Override
    public OAuth2UserInfo getUserInfo(String code, String redirectUri) {
        try {
            // Intercambiar código por token
            GoogleTokenResponse tokenResponse = exchangeCodeForToken(code, redirectUri);
            
            // Obtener información del usuario usando el token
            return fetchUserInfo(tokenResponse.getAccessToken());
        } catch (Exception e) {
            log.error("Error fetching user info from Google: {}", e.getMessage(), e);
            throw new OAuth2Exception("Failed to authenticate with Google", e);
        }
    }

    private GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) throws Exception {
        String body = String.format(
            "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
            code, clientId, clientSecret, redirectUri
        );

        Request request = new Request.Builder()
                .url(TOKEN_ENDPOINT)
                .post(RequestBody.create(body, okhttp3.MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new OAuth2Exception("Failed to exchange authorization code: " + response.body().string());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, GoogleTokenResponse.class);
        }
    }

    private OAuth2UserInfo fetchUserInfo(String accessToken) throws Exception {
        Request request = new Request.Builder()
                .url(USER_INFO_ENDPOINT + "?access_token=" + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new OAuth2Exception("Failed to fetch user info from Google");
            }

            JsonNode jsonNode = objectMapper.readTree(response.body().string());
            
            return OAuth2UserInfo.builder()
                    .email(jsonNode.get("email").asText())
                    .name(jsonNode.get("name").asText())
                    .picture(jsonNode.get("picture").asText())
                    .provider(OAuth2Provider.GOOGLE)
                    .providerId(jsonNode.get("id").asText())
                    .build();
        }
    }
}

