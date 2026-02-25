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
package org.apache.airavata.research.experiment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Immutable;

/**
 * Read-only entity for the experiment_summary view.
 *
 * <p>The view reads experiment state directly from the experiment row's STATE column
 * (no join to events needed). Maps to simplified EXPERIMENT_SUMMARY view.
 */
@Entity
@Table(name = "experiment_summary")
@Immutable
public class ExperimentSummaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "experiment_id")
    private String experimentId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "gateway_id")
    private String gatewayId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "experiment_name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "state")
    private String experimentStatus;

    @Column(name = "time_of_state_change")
    private Instant statusUpdateTime;

    public ExperimentSummaryEntity() {}

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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

    public String getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(String experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public Instant getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Instant statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }
}
