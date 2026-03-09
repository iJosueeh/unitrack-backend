package com.unitrack.backend.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.user.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

	Optional<Profile> findByUser_Id(UUID userId);

}
