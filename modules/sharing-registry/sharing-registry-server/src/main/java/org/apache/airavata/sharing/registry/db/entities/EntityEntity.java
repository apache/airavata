/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.sharing.registry.db.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.nio.ByteBuffer;

@Entity
@Table(name = "ENTITY", schema = "")
@IdClass(EntityPK.class)
public class EntityEntity {
    private final static Logger logger = LoggerFactory.getLogger(EntityEntity.class);
    private String entityId;
    private String domainId;
    private String entityTypeId;
    private String ownerId;
    private String parentEntityId;
    private String name;
    private String description;
    private ByteBuffer binaryData;
    private String fullText;
    private Long originalEntityCreationTime;
    private Long sharedCount;
    private Long createdTime;
    private Long updatedTime;

    @Id
    @Column(name = "ENTITY_ID")
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Id
    @Column(name = "DOMAIN_ID")
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Basic
    @Column(name = "ENTITY_TYPE_ID")
    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    @Basic
    @Column(name = "OWNER_ID")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Basic
    @Column(name = "PARENT_ENTITY_ID")
    public String getParentEntityId() {
        return parentEntityId;
    }

    public void setParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    @Basic
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Lob
    @Column(name="BINARY_DATA")
    public ByteBuffer getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(ByteBuffer binaryData) {
        this.binaryData = binaryData;
    }

    @Basic
    @Column(name = "FULL_TEXT")
    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    @Basic
    @Column(name = "ORIGINAL_ENTITY_CREATION_TIME")
    public Long getOriginalEntityCreationTime() {
        return originalEntityCreationTime;
    }

    public void setOriginalEntityCreationTime(Long originalEntityCreationTime) {
        this.originalEntityCreationTime = originalEntityCreationTime;
    }

    @Basic
    @Column(name = "SHARED_COUNT")
    public Long getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Long sharedCount) {
        this.sharedCount = sharedCount;
    }

    @Basic
    @Column(name = "CREATED_TIME")
    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @Basic
    @Column(name = "UPDATED_TIME")
    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityEntity that = (EntityEntity) o;

        if (getEntityId() != null ? !getEntityId().equals(that.getEntityId()) : that.getEntityId() != null)
            return false;
        if (getDomainId() != null ? !getDomainId().equals(that.getDomainId()) : that.getDomainId() != null)
            return false;
        if (getParentEntityId() != null ? !getParentEntityId().equals(that.getParentEntityId()) : that.getParentEntityId() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getBinaryData().equals(that.getBinaryData())) return false;
        if (getFullText() != null ? !getFullText().equals(that.getFullText()) : that.getFullText() != null)
            return false;
        if (getOriginalEntityCreationTime() != null ? !getOriginalEntityCreationTime().equals(that.getOriginalEntityCreationTime())
                : that.getOriginalEntityCreationTime() != null) return false;
        if (getCreatedTime() != null ? !getCreatedTime().equals(that.getCreatedTime()) : that.getCreatedTime() != null)
            return false;
        if (getUpdatedTime() != null ? !getUpdatedTime().equals(that.getUpdatedTime()) : that.getUpdatedTime() != null)
            return false;
        if (getOwnerId() != null ? !getOwnerId().equals(that.getOwnerId()) : that.getOwnerId() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getEntityId() != null ? getEntityId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getBinaryData() != null ? getBinaryData().hashCode() : 0);
        result = 31 * result + (getFullText() != null ? getFullText().hashCode() : 0);
        result = 31 * result + (getOriginalEntityCreationTime() != null ? getOriginalEntityCreationTime().hashCode() : 0);
        result = 31 * result + (getCreatedTime() != null ? getCreatedTime().hashCode() : 0);
        result = 31 * result + (getUpdatedTime() != null ? getUpdatedTime().hashCode() : 0);
        return result;
    }
}