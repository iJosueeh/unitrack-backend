package com.unitrack.backend.user.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unitrack.backend.user.dto.UserResponse;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

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
