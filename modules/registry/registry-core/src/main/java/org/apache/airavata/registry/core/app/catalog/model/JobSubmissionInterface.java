/**
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
package org.apache.airavata.registry.core.app.catalog.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "JOB_SUBMISSION_INTERFACE")
@IdClass(JobSubmissionInterface_PK.class)
public class JobSubmissionInterface implements Serializable {
	
	@Id
	@Column(name = "JOB_SUBMISSION_INTERFACE_ID")
	private String jobSubmissionInterfaceId;
	
	@Id
	@Column(name = "COMPUTE_RESOURCE_ID")
	private String computeResourceId;
	
	@ManyToOne(cascade= CascadeType.MERGE)
	@JoinColumn(name = "COMPUTE_RESOURCE_ID")
	private ComputeResource computeResource;
	
	@Column(name = "JOB_SUBMISSION_PROTOCOL")
	private String jobSubmissionProtocol;
	
	@Column(name = "PRIORITY_ORDER")
	private int priorityOrder;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }


    public String getJobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}
	
	public String getComputeResourceId() {
		return computeResourceId;
	}
	
	public ComputeResource getComputeResource() {
		return computeResource;
	}
	
	public String getJobSubmissionProtocol() {
		return jobSubmissionProtocol;
	}
	
	public int getPriorityOrder() {
		return priorityOrder;
	}
	
	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId=jobSubmissionInterfaceId;
	}
	
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId=computeResourceId;
	}
	
	public void setComputeResource(ComputeResource computeResource) {
		this.computeResource=computeResource;
	}
	
	public void setJobSubmissionProtocol(String jobSubmissionProtocol) {
		this.jobSubmissionProtocol=jobSubmissionProtocol;
	}
	
	public void setPriorityOrder(int priorityOrder) {
		this.priorityOrder=priorityOrder;
	}
}
