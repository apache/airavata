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
 * Domain model: Sharing
 */
public class Sharing {
    private String permissionTypeId;
    private String entityId;
    private String groupId;
    private SharingType sharingType;
    private String domainId;
    private String inheritedParentId;
    private Long createdTime;
    private Long updatedTime;

    public Sharing() {}

    public String getPermissionTypeId() {
        return permissionTypeId;
    }

    public void setPermissionTypeId(String permissionTypeId) {
        this.permissionTypeId = permissionTypeId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public SharingType getSharingType() {
        return sharingType;
    }

    public void setSharingType(SharingType sharingType) {
        this.sharingType = sharingType;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getInheritedParentId() {
        return inheritedParentId;
    }

    public void setInheritedParentId(String inheritedParentId) {
        this.inheritedParentId = inheritedParentId;
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
        Sharing that = (Sharing) o;
        return Objects.equals(permissionTypeId, that.permissionTypeId)
                && Objects.equals(entityId, that.entityId)
                && Objects.equals(groupId, that.groupId)
                && Objects.equals(sharingType, that.sharingType)
                && Objects.equals(domainId, that.domainId)
                && Objects.equals(inheritedParentId, that.inheritedParentId)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(updatedTime, that.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                permissionTypeId,
                entityId,
                groupId,
                sharingType,
                domainId,
                inheritedParentId,
                createdTime,
                updatedTime);
    }

    @Override
    public String toString() {
        return "Sharing{" + "permissionTypeId="
                + permissionTypeId + ", " + "entityId="
                + entityId + ", " + "groupId="
                + groupId + ", " + "sharingType="
                + sharingType + ", " + "domainId="
                + domainId + ", " + "inheritedParentId="
                + inheritedParentId + ", " + "createdTime="
                + createdTime + ", " + "updatedTime="
                + updatedTime + '}';
    }
}
