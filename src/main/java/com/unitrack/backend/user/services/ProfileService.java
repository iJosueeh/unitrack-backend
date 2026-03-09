package com.unitrack.backend.user.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unitrack.backend.user.dto.ProfileResponse;
import com.unitrack.backend.user.entity.Profile;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.JobTitle;
import com.unitrack.backend.user.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponse getProfileResponse(UUID id) {
        log.info("Encontrando perfil del usuario con id: {}", id);
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario con id {} no encontrado.", id);
                    throw new RuntimeException("User invalid");
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
