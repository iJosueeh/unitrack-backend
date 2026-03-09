package com.unitrack.backend.user.dto;

import java.util.UUID;

import com.unitrack.backend.user.enums.JobTitle;

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
    private String imgUrl;
    private JobTitle jobTitle;

}
