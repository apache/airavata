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

@Entity
@Table(name = "SHARING", schema = "")
@IdClass(SharingPK.class)
public class SharingEntity {
    private final static Logger logger = LoggerFactory.getLogger(SharingEntity.class);
    private String permissionTypeId;
    private String entityId;
    private String groupId;
    private String domainId;
    private String sharingType;
    private String inheritedParentId;
    private Long createdTime;
    private Long updatedTime;

    @Id
    @Column(name = "PERMISSION_TYPE_ID")
    public String getPermissionTypeId() {
        return permissionTypeId;
    }

    public void setPermissionTypeId(String permissionTypeId) {
        this.permissionTypeId = permissionTypeId;
    }

    @Id
    @Column(name = "ENTITY_ID")
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @Id
    @Column(name = "GROUP_ID")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    @Id
    @Column(name = "INHERITED_PARENT_ID")
    public String getInheritedParentId() {
        return inheritedParentId;
    }

    public void setInheritedParentId(String inheritedParentId) {
        this.inheritedParentId = inheritedParentId;
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
    @Column(name = "SHARING_TYPE")
    public String getSharingType() {
        return sharingType;
    }

    public void setSharingType(String sharingType) {
        this.sharingType = sharingType;
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

        SharingEntity that = (SharingEntity) o;

        if (getPermissionTypeId() != null ? !getPermissionTypeId().equals(that.getPermissionTypeId()) : that.getPermissionTypeId() != null)
            return false;
        if (getEntityId() != null ? !getEntityId().equals(that.getEntityId()) : that.getEntityId() != null)
            return false;
        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) return false;
        if (getCreatedTime() != null ? !getCreatedTime().equals(that.getCreatedTime()) : that.getCreatedTime() != null)
            return false;
        if (getUpdatedTime() != null ? !getUpdatedTime().equals(that.getUpdatedTime()) : that.getUpdatedTime() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getPermissionTypeId() != null ? getPermissionTypeId().hashCode() : 0;
        result = 31 * result + (getEntityId() != null ? getEntityId().hashCode() : 0);
        result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
        return result;
    }
}