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
 * Domain model: BatchQueue
 */
public class BatchQueue {
    private String queueName;
    private String queueDescription;
    private int maxRunTime;
    private int maxNodes;
    private int maxProcessors;
    private int maxJobsInQueue;
    private int maxMemory;
    private int cpuPerNode;
    private int defaultNodeCount;
    private int defaultCPUCount;
    private int defaultWalltime;
    private String queueSpecificMacros;
    private boolean isDefaultQueue;

    public BatchQueue() {}

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }

    public int getMaxRunTime() {
        return maxRunTime;
    }

    public void setMaxRunTime(int maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getMaxProcessors() {
        return maxProcessors;
    }

    public void setMaxProcessors(int maxProcessors) {
        this.maxProcessors = maxProcessors;
    }

    public int getMaxJobsInQueue() {
        return maxJobsInQueue;
    }

    public void setMaxJobsInQueue(int maxJobsInQueue) {
        this.maxJobsInQueue = maxJobsInQueue;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getCpuPerNode() {
        return cpuPerNode;
    }

    public void setCpuPerNode(int cpuPerNode) {
        this.cpuPerNode = cpuPerNode;
    }

    public int getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(int defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public int getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(int defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public String getQueueSpecificMacros() {
        return queueSpecificMacros;
    }

    public void setQueueSpecificMacros(String queueSpecificMacros) {
        this.queueSpecificMacros = queueSpecificMacros;
    }

    public boolean getIsDefaultQueue() {
        return isDefaultQueue;
    }

    public void setIsDefaultQueue(boolean isDefaultQueue) {
        this.isDefaultQueue = isDefaultQueue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchQueue that = (BatchQueue) o;
        return Objects.equals(queueName, that.queueName)
                && Objects.equals(queueDescription, that.queueDescription)
                && Objects.equals(maxRunTime, that.maxRunTime)
                && Objects.equals(maxNodes, that.maxNodes)
                && Objects.equals(maxProcessors, that.maxProcessors)
                && Objects.equals(maxJobsInQueue, that.maxJobsInQueue)
                && Objects.equals(maxMemory, that.maxMemory)
                && Objects.equals(cpuPerNode, that.cpuPerNode)
                && Objects.equals(defaultNodeCount, that.defaultNodeCount)
                && Objects.equals(defaultCPUCount, that.defaultCPUCount)
                && Objects.equals(defaultWalltime, that.defaultWalltime)
                && Objects.equals(queueSpecificMacros, that.queueSpecificMacros)
                && Objects.equals(isDefaultQueue, that.isDefaultQueue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                queueName,
                queueDescription,
                maxRunTime,
                maxNodes,
                maxProcessors,
                maxJobsInQueue,
                maxMemory,
                cpuPerNode,
                defaultNodeCount,
                defaultCPUCount,
                defaultWalltime,
                queueSpecificMacros,
                isDefaultQueue);
    }

    @Override
    public String toString() {
        return "BatchQueue{" + "queueName=" + queueName + ", queueDescription=" + queueDescription + ", maxRunTime="
                + maxRunTime + ", maxNodes=" + maxNodes + ", maxProcessors=" + maxProcessors + ", maxJobsInQueue="
                + maxJobsInQueue + ", maxMemory=" + maxMemory + ", cpuPerNode=" + cpuPerNode + ", defaultNodeCount="
                + defaultNodeCount + ", defaultCPUCount=" + defaultCPUCount + ", defaultWalltime=" + defaultWalltime
                + ", queueSpecificMacros=" + queueSpecificMacros + ", isDefaultQueue=" + isDefaultQueue + "}";
    }
}
