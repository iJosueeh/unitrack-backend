package com.unitrack.backend.activity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.activity.entity.Activity;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

}
