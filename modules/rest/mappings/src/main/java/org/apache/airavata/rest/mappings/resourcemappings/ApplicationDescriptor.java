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

package org.apache.airavata.rest.mappings.resourcemappings;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

@XmlRootElement(name = "application")
public class ApplicationDescriptor {
    private String name;
    private String hostdescName;
//    private String serviceName;
    private String executablePath;
    private String workingDir;
    private String jobType;
    private String projectNumber;
    private String projectDescription;
    private String queueName;
    private int maxWallTime;
    private int cpuCount;
    private int nodeCount;
    private int processorsPerNode;
    private int minMemory;
    private int maxMemory;
    private String applicationDescType;
    private ServiceDescriptor serviceDescriptor;
    private String inputDir;
    private String outputDir;
    private String stdIn;
    private String stdOut;
    private String stdError;
    private String staticWorkigDir;
    private HashMap<String, String> environmentVariables;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostdescName() {
        return hostdescName;
    }

    public void setHostdescName(String hostdescName) {
        this.hostdescName = hostdescName;
    }

//    public String getServiceName() {
//        return serviceName;
//    }
//
//    public void setServiceName(String serviceName) {
//        this.serviceName = serviceName;
//    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getMaxWallTime() {
        return maxWallTime;
    }

    public void setMaxWallTime(int maxWallTime) {
        this.maxWallTime = maxWallTime;
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

    public int getProcessorsPerNode() {
        return processorsPerNode;
    }

    public void setProcessorsPerNode(int processorsPerNode) {
        this.processorsPerNode = processorsPerNode;
    }

    public int getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public String getApplicationDescType() {
        return applicationDescType;
    }

    public void setApplicationDescType(String applicationDescType) {
        this.applicationDescType = applicationDescType;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getInputDir() {
        return inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getStdIn() {
        return stdIn;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdError() {
        return stdError;
    }

    public HashMap<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setStdIn(String stdIn) {
        this.stdIn = stdIn;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public void setStdError(String stdError) {
        this.stdError = stdError;
    }

    public void setEnvironmentVariables(HashMap<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public String getStaticWorkigDir() {
        return staticWorkigDir;
    }

    public void setStaticWorkigDir(String staticWorkigDir) {
        this.staticWorkigDir = staticWorkigDir;
    }
}
