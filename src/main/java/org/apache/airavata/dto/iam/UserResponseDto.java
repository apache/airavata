package org.apache.airavata.dto.iam;

import org.apache.airavata.iam.model.enums.UserStatus;

public class UserResponseDto {

    private String userId;
    private UserStatus status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
