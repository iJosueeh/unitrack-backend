package com.unitrack.backend.user.entity;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.user.enums.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends BaseEntity {

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @Enumerated(EnumType.STRING)
    private Rol role;

    private Boolean isActive;
}
