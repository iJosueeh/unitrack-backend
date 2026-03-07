package com.unitrack.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 60, message = "First name must have between 3 and 60 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 60, message = "Last name must have between 3 and 60 characters")
    private String lastName;

    @Email(message = "Email is not valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must have between 8 and 72 characters")
    private String password;
}
