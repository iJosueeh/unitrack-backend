package com.unitrack.backend.auth.services;

import com.unitrack.backend.auth.dto.OAuth2UserInfo;

/**
 * Interfaz para proveedores OAuth2
 */
public interface OAuth2ProviderService {
    
    /**
     * Valida el código de autorización y extrae información del usuario
     */
    OAuth2UserInfo getUserInfo(String code, String redirectUri);
}

