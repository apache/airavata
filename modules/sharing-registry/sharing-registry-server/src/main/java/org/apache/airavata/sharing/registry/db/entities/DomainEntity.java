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
@Table(name = "DOMAIN", schema = "" )
public class DomainEntity {
    private final static Logger logger = LoggerFactory.getLogger(DomainEntity.class);
    private String domainId;
    private String name;
    private String description;
    private Long createdTime;
    private Long updatedTime;
    private String initialUserGroupId;

    @Id
    @Column(name = "DOMAIN_ID")
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
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

    @Basic
    @Column(name = "INITIAL_USER_GROUP_ID")
    public String getInitialUserGroupId() {
        return initialUserGroupId;
    }

    public void setInitialUserGroupId(String initialUserGroupId) {
        this.initialUserGroupId = initialUserGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainEntity that = (DomainEntity) o;

        if (getDomainId() != null ? !getDomainId().equals(that.getDomainId()) : that.getDomainId() != null)
            return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getCreatedTime() != null ? !getCreatedTime().equals(that.getCreatedTime()) : that.getCreatedTime() != null)
            return false;
        if (getUpdatedTime() != null ? !getUpdatedTime().equals(that.getUpdatedTime()) : that.getUpdatedTime() != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getDomainId() != null ? getDomainId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getCreatedTime() != null ? getCreatedTime().hashCode() : 0);
        result = 31 * result + (getUpdatedTime() != null ? getUpdatedTime().hashCode() : 0);
        return result;
    }
}
