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
	/**
	 * Represents the status of the application job execution life cycle.<br /> 
	 * <em><strong>Note</strong> : The applicable <code>ApplicationJobStatus</code> values and the 
	 * particular actions that define or lead to those <code>ApplicationJobStatus</code> values is 
	 * based on type of application (eg: GRAM, EC2) being executed</em>.   
	 */
	public static enum ApplicationJobStatus{

        /**
         * Job not yet submitted to Gram
         */
        UN_SUBMITTED,
		/**
		 * Validating the application job input data and configurations
		 */
		VALIDATE_INPUT,
		/**
		 * Input data/files is being staged for the application job.
		 */
		STAGING,
		/**
		 * Authenticating
		 */
		AUTHENTICATE,
		/**
		 * Application job is being initialized.
		 */
		INITIALIZE, 
		/**
		 * Application job is submitted, possibly waiting to start executing.
		 */
		SUBMITTED,
		/**
		 * Application job is waiting to start/continue its executing.
		 */
		PENDING,
		/**
		 * Application job is being executed.
		 */
		EXECUTING,
        /**
         * Application job is being active.
         */
        ACTIVE,
        /**
		 * Application job is paused/suspended
		 */
		SUSPENDED,
		/**
		 * Application job is waiting for data or a trigger to continue its execution.
		 */
		WAIT_FOR_DATA,
		/**
		 * Finalizing the execution of the application job.
		 */
		FINALIZE,
		/**
		 * Results of the application job execution are being generated.
		 */
		RESULTS_GEN,
		/**
		 * Generated results from the application job execution is being retrieved.
		 */
		RESULTS_RETRIEVE,
		/**
		 * Validating the application job execution results
		 */
		VALIDATE_OUTPUT,
		/**
		 * Application job completed successfully.
		 */
		FINISHED,
		/**
		 * Error occurred during the application job execution and the job was terminated.
		 */
		FAILED, 
		/**
		 * Execution of the application job was cancelled.
		 */
		CANCELLED,
        /**
         * Execution of the application job was cancelled.
         */
        CANCELED,
		/**
		 * Unable to determine the current status of the application job. <br />
		 * <em><strong>Note: </strong>More information may be available on the application job 
		 * </em><code>metadata</code>.
		 */
		UNKNOWN 
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
	private ApplicationJobStatus status;
	
	private String metadata;

	/**
	 * The id of the experiment which this application job corresponds to
	 * @return
	 */
	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	/**
	 * The id of the workflow instance execution which this application job corresponds to
	 */
	public String getWorkflowExecutionId() {
		return workflowExecutionId;
	}

	public void setWorkflowExecutionId(String workflowExecutionId) {
		this.workflowExecutionId = workflowExecutionId;
	}

	/**
	 * The id of the node which this application job corresponds to
	 */
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * The id of the service description which this application job corresponds to
	 */
	public String getServiceDescriptionId() {
		return serviceDescriptionId;
	}

	public void setServiceDescriptionId(String serviceDescriptionId) {
		this.serviceDescriptionId = serviceDescriptionId;
	}
	
	/**
	 * The id of the host description which this application job corresponds to <br />
	 * <em><strong>Note: </strong>For data saved using the deprecated API function 
	 * {@code updateWorkflowNodeGramData(...)} this will be the address of the host</em>
	 */
	public String getHostDescriptionId() {
		return hostDescriptionId;
	}

	public void setHostDescriptionId(String hostDescriptionId) {
		this.hostDescriptionId = hostDescriptionId;
	}

	/**
	 * The id of the application description which this application job corresponds to 
	 */
	public String getApplicationDescriptionId() {
		return applicationDescriptionId;
	}

	public void setApplicationDescriptionId(String applicationDescriptionId) {
		this.applicationDescriptionId = applicationDescriptionId;
	}

	/**
	 * id representing the application job uniquely identified in the Airavata system <br />
	 * <em><strong>Note: </strong>This id may or may not correspond to an id that can identify a 
	 * resource execution in the computational middleware</em>
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * Set a unique id which represents this job in the Airavata system. 
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * Configuration, execution and input data relating to the execution of the application job. <br /> 
	 * <em><strong>Note: </strong>The structure of the data is determined by the type of application 
	 * <code>(eg: GRAM, EC2) being executed.</code></em> 
	 */
	public String getJobData() {
		return jobData;
	}

	/**
	 * Set the configuration, execution and input data relating to the execution of the application. 
	 * job. <br /> 
	 * <em><strong>Note: </strong>The structure of the data is up to the Provider implementation 
	 * <code>(eg: GRAMProvider, EC2Provider)</code>. It is strongly encouraged to include in this 
	 * field all the information (excluding descriptor data & any sensitive data such as password 
	 * credentials) necessary for a 3rd party to repeat the execution of application job if 
	 * necessary.</em> 
	 */
	public void setJobData(String jobData) {
		this.jobData = jobData;
	}

	/**
	 * When was this application job was submitted.
	 */
	public Date getSubmittedTime() {
		return submittedTime;
	}

	public void setSubmittedTime(Date submittedTime) {
		this.submittedTime = submittedTime;
	}

	/**
	 * When was the status of this application job was last updated.
	 * @return
	 */
	public Date getStatusUpdateTime() {
		return statusUpdateTime;
	}

	public void setStatusUpdateTime(Date statusUpdateTime) {
		this.statusUpdateTime = statusUpdateTime;
	}

	/**
	 * Get the currently recorded status of the application job. 
	 * @return
	 */
	public ApplicationJobStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationJobStatus status) {
		this.status = status;
	}

	/**
	 * Custom metadata maintained for the application job containing that may contain any additional 
	 * information relating to the execution.
	 * @return
	 */
	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
}
