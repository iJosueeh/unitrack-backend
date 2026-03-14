package com.unitrack.backend.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.user.dto.ProfileResponse;
import com.unitrack.backend.user.dto.ProfileUpdateRequest;
import com.unitrack.backend.user.services.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {
        ProfileResponse profile = profileService.getAuthenticatedProfile();
        return ResponseEntity.ok(ApiResponse.<ProfileResponse>builder()
                .success(true)
                .message("Profile retrieved successfully")
                .data(profile)
                .build());
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileResponse updatedProfile = profileService.updateAuthenticatedProfile(request);
        return ResponseEntity.ok(ApiResponse.<ProfileResponse>builder()
                .success(true)
                .message("Profile updated successfully")
                .data(updatedProfile)
                .build());
    }
}
