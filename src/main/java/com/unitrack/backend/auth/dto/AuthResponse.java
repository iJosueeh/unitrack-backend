package com.unitrack.backend.auth.dto;

import java.util.UUID;

import com.unitrack.backend.user.enums.SystemRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UUID userId;
    private String email;
    private SystemRole role;
}
