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
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The class for the experiment_summary view.
 */
@Entity
@Table(name = "EXPERIMENT_SUMMARY")
public class ExperimentSummaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Column(name = "PROJECT_ID")
    private String projectId;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EXPERIMENT_NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXECUTION_ID")
    private String executionId;

    @Column(name = "STATE")
    private String experimentStatus;

    @Column(name = "RESOURCE_HOST_ID")
    private String resourceHostId;

    @Column(name = "TIME_OF_STATE_CHANGE")
    private Timestamp statusUpdateTime;

    public ExperimentSummaryEntity() {
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(String experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }
}
