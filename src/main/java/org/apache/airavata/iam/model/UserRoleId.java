package org.apache.airavata.iam.model;

import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.iam.model.enums.UserRole;

/**
 * Composite primary key for {@link UserRoleEntity}: a user can hold more than one
 * role, but not the same role twice. Field names must match {@code @Id} fields on
 * the entity ({@code userId}, {@code role}) for JPA's {@code @IdClass} contract.
 */
public class UserRoleId implements Serializable {

    private String userId;
    private UserRole role;

    public UserRoleId() {}

    public UserRoleId(String userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId that)) return false;
        return Objects.equals(userId, that.userId) && role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, role);
    }
}
