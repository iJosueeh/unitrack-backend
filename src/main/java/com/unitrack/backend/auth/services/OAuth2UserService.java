package com.unitrack.backend.auth.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.dto.OAuth2UserInfo;
import com.unitrack.backend.common.exception.NotFoundException;
import com.unitrack.backend.user.entity.Profile;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.enums.OAuth2Provider;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.repository.ProfileRepository;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.user.services.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para manejar autenticación OAuth2
 * Busca usuarios existentes por email o crea nuevos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserService {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final ApplicationEventPublisher publisher;

    /**
     * Obtiene o crea un usuario basado en información OAuth2
     * Si el usuario existe con ese email, se actualiza con info del proveedor
     * Si no existe, se crea uno nuevo
     */
    @Transactional
    public User getOrCreateUser(OAuth2UserInfo oAuth2UserInfo) {
        return userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .map(user -> updateUserWithOAuth2Info(user, oAuth2UserInfo))
                .orElseGet(() -> createUserFromOAuth2(oAuth2UserInfo));
    }

    /**
     * Actualiza un usuario existente con información de OAuth2
     */
    private User updateUserWithOAuth2Info(User user, OAuth2UserInfo oAuth2UserInfo) {
        // Si el usuario ya tiene este proveedor configurado, no hacer nada
        if (user.getOAuth2Provider() == oAuth2UserInfo.getProvider()
                && user.getOAuth2ProviderId().equals(oAuth2UserInfo.getProviderId())) {
            log.debug("User already linked to this OAuth2 provider: {}", oAuth2UserInfo.getProvider());
            return user;
        }

        // Si el usuario no tenía proveedor OAuth2, asignar uno
        if (user.getOAuth2Provider() == null) {
            user.setOAuth2Provider(oAuth2UserInfo.getProvider());
            user.setOAuth2ProviderId(oAuth2UserInfo.getProviderId());
            User updated = userRepository.save(user);
            log.info("User {} linked to OAuth2 provider: {}", user.getEmail(), oAuth2UserInfo.getProvider());
            return updated;
        }

        // Si el usuario tenía otro proveedor, lanzar excepción
        throw new IllegalStateException(
                "User is already linked to " + user.getOAuth2Provider() +
                        ". Multiple OAuth2 providers per user not yet supported");
    }

    /**
     * Crea un nuevo usuario a partir de información OAuth2
     */
    private User createUserFromOAuth2(OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setEmail(oAuth2UserInfo.getEmail());
        
        // Parsear nombre
        String firstName = oAuth2UserInfo.getFirstName();
        String lastName = oAuth2UserInfo.getLastName();
        
        if (firstName == null || firstName.isEmpty()) {
            // Dividir el nombre completo si no hay nombre/apellido separado
            String[] nameParts = oAuth2UserInfo.getName().split(" ", 2);
            firstName = nameParts.length > 0 ? nameParts[0] : "User";
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(null); // Sin contraseña para usuarios OAuth2
        user.setRole(SystemRole.USER);
        user.setIsActive(true);
        user.setOAuth2Provider(oAuth2UserInfo.getProvider());
        user.setOAuth2ProviderId(oAuth2UserInfo.getProviderId());

        User savedUser = userRepository.save(user);
        log.info("New user created via OAuth2 ({}): {}", oAuth2UserInfo.getProvider(), savedUser.getEmail());

        // Crear perfil automático
        Profile profile = profileService.createProfile(savedUser);

        // Publicar eventos de actividad
        publisher.publishEvent(new ActivityEvent(
                savedUser.getId(),
                ActivityAction.CREATED,
                ActivityEntityType.USERS,
                savedUser.getId()));

        publisher.publishEvent(new ActivityEvent(
                profile.getUser().getId(),
                ActivityAction.CREATED,
                ActivityEntityType.PROFILE,
                profile.getId()));

        return savedUser;
    }
}

