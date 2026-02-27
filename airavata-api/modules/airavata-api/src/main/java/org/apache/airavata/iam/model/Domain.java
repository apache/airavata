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
 * Domain model: Domain
 */
public class Domain {
    private String domainId;
    private String name;
    private String description;
    private Long createdTime;
    private Long updatedTime;
    private String initialUserGroupId;

    public Domain() {}

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
        Domain that = (Domain) o;
        return Objects.equals(domainId, that.domainId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(updatedTime, that.updatedTime)
                && Objects.equals(initialUserGroupId, that.initialUserGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainId, name, description, createdTime, updatedTime, initialUserGroupId);
    }

    @Override
    public String toString() {
        return "Domain{" + "domainId="
                + domainId + ", " + "name="
                + name + ", " + "description="
                + description + ", " + "createdTime="
                + createdTime + ", " + "updatedTime="
                + updatedTime + ", " + "initialUserGroupId="
                + initialUserGroupId + '}';
    }
}
