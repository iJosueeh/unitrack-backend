package com.unitrack.backend.auth.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.dto.AuthResponse;
import com.unitrack.backend.auth.dto.LoginRequest;
import com.unitrack.backend.auth.dto.RegisterRequest;
import com.unitrack.backend.security.jwt.JwtService;
import com.unitrack.backend.user.entity.Profile;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.user.services.ProfileService;
import com.unitrack.backend.user.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ProfileService profileService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher publisher;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    public AuthResponse register(RegisterRequest request) {
        User user = userService.createUser(request);
        Profile profile = profileService.createProfile(user);

        publisher.publishEvent(new ActivityEvent(
                user.getId(),
                ActivityAction.CREATED,
                ActivityEntityType.USERS,
                user.getId()));

        publisher.publishEvent(new ActivityEvent(
                profile.getUser().getId(),
                ActivityAction.CREATED,
                ActivityEntityType.PROFILE,
                profile.getId()));

        log.info("User created with email: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(expiration)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
