package com.unitrack.backend.user.dto;

import java.util.UUID;

import com.unitrack.backend.user.enums.Rol;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private Rol role;
    private Boolean isActive;
}
