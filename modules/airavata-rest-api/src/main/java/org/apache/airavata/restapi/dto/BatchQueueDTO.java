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
package org.apache.airavata.restapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchQueueDTO {
    private String queueName;
    private String queueDescription;
    private Integer maxRunTime;
    private Integer maxNodes;
    private Integer maxProcessors;
    private Integer maxJobsInQueue;
    private Integer maxMemory;
    private Integer cpuPerNode;
    private Integer defaultNodeCount;
    private Integer defaultCPUCount;
    private Integer defaultWalltime;
    private String queueSpecificMacros;
    private Boolean isDefaultQueue;

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

    public Integer getMaxRunTime() {
        return maxRunTime;
    }

    public void setMaxRunTime(Integer maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    public Integer getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(Integer maxNodes) {
        this.maxNodes = maxNodes;
    }

    public Integer getMaxProcessors() {
        return maxProcessors;
    }

    public void setMaxProcessors(Integer maxProcessors) {
        this.maxProcessors = maxProcessors;
    }

    public Integer getMaxJobsInQueue() {
        return maxJobsInQueue;
    }

    public void setMaxJobsInQueue(Integer maxJobsInQueue) {
        this.maxJobsInQueue = maxJobsInQueue;
    }

    public Integer getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Integer maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Integer getCpuPerNode() {
        return cpuPerNode;
    }

    public void setCpuPerNode(Integer cpuPerNode) {
        this.cpuPerNode = cpuPerNode;
    }

    public Integer getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(Integer defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public Integer getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(Integer defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public Integer getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(Integer defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public String getQueueSpecificMacros() {
        return queueSpecificMacros;
    }

    public void setQueueSpecificMacros(String queueSpecificMacros) {
        this.queueSpecificMacros = queueSpecificMacros;
    }

    public Boolean getIsDefaultQueue() {
        return isDefaultQueue;
    }

    public void setIsDefaultQueue(Boolean isDefaultQueue) {
        this.isDefaultQueue = isDefaultQueue;
    }
}



