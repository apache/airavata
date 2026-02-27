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
package org.apache.airavata.compute.resource.model;

import java.util.Objects;

/**
 * Domain model: ComputationalResourceScheduling
 */
public class ComputationalResourceScheduling {
    private String resourceHostId;
    private int totalCPUCount;
    private int nodeCount;
    private int numberOfThreads;
    private String queueName;
    private int wallTimeLimit;
    private int totalPhysicalMemory;
    private String chessisNumber;
    private String staticWorkingDir;
    private String overrideLoginUserName;
    private String overrideScratchLocation;
    private String overrideAllocationProjectNumber;
    private int mGroupCount;

    public ComputationalResourceScheduling() {}

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public int getTotalCPUCount() {
        return totalCPUCount;
    }

    public void setTotalCPUCount(int totalCPUCount) {
        this.totalCPUCount = totalCPUCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public int getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public void setTotalPhysicalMemory(int totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public String getChessisNumber() {
        return chessisNumber;
    }

    public void setChessisNumber(String chessisNumber) {
        this.chessisNumber = chessisNumber;
    }

    public String getStaticWorkingDir() {
        return staticWorkingDir;
    }

    public void setStaticWorkingDir(String staticWorkingDir) {
        this.staticWorkingDir = staticWorkingDir;
    }

    public String getOverrideLoginUserName() {
        return overrideLoginUserName;
    }

    public void setOverrideLoginUserName(String overrideLoginUserName) {
        this.overrideLoginUserName = overrideLoginUserName;
    }

    public String getOverrideScratchLocation() {
        return overrideScratchLocation;
    }

    public void setOverrideScratchLocation(String overrideScratchLocation) {
        this.overrideScratchLocation = overrideScratchLocation;
    }

    public String getOverrideAllocationProjectNumber() {
        return overrideAllocationProjectNumber;
    }

    public void setOverrideAllocationProjectNumber(String overrideAllocationProjectNumber) {
        this.overrideAllocationProjectNumber = overrideAllocationProjectNumber;
    }

    public int getMGroupCount() {
        return mGroupCount;
    }

    public void setMGroupCount(int mGroupCount) {
        this.mGroupCount = mGroupCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationalResourceScheduling that = (ComputationalResourceScheduling) o;
        return Objects.equals(resourceHostId, that.resourceHostId)
                && Objects.equals(totalCPUCount, that.totalCPUCount)
                && Objects.equals(nodeCount, that.nodeCount)
                && Objects.equals(numberOfThreads, that.numberOfThreads)
                && Objects.equals(queueName, that.queueName)
                && Objects.equals(wallTimeLimit, that.wallTimeLimit)
                && Objects.equals(totalPhysicalMemory, that.totalPhysicalMemory)
                && Objects.equals(chessisNumber, that.chessisNumber)
                && Objects.equals(staticWorkingDir, that.staticWorkingDir)
                && Objects.equals(overrideLoginUserName, that.overrideLoginUserName)
                && Objects.equals(overrideScratchLocation, that.overrideScratchLocation)
                && Objects.equals(overrideAllocationProjectNumber, that.overrideAllocationProjectNumber)
                && Objects.equals(mGroupCount, that.mGroupCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceHostId,
                totalCPUCount,
                nodeCount,
                numberOfThreads,
                queueName,
                wallTimeLimit,
                totalPhysicalMemory,
                chessisNumber,
                staticWorkingDir,
                overrideLoginUserName,
                overrideScratchLocation,
                overrideAllocationProjectNumber,
                mGroupCount);
    }

    @Override
    public String toString() {
        return "ComputationalResourceScheduling{" + "resourceHostId=" + resourceHostId + ", totalCPUCount="
                + totalCPUCount + ", nodeCount=" + nodeCount + ", numberOfThreads=" + numberOfThreads + ", queueName="
                + queueName + ", wallTimeLimit=" + wallTimeLimit + ", totalPhysicalMemory=" + totalPhysicalMemory
                + ", chessisNumber=" + chessisNumber + ", staticWorkingDir=" + staticWorkingDir
                + ", overrideLoginUserName=" + overrideLoginUserName + ", overrideScratchLocation="
                + overrideScratchLocation + ", overrideAllocationProjectNumber=" + overrideAllocationProjectNumber
                + ", mGroupCount=" + mGroupCount + "}";
    }
}
