package com.unitrack.backend.user.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.exception.EmailAlreadyRegisteredException;
import com.unitrack.backend.common.exception.NotFoundException;
import com.unitrack.backend.user.dto.ProfileResponse;
import com.unitrack.backend.user.dto.ProfileUpdateRequest;
import com.unitrack.backend.user.entity.Profile;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.JobTitle;
import com.unitrack.backend.user.repository.ProfileRepository;
import com.unitrack.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public ProfileResponse getAuthenticatedProfile() {
        User user = currentUserService.getAuthenticatedUser();
        Profile profile = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Profile not found"));
        log.info("Profile retrieved for user {}", user.getId());
        return mapToProfileResponse(profile);
    }

    @Transactional
    public ProfileResponse updateAuthenticatedProfile(ProfileUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Profile update request cannot be null");
        }

        User user = currentUserService.getAuthenticatedUser();
        Profile profile = profileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        log.info("Updating profile for user {}", user.getId());

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        if (request.getEmail() != null) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                log.warn("Email {} is already registered by another user", newEmail);
                throw new EmailAlreadyRegisteredException("Email is already registered");
            }
            user.setEmail(newEmail);
        }

        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getImgUrl() != null) profile.setImageUrl(request.getImgUrl());
        if (request.getJobTitle() != null) profile.setJobTitle(request.getJobTitle());

        userRepository.save(user);
        Profile saved = profileRepository.save(profile);
        log.info("Profile updated for user {}", user.getId());
        return mapToProfileResponse(saved);
    }

    public Profile createProfile(User user) {
        if (user == null) {
            log.warn("User is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setBio(null);
        profile.setImageUrl(null);
        profile.setJobTitle(JobTitle.NONE);

        Profile saved = profileRepository.save(profile);
        log.info("Profile created for user {}", user.getId());
        return saved;
    }

    private ProfileResponse mapToProfileResponse(Profile body) {
        return ProfileResponse.builder()
                .id(body.getId())
                .firstName(body.getUser().getFirstName())
                .lastName(body.getUser().getLastName())
                .email(body.getUser().getEmail())
                .bio(body.getBio())
                .imgUrl(body.getImageUrl())
                .jobTitle(body.getJobTitle())
                .build();
    }
}
