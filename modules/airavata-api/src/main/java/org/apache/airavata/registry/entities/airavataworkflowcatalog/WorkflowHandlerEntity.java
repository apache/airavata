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
package org.apache.airavata.registry.entities.airavataworkflowcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import org.apache.airavata.common.model.HandlerType;
import org.apache.airavata.registry.entities.InputDataEntity;
import org.apache.airavata.registry.entities.OutputDataEntity;

@Entity
@Table(name = "WORKFLOW_HANDLER")
@IdClass(WorkflowHandlerPK.class)
public class WorkflowHandlerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Id
    @Column(name = "WORKFLOW_ID", nullable = false)
    private String workflowId;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private HandlerType type;

    @Column(name = "CREATED_AT", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    @ManyToOne(targetEntity = AiravataWorkflowEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private AiravataWorkflowEntity workflow;

    /** Status and error records live in unified STATUS/ERROR tables; load via StatusRepository/ErrorRepository by parentId=id, parentType=HANDLER. */

    /**
     * Transient field for handler inputs. Inputs are stored in the unified INPUT_DATA table
     * and loaded via InputDataRepository.findByHandlerId().
     */
    @Transient
    private List<InputDataEntity> inputs;

    /**
     * Transient field for handler outputs. Outputs are stored in the unified OUTPUT_DATA table
     * and loaded via OutputDataRepository.findByHandlerId().
     */
    @Transient
    private List<OutputDataEntity> outputs;

    public WorkflowHandlerEntity() {}

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

    public void setInputs(List<InputDataEntity> inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(List<OutputDataEntity> outputs) {
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

    public List<InputDataEntity> getInputs() {
        return inputs;
    }

    public List<OutputDataEntity> getOutputs() {
        return outputs;
    }
}
