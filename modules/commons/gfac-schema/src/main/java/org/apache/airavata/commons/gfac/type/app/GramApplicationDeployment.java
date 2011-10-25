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

package org.apache.airavata.commons.gfac.type.app;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.schemas.gfac.GramApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.ShellApplicationDeploymentType;

public class GramApplicationDeployment extends ShellApplicationDeployment {

    private GramApplicationDeploymentType gramApplicationDeploymentType;

    public GramApplicationDeployment() {
        this.gramApplicationDeploymentType = GramApplicationDeploymentType.Factory.newInstance();
    }

    private GramApplicationDeployment(GramApplicationDeploymentType gadt) {
        this.gramApplicationDeploymentType = gadt;
    }

    public String getProjectName() {
        return gramApplicationDeploymentType.getProjectName();
    }

    public void setProjectName(String projectName) {
        this.gramApplicationDeploymentType.setProjectName(projectName);
    }

    public String getQueueName() {
        return gramApplicationDeploymentType.getQueueName();
    }

    public void setQueueName(String queueName) {
        this.gramApplicationDeploymentType.setQueueName(queueName);
    }

    public int getWallTime() {
        return gramApplicationDeploymentType.getWallTime();
    }

    public void setWallTime(int wallTime) {
        this.gramApplicationDeploymentType.setWallTime(wallTime);
    }

    public int getNodeCount() {
        return gramApplicationDeploymentType.getNodeCount();
    }

    public void setNodeCount(int nodeCount) {
        this.gramApplicationDeploymentType.setNodeCount(nodeCount);
    }

    public int getCpuCount() {
        return gramApplicationDeploymentType.getCpuCount();
    }

    public void setCpuCount(int cpuCount) {
        this.gramApplicationDeploymentType.setCpuCount(cpuCount);
    }

    public String getJobType() {
        return gramApplicationDeploymentType.getJobType();
    }

    public void setJobType(String jobType) {
        this.gramApplicationDeploymentType.setJobType(jobType);
    }
}
