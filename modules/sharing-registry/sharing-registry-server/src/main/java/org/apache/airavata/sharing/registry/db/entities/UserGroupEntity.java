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
import java.util.List;

@Entity
@Table(name = "USER_GROUP", schema = "")
@IdClass(UserGroupPK.class)
public class UserGroupEntity {
    private final static Logger logger = LoggerFactory.getLogger(UserGroupEntity.class);
    private String groupId;
    private String domainId;
    private String name;
    private String description;
    private String ownerId;
    private String groupType;
    private String groupCardinality;
    private Long createdTime;
    private Long updatedTime;
    private List<GroupAdminEntity> groupAdmins;

    @Id
    @Column(name = "GROUP_ID")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
    @Column(name = "OWNER_ID")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    @Basic
    @Column(name = "GROUP_CARDINALITY")
    public String getGroupCardinality() {
        return groupCardinality;
    }

    public void setGroupCardinality(String groupCardinality) {
        this.groupCardinality = groupCardinality;
    }

    @Basic
    @Column(name = "GROUP_TYPE")
    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String type) {
        this.groupType = type;
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

    @OneToMany(targetEntity = GroupAdminEntity.class, cascade = CascadeType.ALL,
            mappedBy = "userGroup", fetch = FetchType.EAGER)
    public List<GroupAdminEntity> getGroupAdmins() {
        return groupAdmins;
    }

    public void setGroupAdmins(List<GroupAdminEntity> groupAdmins) {
        this.groupAdmins = groupAdmins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserGroupEntity that = (UserGroupEntity) o;

        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) return false;
        if (getDomainId() != null ? !getDomainId().equals(that.getDomainId()) : that.getDomainId() != null)
            return false;
        if (getOwnerId() != null ? !getOwnerId().equals(that.getOwnerId()) : that.getOwnerId() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getGroupType() != null ? !getGroupType().equals(that.getGroupType()) : that.getGroupType() != null)
            return false;
        if (getCreatedTime() != null ? !getCreatedTime().equals(that.getCreatedTime()) : that.getCreatedTime() != null)
            return false;
        if (getUpdatedTime() != null ? !getUpdatedTime().equals(that.getUpdatedTime()) : that.getUpdatedTime() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getGroupId() != null ? getGroupId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getGroupType() != null ? getGroupType().hashCode() : 0);
        result = 31 * result + (getCreatedTime() != null ? getCreatedTime().hashCode() : 0);
        result = 31 * result + (getUpdatedTime() != null ? getUpdatedTime().hashCode() : 0);
        return result;
    }
}