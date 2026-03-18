package com.unitrack.backend.auth.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.dto.AuthResponse;
import com.unitrack.backend.auth.dto.LoginRequest;
import com.unitrack.backend.auth.dto.RegisterRequest;
import com.unitrack.backend.security.jwt.JwtService;
import com.unitrack.backend.user.entity.Profile;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.user.services.ProfileService;
import com.unitrack.backend.user.services.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProfileService profileService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expiration", 900000L);
    }

    @Test
    void register_ShouldPublishUsersCreatedEvent_WhenRequestIsValid() {
        UUID userId = UUID.randomUUID();

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password123");

        User user = new User();
        user.setId(userId);
        user.setEmail("john@example.com");
        user.setRole(SystemRole.USER);

        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUser(user);

        when(userService.createUser(request)).thenReturn(user);
        when(profileService.createProfile(user)).thenReturn(profile);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(userId, response.getUserId());
        assertEquals("john@example.com", response.getEmail());

        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publishEvent(captor.capture());
        ActivityEvent event = captor.getValue();

        assertEquals(userId, event.getUserId());
        assertEquals(ActivityAction.CREATED, event.getAction());
        assertEquals(ActivityEntityType.USERS, event.getEntityType());
        assertEquals(userId, event.getEntityId());
    }

    @Test
    void login_ShouldReturnAuthResponse_AndNotPublishActivityEvent() {
        String email = "john@example.com";
        String password = "Password123";
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setRole(SystemRole.USER);

        when(authenticationManager.authenticate(any())).thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(userId, response.getUserId());
        verify(publisher, never()).publishEvent(any(ActivityEvent.class));
    }

    @Test
    void login_ShouldThrow_WhenUserNotFoundAfterAuthentication() {
        String email = "missing@example.com";

        when(authenticationManager.authenticate(any())).thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("Password123");

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));

        verify(publisher, never()).publishEvent(any(ActivityEvent.class));
    }

    @Test
    void login_ShouldThrow_WhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(userRepository, never()).findByEmail(any());
        verify(publisher, never()).publishEvent(any(ActivityEvent.class));
    }
}


