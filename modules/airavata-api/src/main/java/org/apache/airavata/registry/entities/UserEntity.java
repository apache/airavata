/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * User entity storing only OpenID Connect scope fields.
 *
 * <p>This entity stores the minimal user data from OIDC claims:
 * <ul>
 *   <li><b>openid scope:</b> sub (subject identifier)</li>
 *   <li><b>profile scope:</b> given_name, family_name, preferred_username, picture, zoneinfo, locale</li>
 *   <li><b>email scope:</b> email, email_verified</li>
 * </ul>
 *
 * <p>Additional user profile data (phone, address, custom attributes) should be fetched
 * directly from the identity provider (Keycloak) when needed.
 *
 * <p>Primary key format: {@code sub@gatewayId} (airavataInternalUserId)
 */
@Entity(name = "UserEntity")
@Table(
        name = "AIRAVATA_USER",
        indexes = {
            @Index(name = "idx_user_sub", columnList = "SUB"),
            @Index(name = "idx_user_gateway_id", columnList = "GATEWAY_ID"),
            @Index(name = "idx_user_sub_gateway_id", columnList = "SUB, GATEWAY_ID"),
            @Index(name = "idx_user_email", columnList = "EMAIL")
        })
public class UserEntity {

    // ==================== Primary Key ====================

    /**
     * Primary key in format: sub@gatewayId
     * This ensures uniqueness across gateways while providing a single-column PK.
     */
    @Id
    @Column(name = "AIRAVATA_INTERNAL_USER_ID", nullable = false, length = 512)
    private String airavataInternalUserId;

    // ==================== OIDC 'openid' scope ====================

    /**
     * OIDC subject identifier (sub claim).
     * This is the unique identifier for the user from the identity provider.
     */
    @Column(name = "SUB", nullable = false, length = 255)
    private String sub;

    /**
     * Gateway/domain identifier.
     * Provides multi-tenant context for the user.
     */
    @Column(name = "GATEWAY_ID", nullable = false, length = 255)
    private String gatewayId;

    // ==================== OIDC 'profile' scope ====================

    /**
     * OIDC given_name claim.
     * The user's first/given name.
     */
    @Column(name = "GIVEN_NAME", length = 255)
    private String givenName;

    /**
     * OIDC family_name claim.
     * The user's last/family name.
     */
    @Column(name = "FAMILY_NAME", length = 255)
    private String familyName;

    /**
     * OIDC preferred_username claim.
     * The user's preferred display name.
     */
    @Column(name = "PREFERRED_USERNAME", length = 255)
    private String preferredUsername;

    /**
     * OIDC picture claim.
     * URL to the user's profile picture.
     */
    @Column(name = "PICTURE_URL", length = 1024)
    private String pictureUrl;

    /**
     * OIDC zoneinfo claim.
     * The user's timezone (e.g., "America/New_York").
     */
    @Column(name = "ZONEINFO", length = 100)
    private String zoneinfo;

    /**
     * OIDC locale claim.
     * The user's locale (e.g., "en-US").
     */
    @Column(name = "LOCALE", length = 50)
    private String locale;

    // ==================== OIDC 'email' scope ====================

    /**
     * OIDC email claim.
     * The user's email address.
     */
    @Column(name = "EMAIL", length = 255)
    private String email;

    /**
     * OIDC email_verified claim.
     * Whether the email has been verified.
     */
    @Column(name = "EMAIL_VERIFIED")
    private Boolean emailVerified;

    // ==================== Timestamps ====================

    /**
     * When this user record was created in Airavata.
     */
    @Column(name = "CREATED_AT", nullable = false)
    private Timestamp createdAt;

    /**
     * OIDC updated_at claim.
     * When the user's profile was last updated (seconds since epoch).
     */
    @Column(name = "UPDATED_AT")
    private Long updatedAt;

    /**
     * Personal group ID for this user (Zanzibar-like model).
     * Format: airavataInternalUserId + "_personal". Used for quick lookup.
     */
    @Column(name = "PERSONAL_GROUP_ID", length = 512)
    private String personalGroupId;

    // ==================== Relationships ====================

    /**
     * Associated gateway entity.
     */
    @ManyToOne(targetEntity = GatewayEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "GATEWAY_ID",
            referencedColumnName = "GATEWAY_ID",
            nullable = false,
            insertable = false,
            updatable = false)
    private GatewayEntity gateway;

    // ==================== Constructors ====================

    public UserEntity() {}

    /**
     * Creates a user entity from OIDC claims.
     *
     * @param sub the OIDC subject identifier
     * @param gatewayId the gateway context
     */
    public UserEntity(String sub, String gatewayId) {
        this.sub = sub;
        this.gatewayId = gatewayId;
        this.airavataInternalUserId = createInternalUserId(sub, gatewayId);
    }

    // ==================== Getters and Setters ====================

    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getZoneinfo() {
        return zoneinfo;
    }

    public void setZoneinfo(String zoneinfo) {
        this.zoneinfo = zoneinfo;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPersonalGroupId() {
        return personalGroupId;
    }

    public void setPersonalGroupId(String personalGroupId) {
        this.personalGroupId = personalGroupId;
    }

    public GatewayEntity getGateway() {
        return gateway;
    }

    public void setGateway(GatewayEntity gateway) {
        this.gateway = gateway;
    }

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = AiravataUtils.getCurrentTimestamp();
        }
        if (this.updatedAt == null) {
            this.updatedAt = System.currentTimeMillis() / 1000; // OIDC uses seconds
        }
        // Ensure airavataInternalUserId follows the sub@gatewayId format
        if (this.airavataInternalUserId == null && this.sub != null && this.gatewayId != null) {
            this.airavataInternalUserId = createInternalUserId(this.sub, this.gatewayId);
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = System.currentTimeMillis() / 1000; // OIDC uses seconds
    }

    // ==================== Utility Methods ====================

    /**
     * Returns the display name for this user.
     * Prefers preferred_username, then given_name + family_name, then sub.
     *
     * @return the display name
     */
    public String getDisplayName() {
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        if (givenName != null || familyName != null) {
            StringBuilder name = new StringBuilder();
            if (givenName != null) {
                name.append(givenName);
            }
            if (familyName != null) {
                if (name.length() > 0) {
                    name.append(" ");
                }
                name.append(familyName);
            }
            return name.toString();
        }
        return sub;
    }

    /**
     * Creates the airavataInternalUserId from sub and gatewayId.
     *
     * @param sub the OIDC subject identifier
     * @param gatewayId the gateway identifier
     * @return formatted internal user ID
     */
    public static String createInternalUserId(String sub, String gatewayId) {
        return sub + "@" + gatewayId;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(airavataInternalUserId, that.airavataInternalUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airavataInternalUserId);
    }

    @Override
    public String toString() {
        return "UserEntity{"
                + "airavataInternalUserId='" + airavataInternalUserId + '\''
                + ", sub='" + sub + '\''
                + ", gatewayId='" + gatewayId + '\''
                + ", givenName='" + givenName + '\''
                + ", familyName='" + familyName + '\''
                + ", preferredUsername='" + preferredUsername + '\''
                + ", email='" + email + '\''
                + ", emailVerified=" + emailVerified
                + ", zoneinfo='" + zoneinfo + '\''
                + ", locale='" + locale + '\''
                + '}';
    }
}
