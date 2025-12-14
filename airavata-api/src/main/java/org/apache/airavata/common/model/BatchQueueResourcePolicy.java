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

import java.util.Objects;

/**
 * Domain model: BatchQueueResourcePolicy
 */
public class BatchQueueResourcePolicy {
    private String resourcePolicyId;
    private String computeResourceId;
    private String groupResourceProfileId;
    private String queuename;
    private int maxAllowedNodes;
    private int maxAllowedCores;
    private int maxAllowedWalltime;

    public BatchQueueResourcePolicy() {}

    public String getResourcePolicyId() {
        return resourcePolicyId;
    }

    public void setResourcePolicyId(String resourcePolicyId) {
        this.resourcePolicyId = resourcePolicyId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getQueuename() {
        return queuename;
    }

    public void setQueuename(String queuename) {
        this.queuename = queuename;
    }

    public int getMaxAllowedNodes() {
        return maxAllowedNodes;
    }

    public void setMaxAllowedNodes(int maxAllowedNodes) {
        this.maxAllowedNodes = maxAllowedNodes;
    }

    public int getMaxAllowedCores() {
        return maxAllowedCores;
    }

    public void setMaxAllowedCores(int maxAllowedCores) {
        this.maxAllowedCores = maxAllowedCores;
    }

    public int getMaxAllowedWalltime() {
        return maxAllowedWalltime;
    }

    public void setMaxAllowedWalltime(int maxAllowedWalltime) {
        this.maxAllowedWalltime = maxAllowedWalltime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchQueueResourcePolicy that = (BatchQueueResourcePolicy) o;
        return Objects.equals(resourcePolicyId, that.resourcePolicyId)
                && Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(groupResourceProfileId, that.groupResourceProfileId)
                && Objects.equals(queuename, that.queuename)
                && Objects.equals(maxAllowedNodes, that.maxAllowedNodes)
                && Objects.equals(maxAllowedCores, that.maxAllowedCores)
                && Objects.equals(maxAllowedWalltime, that.maxAllowedWalltime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourcePolicyId,
                computeResourceId,
                groupResourceProfileId,
                queuename,
                maxAllowedNodes,
                maxAllowedCores,
                maxAllowedWalltime);
    }

    @Override
    public String toString() {
        return "BatchQueueResourcePolicy{" + "resourcePolicyId=" + resourcePolicyId + ", computeResourceId="
                + computeResourceId + ", groupResourceProfileId=" + groupResourceProfileId + ", queuename=" + queuename
                + ", maxAllowedNodes=" + maxAllowedNodes + ", maxAllowedCores=" + maxAllowedCores
                + ", maxAllowedWalltime=" + maxAllowedWalltime + "}";
    }
}
