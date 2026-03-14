package com.unitrack.backend.user.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.unitrack.backend.common.exception.EmailAlreadyRegisteredException;
import com.unitrack.backend.common.exception.GlobalExceptionHandler;
import com.unitrack.backend.user.dto.ProfileResponse;
import com.unitrack.backend.user.dto.UserResponse;
import com.unitrack.backend.user.enums.JobTitle;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.services.ProfileService;
import com.unitrack.backend.user.services.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllersSmokeTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserController(userService),
                new ProfileController(profileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyUser_ShouldReturn200() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .role(SystemRole.USER)
                .isActive(true)
                .build();

        when(userService.getAuthenticatedUserResponse()).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void getMyUser_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        doThrow(new AuthenticationCredentialsNotFoundException("Authenticated user is required"))
                .when(userService).getAuthenticatedUserResponse();

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMyProfile_ShouldReturn200() throws Exception {
        ProfileResponse response = ProfileResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("bio")
                .imgUrl("img")
                .jobTitle(JobTitle.SENIOR_DEVELOPER)
                .build();

        when(profileService.getAuthenticatedProfile()).thenReturn(response);

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void updateMyProfile_WithInvalidBody_ShouldReturn400() throws Exception {
        String invalidBody = """
                {
                  "firstName": "J",
                  "email": "invalid"
                }
                """;

        mockMvc.perform(patch("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateMyProfile_WhenEmailAlreadyRegistered_ShouldReturn409() throws Exception {
        String validBody = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john@example.com"
                }
                """;

        doThrow(new EmailAlreadyRegisteredException("Email is already registered"))
                .when(profileService).updateAuthenticatedProfile(org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateMyProfile_WithValidBody_ShouldReturn200() throws Exception {
        String validBody = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john@example.com",
                  "bio": "new bio",
                  "imgUrl": "new-url",
                  "jobTitle": "SENIOR_DEVELOPER"
                }
                """;

        ProfileResponse response = ProfileResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("new bio")
                .imgUrl("new-url")
                .jobTitle(JobTitle.SENIOR_DEVELOPER)
                .build();

        when(profileService.updateAuthenticatedProfile(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(patch("/api/profile/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobTitle").value("SENIOR_DEVELOPER"));
    }
}
