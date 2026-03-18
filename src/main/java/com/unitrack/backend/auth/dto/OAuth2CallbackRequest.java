package com.unitrack.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2CallbackRequest {
    private String code;
    private String provider; // "google" o "microsoft"
    @JsonProperty("redirectUri")
    private String redirectUri;
}

