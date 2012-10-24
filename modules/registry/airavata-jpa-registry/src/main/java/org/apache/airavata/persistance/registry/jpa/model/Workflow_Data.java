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
public class Workflow_Data {

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name="experiment_ID")
    private Experiment_Data experiment_data;

	@Id
	private String workflow_instanceID;
	private String template_name;
	private String status;
	private Timestamp start_time;
	private Timestamp last_update_time;


	public String getWorkflow_instanceID() {
		return workflow_instanceID;
	}

	public void setWorkflow_instanceID(String workflow_instanceID) {
		this.workflow_instanceID = workflow_instanceID;
	}

	public String getTemplate_name() {
		return template_name;
	}

	public void setTemplate_name(String template_name) {
		this.template_name = template_name;
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

    public Experiment_Data getExperiment_data() {
        return experiment_data;
    }

    public void setExperiment_data(Experiment_Data experiment_data) {
        this.experiment_data = experiment_data;
    }
}
