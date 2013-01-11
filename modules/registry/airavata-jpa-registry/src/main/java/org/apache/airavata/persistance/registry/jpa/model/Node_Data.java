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
@IdClass(Node_DataPK.class)
public class Node_Data {

	@Id
    private String workflow_instanceID;

	@ManyToOne()
	@JoinColumn(name = "workflow_instanceID")
	private Workflow_Data workflow_Data;

	@Id
	private String node_id;

    @Id
    private int execution_index;

	private String node_type;
	@Lob
    private byte[] inputs;
	@Lob
    private byte[] outputs;
	private String status;
	private Timestamp start_time;
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

