/**
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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an allocation pool (merged group).
 *
 * <p>An allocation pool groups multiple {@link ResourceProfileEntity} group profiles that
 * represent the same logical project across multiple runtimes. The pool acts as a single
 * pool of credentials and runtimes designated for that project. Member groups are stored
 * in {@link AllocationPoolGroupEntity}.
 */
@Entity
@Table(name = "ALLOCATION_POOL")
public class AllocationPoolEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ALLOCATION_POOL_ID", nullable = false, length = 255)
    private String allocationPoolId;

    @Column(name = "GATEWAY_ID", nullable = false, length = 255)
    private String gatewayId;

    @Column(name = "NAME", nullable = false, length = 512)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "LOGICAL_PROJECT_NAME", length = 512)
    private String logicalProjectName;

    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME", nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    @OneToMany(mappedBy = "allocationPool", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AllocationPoolGroupEntity> memberGroups = new ArrayList<>();

    public AllocationPoolEntity() {}

    public String getAllocationPoolId() {
        return allocationPoolId;
    }

    public void setAllocationPoolId(String allocationPoolId) {
        this.allocationPoolId = allocationPoolId;
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

    public String getLogicalProjectName() {
        return logicalProjectName;
    }

    public void setLogicalProjectName(String logicalProjectName) {
        this.logicalProjectName = logicalProjectName;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public List<AllocationPoolGroupEntity> getMemberGroups() {
        return memberGroups;
    }

    public void setMemberGroups(List<AllocationPoolGroupEntity> memberGroups) {
        this.memberGroups = memberGroups != null ? memberGroups : new ArrayList<>();
    }
}
