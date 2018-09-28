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

import org.apache.airavata.model.workflow.HandlerType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "WORKFLOW_HANDLER")
@IdClass(WorkflowHandlerPK.class)
public class WorkflowHandlerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Id
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private HandlerType type;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @ManyToOne(targetEntity = AiravataWorkflowEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "ID")
    private AiravataWorkflowEntity workflow;

    @OneToMany(targetEntity = HandlerStatusEntity.class, cascade = CascadeType.ALL, mappedBy = "handler", fetch = FetchType.EAGER)
    private List<HandlerStatusEntity> statuses;

    @OneToMany(targetEntity = HandlerErrorEntity.class, cascade = CascadeType.ALL, mappedBy = "handler", fetch = FetchType.EAGER)
    private List<HandlerErrorEntity> errors;

    @OneToMany(targetEntity = HandlerInputEntity.class, cascade = CascadeType.ALL, mappedBy = "handler", fetch = FetchType.EAGER)
    private List<HandlerInputEntity> inputs;

    @OneToMany(targetEntity = HandlerOutputEntity.class, cascade = CascadeType.ALL, mappedBy = "handler", fetch = FetchType.EAGER)
    private List<HandlerOutputEntity> outputs;

    public WorkflowHandlerEntity() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setType(HandlerType type) {
        this.type = type;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setWorkflow(AiravataWorkflowEntity workflow) {
        this.workflow = workflow;
    }

    public void setStatuses(List<HandlerStatusEntity> statuses) {
        this.statuses = statuses;
    }

    public void setErrors(List<HandlerErrorEntity> errors) {
        this.errors = errors;
    }

    public void setInputs(List<HandlerInputEntity> inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(List<HandlerOutputEntity> outputs) {
        this.outputs = outputs;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public HandlerType getType() {
        return type;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public AiravataWorkflowEntity getWorkflow() {
        return workflow;
    }

    public List<HandlerStatusEntity> getStatuses() {
        return statuses;
    }

    public List<HandlerErrorEntity> getErrors() {
        return errors;
    }

    public List<HandlerInputEntity> getInputs() {
        return inputs;
    }

    public List<HandlerOutputEntity> getOutputs() {
        return outputs;
    }
}
