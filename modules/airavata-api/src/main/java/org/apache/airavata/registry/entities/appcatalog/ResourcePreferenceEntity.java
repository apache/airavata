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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.PreferenceLevel;
import org.apache.airavata.common.model.PreferenceResourceType;
import org.apache.airavata.common.model.PreferenceValueType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified key-value entity for resource preferences.
 *
 * <p>This entity stores all resource preferences (both compute and storage) as key-value pairs,
 * supporting hierarchical preference resolution at GATEWAY, GROUP, and USER levels.
 *
 * <h3>Key Design Benefits:</h3>
 * <ul>
 *   <li>No schema changes needed when adding new preference types</li>
 *   <li>Flexible querying by resource, owner, level, or key</li>
 *   <li>Supports typed values (STRING, INTEGER, BOOLEAN, JSON, TIMESTAMP)</li>
 *   <li>Single table for all preference types simplifies maintenance</li>
 * </ul>
 *
 * <h3>Example Preference Keys (Compute):</h3>
 * <ul>
 *   <li>loginUsername - SSH login username</li>
 *   <li>scratchLocation - Scratch directory path</li>
 *   <li>preferredBatchQueue - Default queue name</li>
 *   <li>allocationProjectNumber - Allocation/account number</li>
 *   <li>preferredJobSubmissionProtocol - SSH, LOCAL, etc.</li>
 *   <li>preferredDataMovementProtocol - SCP, SFTP, etc.</li>
 *   <li>qualityOfService - QoS setting</li>
 *   <li>reservation - Reservation name</li>
 *   <li>reservationStartTime - Reservation start (TIMESTAMP)</li>
 *   <li>reservationEndTime - Reservation end (TIMESTAMP)</li>
 *   <li>resourceSpecificCredentialStoreToken - Credential token</li>
 *   <li>sshAccountProvisioner - SSH provisioner class</li>
 *   <li>sshAccountProvisionerConfig - Provisioner config (JSON)</li>
 *   <li>overrideByAiravata - Whether Airavata can override (BOOLEAN)</li>
 * </ul>
 *
 * <h3>Example Preference Keys (Storage):</h3>
 * <ul>
 *   <li>fileSystemRootLocation - Root path for storage</li>
 *   <li>loginUsername - Storage login username</li>
 *   <li>resourceSpecificCredentialStoreToken - Credential token</li>
 * </ul>
 *
 * <h3>Level Resolution (Most Specific Wins):</h3>
 * USER (priority 2) > GROUP (priority 1) > GATEWAY (priority 0)
 *
 * @see PreferenceLevel
 * @see PreferenceResourceType
 * @see PreferenceValueType
 */
@Entity(name = "ResourcePreferenceEntity")
@Table(
        name = "RESOURCE_PREFERENCE",
        indexes = {
            @Index(name = "idx_res_pref_resource", columnList = "RESOURCE_TYPE, RESOURCE_ID"),
            @Index(name = "idx_res_pref_owner", columnList = "OWNER_ID"),
            @Index(name = "idx_res_pref_level", columnList = "PREFERENCE_LEVEL"),
            @Index(name = "idx_res_pref_key", columnList = "PREFERENCE_KEY"),
            @Index(
                    name = "idx_res_pref_owner_level",
                    columnList = "RESOURCE_TYPE, RESOURCE_ID, OWNER_ID, PREFERENCE_LEVEL"),
            @Index(
                    name = "idx_res_pref_lookup",
                    columnList = "RESOURCE_TYPE, RESOURCE_ID, PREFERENCE_LEVEL, PREFERENCE_KEY")
        })
@IdClass(ResourcePreferenceEntityPK.class)
public class ResourcePreferenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The type of resource this preference applies to (COMPUTE or STORAGE).
     */
    @Id
    @Column(name = "RESOURCE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceResourceType resourceType;

    /**
     * The ID of the compute or storage resource.
     */
    @Id
    @Column(name = "RESOURCE_ID", nullable = false)
    private String resourceId;

    /**
     * The owner ID whose interpretation depends on the level:
     * <ul>
     *   <li>GATEWAY: gatewayId</li>
     *   <li>GROUP: groupResourceProfileId</li>
     *   <li>USER: airavataInternalUserId (userId@gatewayId)</li>
     * </ul>
     */
    @Id
    @Column(name = "OWNER_ID", nullable = false, length = 512)
    private String ownerId;

    /**
     * The level at which this preference is set (GATEWAY, GROUP, or USER).
     */
    @Id
    @Column(name = "PREFERENCE_LEVEL", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceLevel level;

    /**
     * The preference key (e.g., "loginUsername", "scratchLocation").
     */
    @Id
    @Column(name = "PREFERENCE_KEY", nullable = false)
    private String key;

    /**
     * The preference value stored as a string.
     * Complex values (lists, objects) are serialized as JSON.
     */
    @Lob
    @Column(name = "PREFERENCE_VALUE")
    private String value;

    /**
     * The data type of the value for proper deserialization.
     */
    @Column(name = "VALUE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceValueType valueType;

    /**
     * When this preference was created.
     */
    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    /**
     * When this preference was last updated.
     */
    @Column(
            name = "UPDATE_TIME",
            nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    public ResourcePreferenceEntity() {}

    // ========== Getters and Setters ==========

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

    public PreferenceLevel getLevel() {
        return level;
    }

    public void setLevel(PreferenceLevel level) {
        this.level = level;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PreferenceValueType getValueType() {
        return valueType;
    }

    public void setValueType(PreferenceValueType valueType) {
        this.valueType = valueType;
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

    // ========== Lifecycle Callbacks ==========

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

    // ========== Convenience Methods ==========

    /**
     * Get the value as a String (no conversion).
     */
    public String getStringValue() {
        return value;
    }

    /**
     * Get the value as an Integer.
     *
     * @throws NumberFormatException if the value cannot be parsed
     */
    public Integer getIntegerValue() {
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get the value as a Long.
     *
     * @throws NumberFormatException if the value cannot be parsed
     */
    public Long getLongValue() {
        return value != null ? Long.parseLong(value) : null;
    }

    /**
     * Get the value as a Boolean.
     */
    public Boolean getBooleanValue() {
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    /**
     * Set the value with automatic type detection.
     */
    public void setTypedValue(Object obj) {
        if (obj == null) {
            this.value = null;
            this.valueType = PreferenceValueType.STRING;
        } else if (obj instanceof String) {
            this.value = (String) obj;
            this.valueType = PreferenceValueType.STRING;
        } else if (obj instanceof Integer || obj instanceof Long) {
            this.value = obj.toString();
            this.valueType = PreferenceValueType.INTEGER;
        } else if (obj instanceof Boolean) {
            this.value = obj.toString();
            this.valueType = PreferenceValueType.BOOLEAN;
        } else if (obj instanceof Timestamp) {
            this.value = String.valueOf(((Timestamp) obj).getTime());
            this.valueType = PreferenceValueType.TIMESTAMP;
        } else {
            // For complex objects, caller should serialize to JSON
            this.value = obj.toString();
            this.valueType = PreferenceValueType.JSON;
        }
    }
}
