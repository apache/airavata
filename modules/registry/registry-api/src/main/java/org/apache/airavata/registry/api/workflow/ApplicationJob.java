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

package org.apache.airavata.registry.api.workflow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationJob {
	public static enum ApplicationJobStatus{
		AUTHENTICATE, //authenticating
		SUBMITTED, //job is submitted, possibly waiting to start executing
		INITIALIZE, //job is being initialized 
		PENDING, //job is pending to be started
		EXECUTING, //submitted job is being executed
		SUSPENDED, //job was paused
		WAITING_FOR_DATA, // job is waiting for data to continue executing
		FINALIZE, //job is being initialized 
		FINISHED, // job completed successfully
		FAILED, // error occurred while job was executing and the job stopped
		CANCELLED, //job was cancelled
		UNKNOWN // unknown status. lookup the metadata for more details.
	}
	
	private String experimentId;
	private String workflowExecutionId;
	private String nodeId;
	
	private String serviceDescriptionId;
	private String hostDescriptionId;
	private String applicationDescriptionId;
	
	private String jobId;
	private String jobData;
	
	private Date submittedTime;
	private Date statusUpdateTime;
	private ApplicationJobStatus jobStatus;
	
	private String metadata;

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getWorkflowExecutionId() {
		return workflowExecutionId;
	}

	public void setWorkflowExecutionId(String workflowExecutionId) {
		this.workflowExecutionId = workflowExecutionId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getServiceDescriptionId() {
		return serviceDescriptionId;
	}

	public void setServiceDescriptionId(String serviceDescriptionId) {
		this.serviceDescriptionId = serviceDescriptionId;
	}

	public String getHostDescriptionId() {
		return hostDescriptionId;
	}

	public void setHostDescriptionId(String hostDescriptionId) {
		this.hostDescriptionId = hostDescriptionId;
	}

	public String getApplicationDescriptionId() {
		return applicationDescriptionId;
	}

	public void setApplicationDescriptionId(String applicationDescriptionId) {
		this.applicationDescriptionId = applicationDescriptionId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobData() {
		return jobData;
	}

	public void setJobData(String jobData) {
		this.jobData = jobData;
	}

	public Date getSubmittedTime() {
		return submittedTime;
	}

	public void setSubmittedTime(Date submittedTime) {
		this.submittedTime = submittedTime;
	}

	public Date getStatusUpdateTime() {
		return statusUpdateTime;
	}

	public void setStatusUpdateTime(Date statusUpdateTime) {
		this.statusUpdateTime = statusUpdateTime;
	}

	public ApplicationJobStatus getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(ApplicationJobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
}
