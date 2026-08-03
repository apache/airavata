package org.apache.airavata.iam.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import org.apache.airavata.iam.model.enums.AuthMethod;
import org.apache.airavata.iam.model.enums.UserStatus;

@Entity(name = "users")
public class UserEntity {

    @Id
    private String userId; // Unique identifier for the user. CILogon users will have a CILogon subject
                           // identifier, while system users will have a UUID.

    @Enumerated(EnumType.STRING)
    private AuthMethod authMethod; // CILogon, System

    private String email;
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserStatus status; // Active, Inactive, Suspended

    private long createdAt; // Timestamp of user creation

    // Deleting a user deletes their role rows too (cascade + orphanRemoval), rather
    // than relying on every caller to remember to clean up user_roles separately.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserRoleEntity> roles = new ArrayList<>();

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<UserRoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRoleEntity> roles) {
        this.roles = roles;
    }
}
