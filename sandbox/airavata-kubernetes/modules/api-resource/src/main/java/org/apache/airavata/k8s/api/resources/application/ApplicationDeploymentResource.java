/**
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
 */
package org.apache.airavata.k8s.api.resources.application;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationDeploymentResource {

    private long id;
    private String name;
    private long applicationModuleId;
    private long computeResourceId;
    private String executablePath;
    private String preJobCommand;
    private String postJobCommand;

    public long getId() {
        return id;
    }

    public ApplicationDeploymentResource setId(long id) {
        this.id = id;
        return this;
    }

    public long getApplicationModuleId() {
        return applicationModuleId;
    }

    public ApplicationDeploymentResource setApplicationModuleId(long applicationModuleId) {
        this.applicationModuleId = applicationModuleId;
        return this;
    }

    public long getComputeResourceId() {
        return computeResourceId;
    }

    public ApplicationDeploymentResource setComputeResourceId(long computeResourceId) {
        this.computeResourceId = computeResourceId;
        return this;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public ApplicationDeploymentResource setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
        return this;
    }

    public String getPreJobCommand() {
        return preJobCommand;
    }

    public ApplicationDeploymentResource setPreJobCommand(String preJobCommand) {
        this.preJobCommand = preJobCommand;
        return this;
    }

    public String getPostJobCommand() {
        return postJobCommand;
    }

    public ApplicationDeploymentResource setPostJobCommand(String postJobCommand) {
        this.postJobCommand = postJobCommand;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationDeploymentResource setName(String name) {
        this.name = name;
        return this;
    }
}
