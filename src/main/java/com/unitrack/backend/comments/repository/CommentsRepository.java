package com.unitrack.backend.comments.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.comments.entity.Comments;

public interface CommentsRepository extends JpaRepository<Comments, UUID> {

}
