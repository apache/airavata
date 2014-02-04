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
package org.apache.airavata.persistance.registry.jpa.model;

import java.sql.Timestamp;

import javax.persistence.*;

@Entity
@Table(name ="NODE_DATA")
@IdClass(Node_DataPK.class)
public class Node_Data {

	@Id
    @Column(name = "WORKFLOW_INSTANCE_ID")
    private String workflow_instanceID;

	@ManyToOne()
	@JoinColumn(name = "WORKFLOW_INSTANCE_ID")
	private Workflow_Data workflow_Data;

	@Id
    @Column(name = "NODE_ID")
	private String node_id;

    @Id
    @Column(name = "EXECUTION_INDEX")
    private int execution_index;

    @Column(name = "NODE_TYPE")
	private String node_type;
	@Lob
    @Column(name = "INPUTS")
    private byte[] inputs;
	@Lob
    @Column(name = "OUTPUTS")
    private byte[] outputs;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "START_TIME")
    private Timestamp start_time;
    @Column(name = "LAST_UPDATE_TIME")
    private Timestamp last_update_time;

	public Workflow_Data getWorkflow_Data() {
		return workflow_Data;
	}

	public void setWorkflow_Data(Workflow_Data workflow_Data) {
		this.workflow_Data = workflow_Data;
	}

	public String getNode_id() {
		return node_id;
	}

	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}

	public String getNode_type() {
		return node_type;
	}

	public void setNode_type(String node_type) {
		this.node_type = node_type;
	}

	public byte[] getInputs() {
		return inputs;
	}

	public void setInputs(byte[] inputs) {
		this.inputs = inputs;
	}

	public byte[] getOutputs() {
		return outputs;
	}

	public void setOutputs(byte[] outputs) {
		this.outputs = outputs;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getStart_time() {
		return start_time;
	}

	public void setStart_time(Timestamp start_time) {
		this.start_time = start_time;
	}

	public Timestamp getLast_update_time() {
		return last_update_time;
	}

	public void setLast_update_time(Timestamp last_update_time) {
		this.last_update_time = last_update_time;
	}

    public String getWorkflow_instanceID() {
        return workflow_instanceID;
    }

    public void setWorkflow_instanceID(String workflow_instanceID) {
        this.workflow_instanceID = workflow_instanceID;
    }

    public int getExecution_index() {
        return execution_index;
    }

    public void setExecution_index(int execution_index) {
        this.execution_index = execution_index;
    }
}

