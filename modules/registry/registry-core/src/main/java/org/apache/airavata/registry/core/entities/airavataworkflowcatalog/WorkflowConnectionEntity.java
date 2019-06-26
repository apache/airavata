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

import org.apache.airavata.model.workflow.ComponentType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "WORKFLOW_CONNECTION")
@IdClass(WorkflowConnectionPK.class)
public class WorkflowConnectionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Id
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    @Column(name = "DATA_BLOCK_ID")
    private String dataBlockId;

    @Column(name = "FROM_TYPE")
    @Enumerated(EnumType.STRING)
    private ComponentType fromType;

    @Column(name = "FROM_ID")
    private String fromId;

    @Column(name = "FROM_OUTPUT_NAME")
    private String fromOutputName;

    @Column(name = "TO_TYPE")
    @Enumerated(EnumType.STRING)
    private ComponentType toType;

    @Column(name = "TO_ID")
    private String toId;

    @Column(name = "TO_INPUT_NAME")
    private String toInputName;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @ManyToOne(targetEntity = AiravataWorkflowEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "ID")
    private AiravataWorkflowEntity workflow;

    @ManyToOne(targetEntity = WorkflowDataBlockEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "DATA_BLOCK_ID", referencedColumnName = "ID")
    private WorkflowDataBlockEntity dataBlock;

    public WorkflowConnectionEntity() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setDataBlockId(String dataBlockId) {
        this.dataBlockId = dataBlockId;
    }

    public void setFromType(ComponentType fromType) {
        this.fromType = fromType;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public void setFromOutputName(String fromOutputName) {
        this.fromOutputName = fromOutputName;
    }

    public void setToType(ComponentType toType) {
        this.toType = toType;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public void setToInputName(String toInputName) {
        this.toInputName = toInputName;
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

    public void setDataBlock(WorkflowDataBlockEntity dataBlock) {
        this.dataBlock = dataBlock;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getDataBlockId() {
        return dataBlockId;
    }

    public ComponentType getFromType() {
        return fromType;
    }

    public String getFromId() {
        return fromId;
    }

    public String getFromOutputName() {
        return fromOutputName;
    }

    public ComponentType getToType() {
        return toType;
    }

    public String getToId() {
        return toId;
    }

    public String getToInputName() {
        return toInputName;
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

    public WorkflowDataBlockEntity getDataBlock() {
        return dataBlock;
    }
}
