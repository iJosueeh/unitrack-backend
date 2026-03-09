package com.unitrack.backend.tasks.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.tasks.entity.Tasks;

public interface TaskRepository extends JpaRepository<Tasks, UUID> {

}
