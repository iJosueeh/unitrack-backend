package com.unitrack.backend.user.dto;

import com.unitrack.backend.user.enums.JobTitle;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Size(min = 2, max = 60, message = "First name must have between 2 and 60 characters")
    private String firstName;

    @Size(min = 2, max = 60, message = "Last name must have between 2 and 60 characters")
    private String lastName;

    @Email(message = "Email is not valid")
    private String email;

    @Size(max = 400, message = "Bio must have at most 400 characters")
    private String bio;

    @Size(max = 500, message = "Image URL must have at most 500 characters")
    private String imgUrl;

    private JobTitle jobTitle;
}
