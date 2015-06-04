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

package org.apache.airavata.registry.core.experiment.catalog.model;

import org.apache.openjpa.persistence.DataCache;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@DataCache
@Entity
@Table(name = "WORKFLOW_NODE_DETAIL")
public class WorkflowNodeDetail implements Serializable {
    @Column(name = "EXPERIMENT_ID")
    private String expId;
    @Id
    @Column(name = "NODE_INSTANCE_ID")
    private String nodeId;
    @Column(name = "EXECUTION_UNIT")
    private String executionUnit;
    @Column(name = "EXECUTION_UNIT_DATA")
    private String executionUnitData;
    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;
    @Column(name = "NODE_NAME")
    private String nodeName;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "EXPERIMENT_ID")
    private Experiment experiment;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "nodeDetail")
    private List<TaskDetail> taskDetails;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "nodeDetail")
    private List<NodeInput> nodeInputs;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "nodeDetail")
    private List<NodeOutput> nodeOutputs;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "nodeDetail")
    private Status nodeStatus;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "nodeDetail")
    private List<ErrorDetail> errorDetails;

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

	public String getExecutionUnitData() {
		return executionUnitData;
	}

	public void setExecutionUnitData(String executionUnitData) {
		this.executionUnitData = executionUnitData;
	}

	public String getExecutionUnit() {
		return executionUnit;
	}

	public void setExecutionUnit(String executionUnit) {
		this.executionUnit = executionUnit;
	}

    public List<TaskDetail> getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(List<TaskDetail> taskDetails) {
        this.taskDetails = taskDetails;
    }

    public List<NodeInput> getNodeInputs() {
        return nodeInputs;
    }

    public void setNodeInputs(List<NodeInput> nodeInputs) {
        this.nodeInputs = nodeInputs;
    }

    public List<NodeOutput> getNodeOutputs() {
        return nodeOutputs;
    }

    public void setNodeOutputs(List<NodeOutput> nodeOutputs) {
        this.nodeOutputs = nodeOutputs;
    }

    public Status getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(Status nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public List<ErrorDetail> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<ErrorDetail> errorDetails) {
        this.errorDetails = errorDetails;
    }
}
