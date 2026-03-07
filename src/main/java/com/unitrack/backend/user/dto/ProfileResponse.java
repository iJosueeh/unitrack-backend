package com.unitrack.backend.user.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;

}
