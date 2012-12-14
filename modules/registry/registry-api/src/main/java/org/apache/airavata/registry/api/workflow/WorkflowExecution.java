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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkflowExecution {
	private String experimentId;
	private String workflowExecutionId;
	private String templateName;

    public WorkflowExecution() {
    }

    public WorkflowExecution(String experimentId,String instanceId) {
		setExperimentId(experimentId);
		setWorkflowExecutionId(instanceId);
	}

    public WorkflowExecution(String experimentId, String workflowInstanceId, String templateName) {
        this.experimentId = experimentId;
        this.workflowExecutionId = workflowInstanceId;
        this.templateName = templateName;
    }

    public String getWorkflowExecutionId() {
		return workflowExecutionId;
	}

	public void setWorkflowExecutionId(String workflowExecutionId) {
		this.workflowExecutionId = workflowExecutionId;
	}

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
}
