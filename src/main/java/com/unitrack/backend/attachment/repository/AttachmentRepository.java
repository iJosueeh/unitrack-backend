package com.unitrack.backend.attachment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.attachment.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

}
