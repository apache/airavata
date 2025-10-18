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
package org.apache.airavata.catalog.sharing.db.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "GROUP_ADMIN")
@IdClass(GroupAdminPK.class)
public class GroupAdminEntity {
    private String groupId;
    private String domainId;
    private String adminId;
    private UserGroupEntity userGroup;

    @Id
    @Column(name = "GROUP_ID")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Id
    @Column(name = "DOMAIN_ID", length = 100)
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Id
    @Column(name = "ADMIN_ID")
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    @ManyToOne(targetEntity = UserGroupEntity.class, cascade = CascadeType.MERGE)
    @JoinColumns({
        @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID", insertable = false, updatable = false),
        @JoinColumn(name = "DOMAIN_ID", referencedColumnName = "DOMAIN_ID", insertable = false, updatable = false)
    })
    public UserGroupEntity getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroupEntity userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupAdminEntity that = (GroupAdminEntity) o;

        if (!getGroupId().equals(that.getGroupId())) return false;
        if (!getDomainId().equals(that.getDomainId())) return false;
        return getAdminId().equals(that.getAdminId());
    }

    @Override
    public int hashCode() {
        int result = getGroupId().hashCode();
        result = 31 * result + getDomainId().hashCode();
        result = 31 * result + getAdminId().hashCode();
        return result;
    }
}
