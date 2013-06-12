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

import java.util.Date;

import org.apache.airavata.registry.api.workflow.ApplicationJob.ApplicationJobStatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationJobStatusData {
	private String jobId;
	private ApplicationJobStatus status;
	private Date time;
	
	public ApplicationJobStatusData(String jobId, ApplicationJobStatus status, Date time) {
		setJobId(jobId);
		setStatus(status);
		setTime(time);
	}
	
	public String getJobId() {
		return jobId;
	}
	private void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public ApplicationJobStatus getStatus() {
		return status;
	}
	private void setStatus(ApplicationJobStatus status) {
		this.status = status;
	}
	public Date getTime() {
		return time;
	}
	private void setTime(Date time) {
		this.time = time;
	}
}
