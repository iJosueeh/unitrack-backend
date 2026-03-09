package com.unitrack.backend.user.services;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unitrack.backend.auth.dto.RegisterRequest;
import com.unitrack.backend.common.exception.EmailAlreadyRegisteredException;
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

    public UserResponse getUserById(UUID id) {
        log.info("Encontrando usuario con id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario con id {} no encontrado", id);
                    throw new RuntimeException("User invalid");
                });
        log.info("Usuario encontrado: {}", user.getId());
        return mapTUserResponse(user);
    }

    public User createdUser(RegisterRequest request) {
        if (request == null) {
            log.warn("RegisterRequest es nulo");
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User with email {} is already registered", request.getEmail());
            throw new EmailAlreadyRegisteredException("Email is already registered");
        }

        User usuario = new User();
        usuario.setFirstName(request.getFirstName());
        usuario.setLastName(request.getLastName());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setIsActive(true);
        usuario.setRole(SystemRole.USER);
        
        User savedUser = userRepository.save(usuario);
        log.info("Usuario creado con email: {}", savedUser.getEmail());
        return savedUser;
    }

    private UserResponse mapTUserResponse(User body) {
        return UserResponse.builder()
                .id(body.getId())
                .fullName(body.getFirstName().concat(" ").concat(body.getLastName()))
                .email(body.getEmail())
                .role(body.getRole())
                .isActive(body.getIsActive())
                .build();
    };

}
