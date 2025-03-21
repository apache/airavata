/*
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
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.airavata.agent.connection.service.models;

import java.util.ArrayList;
import java.util.List;

public class AgentLaunchRequest {

    private String experimentName;
    private String projectName;
    private String remoteCluster;
    private String group;
    private List<String> libraries = new ArrayList<>();
    private List<String> pip = new ArrayList<>();
    private List<String> mounts = new ArrayList<>();

    private String queue = "shared";
    private int wallTime = 30;
    private int cpuCount = 2;
    private int nodeCount = 1;
    private int memory = 2048;

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getRemoteCluster() {
        return remoteCluster;
    }

    public void setRemoteCluster(String remoteCluster) {
        this.remoteCluster = remoteCluster;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getWallTime() {
        return wallTime;
    }

    public void setWallTime(int wallTime) {
        this.wallTime = wallTime;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getApplicationInterfaceName() {
        return remoteCluster + (group != null && !group.trim().isEmpty() ? ("_" + group) : "");
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public List<String> getPip() {
        return pip;
    }

    public void setPip(List<String> pip) {
        this.pip = pip;
    }

    public List<String> getMounts() {
        return mounts;
    }

    public void setMounts(List<String> mounts) {
        this.mounts = mounts;
    }
}
