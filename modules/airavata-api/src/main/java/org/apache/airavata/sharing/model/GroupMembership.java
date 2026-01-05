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

import java.util.Objects;

/**
 * Domain model: GroupMembership
 */
public class GroupMembership {
    private String parentId;
    private String childId;
    private String domainId;
    private GroupChildType childType;
    private Long createdTime;
    private Long updatedTime;

    public GroupMembership() {}

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public GroupChildType getChildType() {
        return childType;
    }

    public void setChildType(GroupChildType childType) {
        this.childType = childType;
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
        GroupMembership that = (GroupMembership) o;
        return Objects.equals(parentId, that.parentId)
                && Objects.equals(childId, that.childId)
                && Objects.equals(domainId, that.domainId)
                && Objects.equals(childType, that.childType)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(updatedTime, that.updatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, childId, domainId, childType, createdTime, updatedTime);
    }

    @Override
    public String toString() {
        return "GroupMembership{" + "parentId="
                + parentId + ", " + "childId="
                + childId + ", " + "domainId="
                + domainId + ", " + "childType="
                + childType + ", " + "createdTime="
                + createdTime + ", " + "updatedTime="
                + updatedTime + '}';
    }
}
