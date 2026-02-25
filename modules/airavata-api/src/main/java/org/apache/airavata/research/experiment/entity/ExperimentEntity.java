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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.apache.airavata.execution.entity.ProcessEntity;
import org.apache.airavata.research.project.entity.ProjectEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Experiment entity mapping to the EXPERIMENT table. Captures user-level experiment
 * metadata including application binding, structured inputs/outputs, and scheduling preferences.
 *
 * <p>Experiment state is a direct mutable column (not event-driven). Process-level events
 * cascade state updates to the experiment via the orchestration layer.
 */
@Entity
@Table(name = "experiment")
@EntityListeners(AuditingEntityListener.class)
public class ExperimentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "experiment_id")
    private String experimentId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "experiment_name", nullable = false)
    private String experimentName;

    @Column(name = "description")
    private String description;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "binding_id", nullable = false)
    private String bindingId;

    @Column(name = "state", nullable = false, length = 50)
    private String state = "CREATED";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scheduling", columnDefinition = "json")
    private Map<String, Object> scheduling;

    @Column(name = "parent_experiment_id")
    private String parentExperimentId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "json")
    private List<String> tags;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private ProjectEntity project;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "experiment", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcessEntity> processes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "experiment", orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<ExperimentInputEntity> inputs;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "experiment", orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<ExperimentOutputEntity> outputs;

    public ExperimentEntity() {}

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getScheduling() {
        return scheduling;
    }

    public void setScheduling(Map<String, Object> scheduling) {
        this.scheduling = scheduling;
    }

    public String getParentExperimentId() {
        return parentExperimentId;
    }

    public void setParentExperimentId(String parentExperimentId) {
        this.parentExperimentId = parentExperimentId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public List<ProcessEntity> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessEntity> processes) {
        this.processes = processes;
    }

    public List<ExperimentInputEntity> getInputs() {
        return inputs;
    }

    public void setInputs(List<ExperimentInputEntity> inputs) {
        this.inputs = inputs;
    }

    public List<ExperimentOutputEntity> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ExperimentOutputEntity> outputs) {
        this.outputs = outputs;
    }
}
