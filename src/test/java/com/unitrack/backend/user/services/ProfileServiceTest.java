package com.unitrack.backend.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
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

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getAuthenticatedProfile_ShouldThrow_WhenProfileNotFound() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(user);
        when(profileRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> profileService.getAuthenticatedProfile());
    }

    @Test
    void updateAuthenticatedProfile_ShouldThrow_WhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> profileService.updateAuthenticatedProfile(null));
    }

    @Test
    void updateAuthenticatedProfile_ShouldThrow_WhenProfileNotFound() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(user);
        when(profileRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> profileService.updateAuthenticatedProfile(new ProfileUpdateRequest()));
    }

    @Test
    void updateAuthenticatedProfile_ShouldThrow_WhenEmailBelongsToAnotherUser() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");
        user.setFirstName("Old");
        user.setLastName("Name");

        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        profile.setBio("old bio");
        profile.setImageUrl("old-url");
        profile.setJobTitle(JobTitle.NONE);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setEmail("new@example.com");

        when(currentUserService.getAuthenticatedUser()).thenReturn(user);
        when(profileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> profileService.updateAuthenticatedProfile(request));

        verify(publisher, never()).publishEvent(any(ActivityEvent.class));
    }

    @Test
    void updateAuthenticatedProfile_ShouldUpdateAndReturnResponse_WhenRequestIsValid() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("old@example.com");
        user.setFirstName("Old");
        user.setLastName("Name");

        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);
        profile.setBio("old bio");
        profile.setImageUrl("old-url");
        profile.setJobTitle(JobTitle.NONE);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setBio("new bio");
        request.setImgUrl("new-url");
        request.setJobTitle(JobTitle.SENIOR_DEVELOPER);

        when(currentUserService.getAuthenticatedUser()).thenReturn(user);
        when(profileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        ProfileResponse response = profileService.updateAuthenticatedProfile(request);

        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("new bio", response.getBio());
        assertEquals("new-url", response.getImgUrl());
        assertEquals(JobTitle.SENIOR_DEVELOPER, response.getJobTitle());

        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publishEvent(captor.capture());
        ActivityEvent event = captor.getValue();

        assertEquals(userId, event.getUserId());
        assertEquals(ActivityAction.UPDATED, event.getAction());
        assertEquals(ActivityEntityType.PROFILE, event.getEntityType());
        assertEquals(profile.getId(), event.getEntityId());
    }

    @Test
    void createProfile_ShouldPublishCreatedEvent_WhenUserIsValid() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> {
            Profile saved = i.getArgument(0);
            saved.setId(profileId);
            return saved;
        });

        Profile saved = profileService.createProfile(user);
        assertEquals(profileId, saved.getId());

        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publishEvent(captor.capture());
        ActivityEvent event = captor.getValue();

        assertEquals(userId, event.getUserId());
        assertEquals(ActivityAction.CREATED, event.getAction());
        assertEquals(ActivityEntityType.PROFILE, event.getEntityType());
        assertEquals(profileId, event.getEntityId());
    }
}
