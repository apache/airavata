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
package org.apache.airavata.accounting.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain model: AllocationProject
 * Represents a compute allocation project (e.g., an HPC account or charge code) granted on
 * a specific {@link Resource}. Users are linked to allocation projects via
 * {@link CredentialAllocationProject}. The {@code projectCode} is the identifier recognized by
 * the scheduler (e.g., a SLURM account name).
 */
public class AllocationProject {
    private String allocationProjectId;
    /** Scheduler-level account or charge code (e.g., {@code "abc123"} for SLURM {@code --account}). */
    private String projectCode;

    private String resourceId;
    private String description;
    private String gatewayId;
    private Instant createdAt;

    public AllocationProject() {}

    public String getAllocationProjectId() {
        return allocationProjectId;
    }

    public void setAllocationProjectId(String allocationProjectId) {
        this.allocationProjectId = allocationProjectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocationProject that = (AllocationProject) o;
        return Objects.equals(allocationProjectId, that.allocationProjectId)
                && Objects.equals(projectCode, that.projectCode)
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(description, that.description)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocationProjectId, projectCode, resourceId, description, gatewayId, createdAt);
    }

    @Override
    public String toString() {
        return "AllocationProject{" + "allocationProjectId=" + allocationProjectId + ", projectCode=" + projectCode
                + ", resourceId=" + resourceId + ", description=" + description + ", gatewayId=" + gatewayId
                + ", createdAt=" + createdAt + "}";
    }
}
