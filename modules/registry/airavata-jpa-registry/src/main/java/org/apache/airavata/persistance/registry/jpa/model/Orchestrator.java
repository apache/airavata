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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "Orchestrator")
public class Orchestrator {
	
	@Id
    private String experiment_ID;
	private String username;
	private String status;
	private String state;
	private String gfacEPR;
	private String applicationName;
	@Lob
	private String jobRequest;
    private Timestamp  submitted_time;
    private Timestamp  status_update_time;
	
	public String getExperiment_ID() {
		return experiment_ID;
	}
	public void setExperiment_ID(String experiment_ID) {
		this.experiment_ID = experiment_ID;
	}
	
	public String getUserName() {
		return username;
	}
	public void setUserName(String username) {
		this.username = username;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getGfacEPR() {
		return gfacEPR;
	}
	public void setGfacEPR(String gfacEPR) {
		this.gfacEPR = gfacEPR;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getJobRequest() {
		return jobRequest;
	}
	public void setJobRequest(String jobRequest) {
		this.jobRequest = jobRequest;
	}
	public Timestamp getSubmittedTime() {
		return submitted_time;
	}
	public void setSubmittedTime(Timestamp submitted_time) {
		this.submitted_time = submitted_time;
	}
	public Timestamp getStatusUpdateTime() {
		return status_update_time;
	}
	public void setStatusUpdateTime(Timestamp status_update_time) {
		this.status_update_time = status_update_time;
	}
	

}
