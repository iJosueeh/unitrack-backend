package com.unitrack.backend.attachment.entity;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.tasks.entity.Tasks;
import com.unitrack.backend.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
    @JoinColumn(name = "task_id", nullable = false)
    private Tasks task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_by_id")
    private User uploaderBy;

}
