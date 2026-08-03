package org.apache.airavata.iam.model;

import org.apache.airavata.iam.model.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "user_roles")
@IdClass(UserRoleId.class)
public class UserRoleEntity {

    @Id
    private String userId; // Unique identifier for the user

    @Id
    @Enumerated(EnumType.STRING) // stored by name, not ordinal, since this is now part of the primary key
    private UserRole role; // Role assigned to the user (SUPER_ADMIN, ADMIN, USER)

    // Read-only mapping of the same userId column, purely so UserEntity's
    // @OneToMany(mappedBy = "user") can cascade deletes onto this entity.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserEntity user;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
