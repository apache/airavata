/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.sample.gateway;

import org.apache.airavata.schemas.gfac.JobTypeType;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/26/13
 * Time: 10:00 AM
 */

public class ExecutionParameters {

    private String hostAddress;
    private String hostName;
    private String gateKeeperAddress;
    private String gridftpAddress;
    private String projectNumber;

    private String applicationName = "EchoApplication";
    private String executableLocation = "/bin/echo";
    private String workingDirectory = "/scratch/01437/ogce";

    private String queueName = "shared";
    private JobTypeType.Enum jobType = JobTypeType.SERIAL;
    private int maxWallTime = 30;
    private int maxMemory = 100;
    private int cpuCount = 1;
    private int maxNodeCount = 1;
    private int maxProcessorsPerNode = 1;


    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getGateKeeperAddress() {
        return gateKeeperAddress;
    }

    public void setGateKeeperAddress(String gateKeeperAddress) {
        this.gateKeeperAddress = gateKeeperAddress;
    }

    public String getGridftpAddress() {
        return gridftpAddress;
    }

    public void setGridftpAddress(String gridftpAddress) {
        this.gridftpAddress = gridftpAddress;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getExecutableLocation() {
        return executableLocation;
    }

    public void setExecutableLocation(String executableLocation) {
        this.executableLocation = executableLocation;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public JobTypeType.Enum getJobType() {
        return jobType;
    }

    public void setJobType(JobTypeType.Enum jobType) {
        this.jobType = jobType;
    }

    public int getMaxWallTime() {
        return maxWallTime;
    }

    public void setMaxWallTime(int maxWallTime) {
        this.maxWallTime = maxWallTime;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getMaxNodeCount() {
        return maxNodeCount;
    }

    public void setMaxNodeCount(int maxNodeCount) {
        this.maxNodeCount = maxNodeCount;
    }

    public int getMaxProcessorsPerNode() {
        return maxProcessorsPerNode;
    }

    public void setMaxProcessorsPerNode(int maxProcessorsPerNode) {
        this.maxProcessorsPerNode = maxProcessorsPerNode;
    }
}
