package com.unitrack.backend.user.controllers;

import static org.mockito.ArgumentMatchers.any;
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
    void getUserById_ShouldReturn200() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .role(SystemRole.USER)
                .isActive(true)
                .build();

        when(userService.getUserById(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get("/api/usuarios/{id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void getUserById_WhenServiceThrowsIllegalArgument_ShouldReturn400() throws Exception {
        doThrow(new IllegalArgumentException("User invalid"))
                .when(userService)
                .getUserById(any(UUID.class));

        mockMvc.perform(get("/api/usuarios/{id}", UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getProfile_ShouldReturn200() throws Exception {
        ProfileResponse response = ProfileResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("bio")
                .imgUrl("img")
                .jobTitle(JobTitle.SENIOR_DEVELOPER)
                .build();

        when(profileService.getProfileResponse(any(UUID.class))).thenReturn(response);

        mockMvc.perform(get("/api/perfil/{id}", UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void updateProfile_WithInvalidBody_ShouldReturn400() throws Exception {
        String invalidBody = """
                {
                  "firstName": "J",
                  "email": "invalid"
                }
                """;

        mockMvc.perform(patch("/api/perfil/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateProfile_WhenEmailAlreadyRegistered_ShouldReturn409() throws Exception {
        String validBody = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john@example.com"
                }
                """;

        doThrow(new EmailAlreadyRegisteredException("Email is already registered"))
                .when(profileService)
                .updateProfile(any(UUID.class), any());

        mockMvc.perform(patch("/api/perfil/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateProfile_WithValidBody_ShouldReturn200() throws Exception {
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

        when(profileService.updateProfile(any(UUID.class), any())).thenReturn(response);

        mockMvc.perform(patch("/api/perfil/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobTitle").value("SENIOR_DEVELOPER"));
    }
}
