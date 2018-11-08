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
 */
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "AIRAVATA_WORKFLOW")
public class AiravataWorkflowEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @OneToMany(targetEntity = WorkflowApplicationEntity.class, cascade = CascadeType.ALL, mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<WorkflowApplicationEntity> applications;

    @OneToMany(targetEntity = WorkflowHandlerEntity.class, cascade = CascadeType.ALL, mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<WorkflowHandlerEntity> handlers;

    @OneToMany(targetEntity = WorkflowConnectionEntity.class, cascade = CascadeType.ALL, mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<WorkflowConnectionEntity> connections;

    @OneToMany(targetEntity = AiravataWorkflowStatusEntity.class, cascade = CascadeType.ALL, mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<AiravataWorkflowStatusEntity> statuses;

    @OneToMany(targetEntity = AiravataWorkflowErrorEntity.class, cascade = CascadeType.ALL, mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<AiravataWorkflowErrorEntity> errors;

    public AiravataWorkflowEntity() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setApplications(List<WorkflowApplicationEntity> applications) {
        this.applications = applications;
    }

    public void setHandlers(List<WorkflowHandlerEntity> handlers) {
        this.handlers = handlers;
    }

    public void setConnections(List<WorkflowConnectionEntity> connections) {
        this.connections = connections;
    }

    public void setStatuses(List<AiravataWorkflowStatusEntity> statuses) {
        this.statuses = statuses;
    }

    public void setErrors(List<AiravataWorkflowErrorEntity> errors) {
        this.errors = errors;
    }

    public String getId() {
        return id;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public List<WorkflowApplicationEntity> getApplications() {
        return applications;
    }

    public List<WorkflowHandlerEntity> getHandlers() {
        return handlers;
    }

    public List<WorkflowConnectionEntity> getConnections() {
        return connections;
    }

    public List<AiravataWorkflowStatusEntity> getStatuses() {
        return statuses;
    }

    public List<AiravataWorkflowErrorEntity> getErrors() {
        return errors;
    }
}
