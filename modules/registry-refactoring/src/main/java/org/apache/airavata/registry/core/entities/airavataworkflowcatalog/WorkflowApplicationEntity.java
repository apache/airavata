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

import org.apache.airavata.registry.core.entities.expcatalog.ProcessEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "WORKFLOW_APPLICATION")
@IdClass(WorkflowApplicationPK.class)
public class WorkflowApplicationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Id
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    @Column(name = "PROCESS_ID")
    private String processId;

    @Column(name = "APPLICATION_INTERFACE_ID")
    private String applicationInterfaceId;

    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Column(name = "QUEUE_NAME")
    private String queueName;

    @Column(name = "NODE_COUNT")
    private int nodeCount;

    @Column(name = "CORE_COUNT")
    private int coreCount;

    @Column(name = "WALL_TIME_LIMIT")
    private int wallTimeLimit;

    @Column(name = "PHYSICAL_MEMORY")
    private int physicalMemory;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Column(name = "UPDATED_AT")
    private Timestamp updatedAt;

    @ManyToOne(targetEntity = AiravataWorkflowEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "WORKFLOW_ID", referencedColumnName = "ID")
    private AiravataWorkflowEntity workflow;

    @OneToMany(targetEntity = ApplicationStatusEntity.class, cascade = CascadeType.ALL, mappedBy = "application", fetch = FetchType.EAGER)
    private List<ApplicationStatusEntity> statuses;

    @OneToMany(targetEntity = ApplicationErrorEntity.class, cascade = CascadeType.ALL, mappedBy = "application", fetch = FetchType.EAGER)
    private List<ApplicationErrorEntity> errors;

    public WorkflowApplicationEntity() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    public void setWallTimeLimit(int wallTimeLimit) {
        this.wallTimeLimit = wallTimeLimit;
    }

    public void setPhysicalMemory(int physicalMemory) {
        this.physicalMemory = physicalMemory;
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

    public void setStatuses(List<ApplicationStatusEntity> statuses) {
        this.statuses = statuses;
    }

    public void setErrors(List<ApplicationErrorEntity> errors) {
        this.errors = errors;
    }

    public String getId() {
        return id;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getProcessId() {
        return processId;
    }

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public int getWallTimeLimit() {
        return wallTimeLimit;
    }

    public int getPhysicalMemory() {
        return physicalMemory;
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

    public List<ApplicationStatusEntity> getStatuses() {
        return statuses;
    }

    public List<ApplicationErrorEntity> getErrors() {
        return errors;
    }
}
