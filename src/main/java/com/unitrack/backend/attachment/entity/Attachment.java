package com.unitrack.backend.attachment.entity;

import org.springframework.scheduling.config.Task;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Attachment extends BaseEntity {

    private String fileUrl;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    private User uploaderBy;

}
