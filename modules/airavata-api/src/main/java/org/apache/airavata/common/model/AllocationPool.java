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
package org.apache.airavata.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model: AllocationPool (merged group).
 *
 * <p>An allocation pool groups multiple {@link GroupResourceProfile} instances that represent
 * the same logical project across multiple runtimes. Each member group profile carries
 * (credential, compute resource, partition/project) for one Slurm cluster; the pool is the
 * union of those, usable as a single pool of credentials and runtimes designated for
 * an actual project.
 *
 * <p>Use pools to: (1) merge groups that represent the same project across runtimes,
 * (2) use the pool as the set of (credential, runtime) options for job submission.
 */
public class AllocationPool {
    private String allocationPoolId;
    private String gatewayId;
    private String name;
    private String description;
    /** Logical project name this pool is designated for (e.g. matches group designatedAllocationProjectName). */
    private String logicalProjectName;
    private List<String> groupResourceProfileIds = new ArrayList<>();

    public AllocationPool() {}

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

    public List<String> getGroupResourceProfileIds() {
        return groupResourceProfileIds;
    }

    public void setGroupResourceProfileIds(List<String> groupResourceProfileIds) {
        this.groupResourceProfileIds = groupResourceProfileIds != null ? new ArrayList<>(groupResourceProfileIds) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllocationPool that = (AllocationPool) o;
        return Objects.equals(allocationPoolId, that.allocationPoolId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(logicalProjectName, that.logicalProjectName)
                && Objects.equals(groupResourceProfileIds, that.groupResourceProfileIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocationPoolId, gatewayId, name, description, logicalProjectName, groupResourceProfileIds);
    }
}
