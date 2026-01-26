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
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import org.apache.airavata.common.model.MetadataParentType;
import org.apache.airavata.common.model.PreferenceValueType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified MetadataEntity that consolidates key-value metadata records from various
 * entity types including data products, data replicas, experiments, and processes.
 *
 * <p>This entity replaces the following separate metadata entities:
 * <ul>
 *   <li>{@code DataProductMetadataEntity}</li>
 *   <li>{@code DataReplicaMetadataEntity}</li>
 * </ul>
 *
 * <p>The {@code parentType} field discriminates between different parent entity types,
 * while {@code parentId} stores the ID of the parent entity (product URI, replica ID, etc.).
 * The {@code key} and {@code value} fields store the actual metadata key-value pairs.
 *
 * <p>The {@code valueType} field indicates how the value should be interpreted (string,
 * integer, boolean, JSON, or timestamp), enabling type-safe metadata handling.
 */
@Entity(name = "MetadataEntity")
@Table(
        name = "METADATA",
        indexes = {
            @Index(name = "idx_metadata_parent", columnList = "PARENT_TYPE, PARENT_ID"),
            @Index(name = "idx_metadata_key", columnList = "METADATA_KEY"),
            @Index(name = "idx_metadata_parent_key", columnList = "PARENT_TYPE, PARENT_ID, METADATA_KEY")
        })
@IdClass(MetadataEntityPK.class)
public class MetadataEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetadataParentType parentType;

    @Id
    @Column(name = "PARENT_ID", nullable = false)
    private String parentId;

    @Id
    @Column(name = "METADATA_KEY", nullable = false)
    private String key;

    @Lob
    @Column(name = "METADATA_VALUE")
    private String value;

    @Column(name = "VALUE_TYPE")
    @Enumerated(EnumType.STRING)
    private PreferenceValueType valueType = PreferenceValueType.STRING;

    @Column(name = "CREATION_TIME", nullable = false, updatable = false)
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    public MetadataEntity() {}

    /**
     * Creates a metadata entity for a specific parent.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID (product URI, replica ID, etc.)
     * @param key the metadata key
     * @param value the metadata value
     */
    public MetadataEntity(MetadataParentType parentType, String parentId, String key, String value) {
        this.parentType = parentType;
        this.parentId = parentId;
        this.key = key;
        this.value = value;
        this.valueType = PreferenceValueType.STRING;
    }

    /**
     * Creates a metadata entity with a specific value type.
     *
     * @param parentType the type of parent entity
     * @param parentId the parent entity ID (product URI, replica ID, etc.)
     * @param key the metadata key
     * @param value the metadata value
     * @param valueType the type of the value
     */
    public MetadataEntity(
            MetadataParentType parentType, String parentId, String key, String value, PreferenceValueType valueType) {
        this.parentType = parentType;
        this.parentId = parentId;
        this.key = key;
        this.value = value;
        this.valueType = valueType;
    }

    public MetadataParentType getParentType() {
        return parentType;
    }

    public void setParentType(MetadataParentType parentType) {
        this.parentType = parentType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    @PrePersist
    void onCreate() {
        Timestamp now = AiravataUtils.getUniqueTimestamp();
        if (this.creationTime == null) {
            this.creationTime = now;
        }
        if (this.updateTime == null) {
            this.updateTime = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updateTime = AiravataUtils.getUniqueTimestamp();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MetadataEntity that = (MetadataEntity) obj;
        return parentType == that.parentType
                && Objects.equals(parentId, that.parentId)
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentType, parentId, key);
    }

    @Override
    public String toString() {
        return "MetadataEntity{"
                + "parentType="
                + parentType
                + ", parentId='"
                + parentId
                + '\''
                + ", key='"
                + key
                + '\''
                + ", value='"
                + (value != null ? value.substring(0, Math.min(50, value.length())) + "..." : "null")
                + '\''
                + ", valueType="
                + valueType
                + ", creationTime="
                + creationTime
                + ", updateTime="
                + updateTime
                + '}';
    }
}
