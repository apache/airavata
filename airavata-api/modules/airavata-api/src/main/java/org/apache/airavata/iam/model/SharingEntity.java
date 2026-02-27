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
package org.apache.airavata.iam.model;

import java.util.Objects;

/**
 * Domain model: SharingEntity
 */
public class SharingEntity {
    private String entityId;
    private String domainId;
    private String entityTypeId;
    private String ownerId;
    private String parentEntityId;
    private String name;
    private String description;
    private byte[] binaryData;
    private String fullText;
    private Long sharedCount;
    private Long originalEntityCreationTime;
    private Long createdTime;
    private Long updatedTime;

    public SharingEntity() {}

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(String entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getParentEntityId() {
        return parentEntityId;
    }

    public void setParentEntityId(String parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public Long getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Long sharedCount) {
        this.sharedCount = sharedCount;
    }

    public Long getOriginalEntityCreationTime() {
        return originalEntityCreationTime;
    }

    public void setOriginalEntityCreationTime(Long originalEntityCreationTime) {
        this.originalEntityCreationTime = originalEntityCreationTime;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

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
        SharingEntity that = (SharingEntity) o;
        return Objects.equals(entityId, that.entityId)
                && Objects.equals(domainId, that.domainId)
                && Objects.equals(entityTypeId, that.entityTypeId)
                && Objects.equals(ownerId, that.ownerId)
                && Objects.equals(parentEntityId, that.parentEntityId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(binaryData, that.binaryData)
                && Objects.equals(fullText, that.fullText)
                && Objects.equals(sharedCount, that.sharedCount)
                && Objects.equals(originalEntityCreationTime, that.originalEntityCreationTime)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(updatedTime, that.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                entityId,
                domainId,
                entityTypeId,
                ownerId,
                parentEntityId,
                name,
                description,
                binaryData,
                fullText,
                sharedCount,
                originalEntityCreationTime,
                createdTime,
                updatedTime);
    }

    @Override
    public String toString() {
        return "SharingEntity{" + "entityId="
                + entityId + ", " + "domainId="
                + domainId + ", " + "entityTypeId="
                + entityTypeId + ", " + "ownerId="
                + ownerId + ", " + "parentEntityId="
                + parentEntityId + ", " + "name="
                + name + ", " + "description="
                + description + ", " + "binaryData="
                + binaryData + ", " + "fullText="
                + fullText + ", " + "sharedCount="
                + sharedCount + ", " + "originalEntityCreationTime="
                + originalEntityCreationTime + ", " + "createdTime="
                + createdTime + ", " + "updatedTime="
                + updatedTime + '}';
    }
}
