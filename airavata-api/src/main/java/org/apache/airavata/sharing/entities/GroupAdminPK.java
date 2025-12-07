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
package org.apache.airavata.sharing.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupAdminPK implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(GroupAdminPK.class);
    private String groupId;
    private String domainId;
    private String adminId;

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

    @Id
    @Column(name = "ADMIN_ID")
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupAdminPK groupAdminPK = (GroupAdminPK) o;

        if (!getGroupId().equals(groupAdminPK.getGroupId())) return false;
        if (!getDomainId().equals(groupAdminPK.getDomainId())) return false;
        return getAdminId().equals(groupAdminPK.getAdminId());
    }

    @Override
    public int hashCode() {
        int result = getGroupId().hashCode();
        result = 31 * result + getDomainId().hashCode();
        result = 31 * result + getAdminId().hashCode();
        return result;
    }
}
