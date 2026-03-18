package com.unitrack.backend.auth.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.unitrack.backend.auth.dto.OAuth2UserInfo;
import com.unitrack.backend.user.entity.Profile;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.OAuth2Provider;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.user.services.ProfileService;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    private OAuth2UserInfo oauth2UserInfo;

    @BeforeEach
    void setUp() {
        oauth2UserInfo = OAuth2UserInfo.builder()
                .email("test@google.com")
                .name("John Doe")
                .firstName("John")
                .lastName("Doe")
                .picture("https://example.com/photo.jpg")
                .provider(OAuth2Provider.GOOGLE)
                .providerId("google-id-123")
                .build();
    }

    @Test
    void getOrCreateUser_WithNewUser_ShouldCreateUser() {
        // Arrange
        when(userRepository.findByEmail(oauth2UserInfo.getEmail()))
                .thenReturn(Optional.empty());
        
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail(oauth2UserInfo.getEmail());
        newUser.setFirstName(oauth2UserInfo.getFirstName());
        newUser.setLastName(oauth2UserInfo.getLastName());
        newUser.setOAuth2Provider(OAuth2Provider.GOOGLE);
        newUser.setOAuth2ProviderId(oauth2UserInfo.getProviderId());
        newUser.setRole(SystemRole.USER);
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        
        // Mock profile con user asignado
        Profile profile = new Profile();
        profile.setUser(newUser);
        when(profileService.createProfile(any(User.class))).thenReturn(profile);

        // Act
        User result = oAuth2UserService.getOrCreateUser(oauth2UserInfo);

        // Assert
        assertNotNull(result);
        assertEquals(oauth2UserInfo.getEmail(), result.getEmail());
        assertEquals(OAuth2Provider.GOOGLE, result.getOAuth2Provider());
    }

    @Test
    void getOrCreateUser_WithExistingUser_ShouldReturnExistingUser() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail(oauth2UserInfo.getEmail());
        existingUser.setOAuth2Provider(OAuth2Provider.GOOGLE);
        existingUser.setOAuth2ProviderId(oauth2UserInfo.getProviderId());

        when(userRepository.findByEmail(oauth2UserInfo.getEmail()))
                .thenReturn(Optional.of(existingUser));

        // Act
        User result = oAuth2UserService.getOrCreateUser(oauth2UserInfo);

        // Assert
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
    }
}

