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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Entity representing access to a resource with a specific credential.
 *
 * <p>This entity explicitly links an access owner (user or group) to a resource
 * (compute or storage) with a specific credential. This separates the "who can
 * access what with which credential" concern from configuration preferences.
 *
 * <h2>Visibility vs Access Model</h2>
 *
 * <p>Airavata uses a two-layer access control model for resources:
 *
 * <h3>1. Visibility (Sharing Registry)</h3>
 * <p>The Sharing Registry (ENTITY, SHARING tables) controls <b>visibility</b>:
 * which users/groups can see that a resource exists. This is managed via
 * {@link org.apache.airavata.common.model.SharingResourceType} and the
 * sharing registry service methods.
 *
 * <ul>
 *   <li>Controls: Who can see a resource in the UI/API</li>
 *   <li>Mechanism: ENTITY + SHARING tables with permission grants</li>
 *   <li>Entity Types: APPLICATION_INTERFACE, COMPUTE_RESOURCE, STORAGE_RESOURCE, etc.</li>
 *   <li>Check Location: Query methods (filter accessible resources before returning)</li>
 * </ul>
 *
 * <h3>2. Access (RESOURCE_ACCESS Table)</h3>
 * <p>This table controls <b>access</b>: who can actually use a resource with
 * which credential. A user may be able to see a resource exists but not have
 * a credential grant to use it.
 *
 * <ul>
 *   <li>Controls: Who can execute jobs/transfers on the resource</li>
 *   <li>Mechanism: This RESOURCE_ACCESS table with credential associations</li>
 *   <li>Check Location: Runtime/execution (before submitting jobs)</li>
 * </ul>
 *
 * <h3>Access Resolution Flow</h3>
 * <p>For a user to use a compute/storage resource, they must pass both checks:
 * <ol>
 *   <li><b>Visibility check</b>: User must have sharing permission to see the resource</li>
 *   <li><b>Access check</b>: User must have a RESOURCE_ACCESS grant with a valid credential</li>
 *   <li><b>Preference loading</b>: Configuration from RESOURCE_PREFERENCE is loaded</li>
 * </ol>
 *
 * <h3>Access Grant Model:</h3>
 * <ul>
 *   <li>An access owner (user or group) is granted access to a resource</li>
 *   <li>The access is mediated by a specific credential</li>
 *   <li>Access can be enabled/disabled without deleting the record</li>
 *   <li>Configuration preferences are stored separately in RESOURCE_PREFERENCE</li>
 * </ul>
 *
 * <h3>Owner ID Format:</h3>
 * <ul>
 *   <li>USER: userId@gatewayId (airavataInternalUserId format)</li>
 *   <li>GROUP: groupResourceProfileId</li>
 *   <li>GATEWAY: gatewayId</li>
 * </ul>
 *
 * @see PreferenceLevel
 * @see PreferenceResourceType
 */
@Entity
@Table(
        name = "RESOURCE_ACCESS",
        indexes = {
            @Index(name = "idx_res_access_owner", columnList = "OWNER_ID, OWNER_TYPE"),
            @Index(name = "idx_res_access_resource", columnList = "RESOURCE_TYPE, RESOURCE_ID"),
            @Index(name = "idx_res_access_credential", columnList = "CREDENTIAL_TOKEN"),
            @Index(name = "idx_res_access_gateway", columnList = "GATEWAY_ID"),
            @Index(name = "idx_res_access_enabled", columnList = "ENABLED")
        })
public class ResourceAccessEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * The type of resource being accessed (COMPUTE or STORAGE).
     */
    @Column(name = "RESOURCE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceResourceType resourceType;

    /**
     * The ID of the compute or storage resource.
     */
    @Column(name = "RESOURCE_ID", nullable = false)
    private String resourceId;

    /**
     * The owner ID whose interpretation depends on the owner type:
     * <ul>
     *   <li>USER: airavataInternalUserId (userId@gatewayId)</li>
     *   <li>GROUP: groupResourceProfileId</li>
     *   <li>GATEWAY: gatewayId</li>
     * </ul>
     */
    @Column(name = "OWNER_ID", nullable = false, length = 512)
    private String ownerId;

    /**
     * The type of owner (USER, GROUP, or GATEWAY).
     */
    @Column(name = "OWNER_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceLevel ownerType;

    /**
     * The gateway ID this access belongs to.
     * Always set for auditing and scoping queries.
     */
    @Column(name = "GATEWAY_ID", nullable = false)
    private String gatewayId;

    /**
     * Token for the credential used to access this resource.
     * References CREDENTIALS.TOKEN_ID.
     */
    @Column(name = "CREDENTIAL_TOKEN", nullable = false)
    private String credentialToken;

    /**
     * Optional login username override for this access.
     * If null, the username from the credential or preference is used.
     */
    @Column(name = "LOGIN_USERNAME")
    private String loginUsername;

    /**
     * Whether this access grant is currently enabled.
     * Allows disabling access without deleting the record.
     */
    @Column(name = "ENABLED", nullable = false)
    private boolean enabled = true;

    /**
     * Optional description or notes about this access grant.
     */
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * When this access was created.
     */
    @Column(name = "CREATION_TIME", nullable = false)
    private Timestamp creationTime;

    /**
     * When this access was last updated.
     */
    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    public ResourceAccessEntity() {}

    @PrePersist
    void setTimestamps() {
        Timestamp now = AiravataUtils.getCurrentTimestamp();
        if (this.creationTime == null) {
            this.creationTime = now;
        }
        this.updateTime = now;
    }

    @PreUpdate
    void updateTimestamp() {
        this.updateTime = AiravataUtils.getCurrentTimestamp();
    }

    // ========== Factory Methods ==========

    /**
     * Create a gateway-level resource access.
     */
    public static ResourceAccessEntity forGateway(
            String gatewayId,
            PreferenceResourceType resourceType,
            String resourceId,
            String credentialToken) {
        ResourceAccessEntity entity = new ResourceAccessEntity();
        entity.setOwnerType(PreferenceLevel.GATEWAY);
        entity.setOwnerId(gatewayId);
        entity.setGatewayId(gatewayId);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setCredentialToken(credentialToken);
        return entity;
    }

    /**
     * Create a group-level resource access.
     */
    public static ResourceAccessEntity forGroup(
            String groupResourceProfileId,
            String gatewayId,
            PreferenceResourceType resourceType,
            String resourceId,
            String credentialToken) {
        ResourceAccessEntity entity = new ResourceAccessEntity();
        entity.setOwnerType(PreferenceLevel.GROUP);
        entity.setOwnerId(groupResourceProfileId);
        entity.setGatewayId(gatewayId);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setCredentialToken(credentialToken);
        return entity;
    }

    /**
     * Create a user-level resource access.
     */
    public static ResourceAccessEntity forUser(
            String userId,
            String gatewayId,
            PreferenceResourceType resourceType,
            String resourceId,
            String credentialToken) {
        ResourceAccessEntity entity = new ResourceAccessEntity();
        entity.setOwnerType(PreferenceLevel.USER);
        entity.setOwnerId(userId + "@" + gatewayId);
        entity.setGatewayId(gatewayId);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setCredentialToken(credentialToken);
        return entity;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PreferenceResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(PreferenceResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public PreferenceLevel getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(PreferenceLevel ownerType) {
        this.ownerType = ownerType;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

    public void setCredentialToken(String credentialToken) {
        this.credentialToken = credentialToken;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ResourceAccessEntity{"
                + "id=" + id
                + ", resourceType=" + resourceType
                + ", resourceId='" + resourceId + '\''
                + ", ownerId='" + ownerId + '\''
                + ", ownerType=" + ownerType
                + ", credentialToken='" + credentialToken + '\''
                + ", enabled=" + enabled
                + '}';
    }
}
