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
package org.apache.airavata.registry.core.entities.airavataworkflowcatalog;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.model.workflow.WorkflowState;

@Entity
@Table(name = "AIRAVATA_WORKFLOW_STATUS")
@IdClass(AiravataWorkflowStatusPK.class)
public class AiravataWorkflowStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Id
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private WorkflowState state;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @ManyToOne(targetEntity = AiravataWorkflowEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "ID")
    private AiravataWorkflowEntity workflow;

    public AiravataWorkflowStatusEntity() {}

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setWorkflow(AiravataWorkflowEntity workflow) {
        this.workflow = workflow;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public WorkflowState getState() {
        return state;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public AiravataWorkflowEntity getWorkflow() {
        return workflow;
    }
}
