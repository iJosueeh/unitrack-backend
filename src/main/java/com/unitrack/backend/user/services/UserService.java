package com.unitrack.backend.user.services;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unitrack.backend.auth.dto.RegisterRequest;
import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.exception.EmailAlreadyRegisteredException;
import com.unitrack.backend.common.exception.NotFoundException;
import com.unitrack.backend.user.dto.UserResponse;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserResponse getAuthenticatedUserResponse() {
        User user = currentUserService.getAuthenticatedUser();
        return mapToUserResponse(user);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return mapToUserResponse(user);
    }

    public User createUser(RegisterRequest request) {
        if (request == null) {
            log.warn("RegisterRequest is null");
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User with email {} is already registered", request.getEmail());
            throw new EmailAlreadyRegisteredException("Email is already registered");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setRole(SystemRole.USER);

        User saved = userRepository.save(user);
        log.info("User created with email: {}", saved.getEmail());
        return saved;
    }

    private UserResponse mapToUserResponse(User body) {
        return UserResponse.builder()
                .id(body.getId())
                .fullName(body.getFirstName() + " " + body.getLastName())
                .email(body.getEmail())
                .role(body.getRole())
                .isActive(body.getIsActive())
                .build();
    }
}
