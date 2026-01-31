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
package org.apache.airavata.sharing.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: UserGroup
 */
public class UserGroup {
    private String groupId;
    private String domainId;
    private String name;
    private String description;
    private String ownerId;
    private GroupType groupType;
    private GroupCardinality groupCardinality;
    private Long createdTime;
    private Long updatedTime;
    private List<GroupAdmin> groupAdmins;
    private Boolean isPersonalGroup;

    public UserGroup() {}

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public GroupCardinality getGroupCardinality() {
        return groupCardinality;
    }

    public void setGroupCardinality(GroupCardinality groupCardinality) {
        this.groupCardinality = groupCardinality;
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

    public List<GroupAdmin> getGroupAdmins() {
        return groupAdmins;
    }

    public void setGroupAdmins(List<GroupAdmin> groupAdmins) {
        this.groupAdmins = groupAdmins;
    }

    public Boolean getIsPersonalGroup() {
        return isPersonalGroup;
    }

    public void setIsPersonalGroup(Boolean isPersonalGroup) {
        this.isPersonalGroup = isPersonalGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroup that = (UserGroup) o;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(domainId, that.domainId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(ownerId, that.ownerId)
                && Objects.equals(groupType, that.groupType)
                && Objects.equals(groupCardinality, that.groupCardinality)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(updatedTime, that.updatedTime)
                && Objects.equals(groupAdmins, that.groupAdmins)
                && Objects.equals(isPersonalGroup, that.isPersonalGroup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                groupId,
                domainId,
                name,
                description,
                ownerId,
                groupType,
                groupCardinality,
                createdTime,
                updatedTime,
                groupAdmins,
                isPersonalGroup);
    }

    @Override
    public String toString() {
        return "UserGroup{" + "groupId="
                + groupId + ", " + "domainId="
                + domainId + ", " + "name="
                + name + ", " + "description="
                + description + ", " + "ownerId="
                + ownerId + ", " + "groupType="
                + groupType + ", " + "groupCardinality="
                + groupCardinality + ", " + "createdTime="
                + createdTime                 + ", " + "updatedTime="
                + updatedTime + ", " + "groupAdmins="
                + groupAdmins + ", " + "isPersonalGroup="
                + isPersonalGroup + '}';
    }
}
