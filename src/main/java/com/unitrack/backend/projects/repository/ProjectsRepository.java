package com.unitrack.backend.projects.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.projects.entity.Projects;

public interface ProjectsRepository extends JpaRepository<Projects, UUID> {

}
