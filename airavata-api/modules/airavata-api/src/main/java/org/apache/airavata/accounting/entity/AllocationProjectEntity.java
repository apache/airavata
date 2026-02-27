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
package org.apache.airavata.accounting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity representing an HPC allocation project that grants compute time on a resource.
 *
 * <p>Allocation projects (also known as charge accounts or project codes) are issued by
 * HPC centres to control and track resource consumption. A project is tied to a specific
 * {@code resourceId} and identified by its {@code projectCode} which is passed to the
 * scheduler at job submission time.
 *
 * <p>Users are associated with allocation projects via {@link CredentialAllocationProjectEntity}.
 */
@Entity
@Table(name = "allocation_project")
@EntityListeners(AuditingEntityListener.class)
public class AllocationProjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "allocation_project_id")
    private String allocationProjectId;

    @Column(name = "project_code", nullable = false)
    private String projectCode;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AllocationProjectEntity() {}

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
    public String toString() {
        return "AllocationProjectEntity{"
                + "allocationProjectId='" + allocationProjectId + '\''
                + ", projectCode='" + projectCode + '\''
                + ", resourceId='" + resourceId + '\''
                + ", gatewayId='" + gatewayId + '\''
                + '}';
    }
}
