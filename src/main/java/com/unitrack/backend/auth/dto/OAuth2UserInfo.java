package com.unitrack.backend.auth.dto;

import com.unitrack.backend.user.enums.OAuth2Provider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuth2UserInfo {
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String picture;
    private OAuth2Provider provider;
    private String providerId;
}

