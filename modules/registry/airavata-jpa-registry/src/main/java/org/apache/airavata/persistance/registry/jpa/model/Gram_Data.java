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

import javax.persistence.*;

@Entity
@IdClass(Gram_DataPK.class)
public class Gram_Data {

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "workflow_instanceID")
	private Workflow_Data workflow_Data;

    @Id
    private String workflow_instanceID;
	@Id
	private String node_id;

    @Lob
	private byte[] rsl;
	private String invoked_host;
    private String local_Job_ID;

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

	public byte[] getRsl() {
		return rsl;
	}

	public void setRsl(byte[] rsl) {
		this.rsl = rsl;
	}

	public String getInvoked_host() {
		return invoked_host;
	}

	public void setInvoked_host(String invoked_host) {
		this.invoked_host = invoked_host;
	}

    public String getLocal_Job_ID() {
        return local_Job_ID;
    }

    public void setLocal_Job_ID(String local_Job_ID) {
        this.local_Job_ID = local_Job_ID;
    }

    public String getWorkflow_instanceID() {
        return workflow_instanceID;
    }

    public void setWorkflow_instanceID(String workflow_instanceID) {
        this.workflow_instanceID = workflow_instanceID;
    }
}

