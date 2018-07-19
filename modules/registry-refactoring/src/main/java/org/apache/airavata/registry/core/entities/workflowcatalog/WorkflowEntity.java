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
 *
*/
package org.apache.airavata.registry.core.entities.workflowcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;

import java.nio.ByteBuffer;
import java.util.List;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the workflow database table.
 */
@Entity
@Table(name="WORKFLOW")
public class WorkflowEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "GRAPH")
    private String graph;

    @Column(name = "IMAGE")
    private ByteBuffer image;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "WORKFLOW_NAME")
    private String name;

    @OneToMany(targetEntity = WorkflowInputEntity.class, cascade = CascadeType.ALL,
            mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<WorkflowInputEntity> workflowInputs;

    @OneToMany(targetEntity = WorkflowOutputEntity.class, cascade = CascadeType.ALL,
            mappedBy = "workflow", fetch = FetchType.EAGER)
    private List<WorkflowOutputEntity> workflowOutputs;

    public WorkflowEntity() {
    }

    public String getTemplateId() {

        return this.templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    public String getCreatedUser() {

        return this.createdUser;
    }

    public void setCreatedUser(String createdUser) {

        this.createdUser = createdUser;
    }

    public Timestamp getCreationTime() {

        return this.creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {

        this.creationTime = creationTime;
    }

    public String getGatewayId() {

        return this.gatewayId;
    }

    public void setGatewayId(String gatewayId) {

        this.gatewayId = gatewayId;
    }

    public String getGraph() {

        return this.graph;
    }

    public void setGraph(String graph) {

        this.graph = graph;
    }

    public ByteBuffer getImage() {

        return this.image;
    }

    public void setImage(ByteBuffer image) {

        this.image = image;
    }

    public Timestamp getUpdateTime() {

        return this.updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {

        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WorkflowInputEntity> getWorkflowInputs() {
        return workflowInputs;
    }

    public void setWorkflowInputs(List<WorkflowInputEntity> workflowInputs) {
        this.workflowInputs = workflowInputs;
    }

    public List<WorkflowOutputEntity> getWorkflowOutputs() {
        return workflowOutputs;
    }

    public void setWorkflowOutputs(List<WorkflowOutputEntity> workflowOutputs) {
        this.workflowOutputs = workflowOutputs;
    }
}