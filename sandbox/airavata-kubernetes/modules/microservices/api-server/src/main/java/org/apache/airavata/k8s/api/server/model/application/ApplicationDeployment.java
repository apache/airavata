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
package org.apache.airavata.k8s.api.server.model.application;

import org.apache.airavata.k8s.api.server.model.compute.ComputeResourceModel;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "APPLICATION_DEPLOYMENT")
public class ApplicationDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "APPLICATION_MODULE_ID")
    private ApplicationModule applicationModule;

    @ManyToOne
    @JoinColumn(name = "COMPUTE_RESOURCE_ID")
    private ComputeResourceModel computeResource;

    private String name;
    private String executablePath;
    private String preJobCommand;
    private String postJobCommand;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ApplicationModule getApplicationModule() {
        return applicationModule;
    }

    public void setApplicationModule(ApplicationModule applicationModule) {
        this.applicationModule = applicationModule;
    }

    public ComputeResourceModel getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResourceModel computeResourceModel) {
        this.computeResource = computeResourceModel;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getPreJobCommand() {
        return preJobCommand;
    }

    public void setPreJobCommand(String preJobCommand) {
        this.preJobCommand = preJobCommand;
    }

    public String getPostJobCommand() {
        return postJobCommand;
    }

    public void setPostJobCommand(String postJobCommand) {
        this.postJobCommand = postJobCommand;
    }

    public String getName() {
        return name;
    }

    public ApplicationDeployment setName(String name) {
        this.name = name;
        return this;
    }
}
