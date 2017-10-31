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
package org.apache.airavata.k8s.api.resources.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExperimentResource {

    private long id;
    private String experimentName;
    private long creationTime;
    private String description;
    private long applicationInterfaceId;
    private String applicationInterfaceName;
    private long applicationDeploymentId;
    private String applicationDeploymentName;

    private List<ExperimentInputResource> experimentInputs = new ArrayList<>();

    private List<ExperimentOutputResource> experimentOutputs = new ArrayList<>();

    private List<ExperimentStatusResource> experimentStatus = new ArrayList<>();

    private List<Long> errorsIds = new ArrayList<>();

    private List<Long> processIds = new ArrayList<>();

    public long getId() {
        return id;
    }

    public ExperimentResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public ExperimentResource setExperimentName(String experimentName) {
        this.experimentName = experimentName;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public ExperimentResource setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExperimentResource setDescription(String description) {
        this.description = description;
        return this;
    }

    public long getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public ExperimentResource setApplicationInterfaceId(long applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
        return this;
    }

    public long getApplicationDeploymentId() {
        return applicationDeploymentId;
    }

    public ExperimentResource setApplicationDeploymentId(long applicationDeploymentId) {
        this.applicationDeploymentId = applicationDeploymentId;
        return this;
    }

    public List<ExperimentInputResource> getExperimentInputs() {
        return experimentInputs;
    }

    public ExperimentResource setExperimentInputs(List<ExperimentInputResource> experimentInputs) {
        this.experimentInputs = experimentInputs;
        return this;
    }

    public List<ExperimentOutputResource> getExperimentOutputs() {
        return experimentOutputs;
    }

    public ExperimentResource setExperimentOutputs(List<ExperimentOutputResource> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
        return this;
    }

    public List<ExperimentStatusResource> getExperimentStatus() {
        return experimentStatus;
    }

    public ExperimentResource setExperimentStatus(List<ExperimentStatusResource> experimentStatus) {
        this.experimentStatus = experimentStatus;
        return this;
    }

    public List<Long> getErrorsIds() {
        return errorsIds;
    }

    public ExperimentResource setErrorsIds(List<Long> errorsIds) {
        this.errorsIds = errorsIds;
        return this;
    }

    public List<Long> getProcessIds() {
        return processIds;
    }

    public ExperimentResource setProcessIds(List<Long> processIds) {
        this.processIds = processIds;
        return this;
    }

    public String getApplicationInterfaceName() {
        return applicationInterfaceName;
    }

    public ExperimentResource setApplicationInterfaceName(String applicationInterfaceName) {
        this.applicationInterfaceName = applicationInterfaceName;
        return this;
    }

    public String getApplicationDeploymentName() {
        return applicationDeploymentName;
    }

    public ExperimentResource setApplicationDeploymentName(String applicationDeploymentName) {
        this.applicationDeploymentName = applicationDeploymentName;
        return this;
    }
}
