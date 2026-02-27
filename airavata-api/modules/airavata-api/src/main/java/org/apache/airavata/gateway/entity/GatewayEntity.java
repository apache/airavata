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
package org.apache.airavata.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Unified GatewayEntity that combines fields from ProfileGatewayEntity, expcatalog GatewayEntity,
 * and DomainEntity into a single entity. This entity serves as the single source of truth for
 * gateway data in the system, including sharing domain functionality.
 *
 * <p>The entity uses gatewayId as the primary key and maintains gatewayName as a unique indexed
 * field for URL-slug-style lookups.
 *
 * <p><strong>Sharing Domain:</strong>
 * The gatewayId serves as the domainId for the sharing registry. All sharing entities
 * (UserGroup, Entity, EntityType, PermissionType, GroupMembership, Sharing, GroupAdmin)
 * reference this gateway via its gatewayId for domain-scoped operations.
 *
 * @see org.apache.airavata.iam.service.SharingService
 */
@Entity(name = "GatewayEntity")
@Table(
        name = "gateway",
        indexes = {@Index(name = "uk_gateway_name", columnList = "gateway_name", unique = true)})
@EntityListeners(AuditingEntityListener.class)
public class GatewayEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @Column(name = "gateway_name", nullable = false, unique = true)
    private String gatewayName;

    @Column(name = "gateway_domain")
    private String domain;

    @Column(name = "email_address")
    private String emailAddress;

    /**
     * When this gateway/domain was created.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /**
     * When this gateway/domain was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * The initial user group ID for this gateway's sharing domain.
     * New users added to this gateway will automatically be added to this group.
     */
    @Column(name = "initial_user_group_id")
    private String initialUserGroupId;

    // ========== Gateway Groups (merged from GATEWAY_GROUPS table) ==========
    // These fields were merged from the former 1:1 GATEWAY_GROUPS table.

    /**
     * Group ID for gateway administrators.
     */
    @Column(name = "admins_group_id")
    private String adminsGroupId;

    /**
     * Group ID for read-only gateway administrators.
     */
    @Column(name = "read_only_admins_group_id")
    private String readOnlyAdminsGroupId;

    /**
     * Group ID for default gateway users.
     */
    @Column(name = "default_gateway_users_group_id")
    private String defaultGatewayUsersGroupId;

    public GatewayEntity() {
        this.gatewayId = UUID.randomUUID().toString();
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Returns the creation time for this gateway.
     *
     * @return the creation time, or null if not set
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation time for this gateway.
     *
     * @param createdAt the creation time
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the last updated time for this gateway.
     *
     * @return the last updated time, or null if not set
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last updated time for this gateway.
     *
     * @param updatedAt the last updated time
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the initial user group ID for this gateway's sharing domain.
     * New users added to this gateway will automatically be added to this group.
     *
     * @return the initial user group ID, or null if not set
     */
    public String getInitialUserGroupId() {
        return initialUserGroupId;
    }

    /**
     * Sets the initial user group ID for this gateway's sharing domain.
     *
     * @param initialUserGroupId the initial user group ID
     */
    public void setInitialUserGroupId(String initialUserGroupId) {
        this.initialUserGroupId = initialUserGroupId;
    }

    /**
     * Returns the group ID for gateway administrators.
     *
     * @return the admins group ID, or null if not set
     */
    public String getAdminsGroupId() {
        return adminsGroupId;
    }

    /**
     * Sets the group ID for gateway administrators.
     *
     * @param adminsGroupId the admins group ID
     */
    public void setAdminsGroupId(String adminsGroupId) {
        this.adminsGroupId = adminsGroupId;
    }

    /**
     * Returns the group ID for read-only gateway administrators.
     *
     * @return the read-only admins group ID, or null if not set
     */
    public String getReadOnlyAdminsGroupId() {
        return readOnlyAdminsGroupId;
    }

    /**
     * Sets the group ID for read-only gateway administrators.
     *
     * @param readOnlyAdminsGroupId the read-only admins group ID
     */
    public void setReadOnlyAdminsGroupId(String readOnlyAdminsGroupId) {
        this.readOnlyAdminsGroupId = readOnlyAdminsGroupId;
    }

    /**
     * Returns the group ID for default gateway users.
     *
     * @return the default gateway users group ID, or null if not set
     */
    public String getDefaultGatewayUsersGroupId() {
        return defaultGatewayUsersGroupId;
    }

    /**
     * Sets the group ID for default gateway users.
     *
     * @param defaultGatewayUsersGroupId the default gateway users group ID
     */
    public void setDefaultGatewayUsersGroupId(String defaultGatewayUsersGroupId) {
        this.defaultGatewayUsersGroupId = defaultGatewayUsersGroupId;
    }

    @PrePersist
    void ensureDefaults() {
        if (this.gatewayName == null && this.gatewayId != null) {
            this.gatewayName = this.gatewayId;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GatewayEntity that = (GatewayEntity) obj;
        return Objects.equals(gatewayId, that.gatewayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId);
    }

    @Override
    public String toString() {
        return "GatewayEntity{"
                + "gatewayId='"
                + gatewayId
                + '\''
                + ", gatewayName='"
                + gatewayName
                + '\''
                + ", domain='"
                + domain
                + '\''
                + ", emailAddress='"
                + emailAddress
                + '\''
                + ", createdAt="
                + createdAt
                + ", updatedAt="
                + updatedAt
                + ", initialUserGroupId='"
                + initialUserGroupId
                + '\''
                + ", adminsGroupId='"
                + adminsGroupId
                + '\''
                + ", readOnlyAdminsGroupId='"
                + readOnlyAdminsGroupId
                + '\''
                + ", defaultGatewayUsersGroupId='"
                + defaultGatewayUsersGroupId
                + '\''
                + '}';
    }
}
