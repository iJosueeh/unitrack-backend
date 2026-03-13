package com.unitrack.backend.user.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.common.exception.EmailAlreadyRegisteredException;
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

    public ProfileResponse getProfileResponse(UUID id) {
        log.info("Encontrando perfil del usuario con id: {}", id);
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario con id {} no encontrado.", id);
                    throw new IllegalArgumentException("User invalid");
                });
        log.info("Perfil encontrado para el usuario con id: {}", id);
        return mapToProfileResponse(profile);
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

        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile created for user with id: {}", user.getId());
        return savedProfile;
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Profile update request cannot be null");
        }

        Profile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> {
                    log.warn("Profile not found for user with id: {}", userId);
                    throw new IllegalArgumentException("Profile not found");
                });

        User user = profile.getUser();
        log.info("Updating profile for user with id: {}", user.getId());

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getEmail() != null) {
            String newEmail = request.getEmail().trim();

            boolean isDifferentEmail = !newEmail.equalsIgnoreCase(user.getEmail());
            if (isDifferentEmail && userRepository.existsByEmail(newEmail)) {
                log.warn("Email {} is already registered by another user", newEmail);
                throw new EmailAlreadyRegisteredException("Email is already registered");
            }

            user.setEmail(newEmail);
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getImgUrl() != null) {
            profile.setImageUrl(request.getImgUrl());
        }

        if (request.getJobTitle() != null) {
            profile.setJobTitle(request.getJobTitle());
        }

        userRepository.save(user);
        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile updated for user with id: {}", userId);
        return mapToProfileResponse(savedProfile);
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
