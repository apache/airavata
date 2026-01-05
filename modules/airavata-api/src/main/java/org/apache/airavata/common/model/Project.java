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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: Project
 */
public class Project {
    private String projectID;
    private String owner;
    private String gatewayId;
    private String name;
    private String description;
    private long creationTime;
    private List<String> sharedUsers;
    private List<String> sharedGroups;

    public Project() {}

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public List<String> getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(List<String> sharedUsers) {
        this.sharedUsers = sharedUsers;
    }

    public List<String> getSharedGroups() {
        return sharedGroups;
    }

    public void setSharedGroups(List<String> sharedGroups) {
        this.sharedGroups = sharedGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project that = (Project) o;
        return Objects.equals(projectID, that.projectID)
                && Objects.equals(owner, that.owner)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(sharedUsers, that.sharedUsers)
                && Objects.equals(sharedGroups, that.sharedGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectID, owner, gatewayId, name, description, creationTime, sharedUsers, sharedGroups);
    }

    @Override
    public String toString() {
        return "Project{" + "projectID=" + projectID + ", owner=" + owner + ", gatewayId=" + gatewayId + ", name="
                + name + ", description=" + description + ", creationTime=" + creationTime + ", sharedUsers="
                + sharedUsers + ", sharedGroups=" + sharedGroups + "}";
    }
}
