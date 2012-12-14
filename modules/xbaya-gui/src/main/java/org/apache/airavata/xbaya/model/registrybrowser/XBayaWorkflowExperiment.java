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

package org.apache.airavata.xbaya.model.registrybrowser;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.exception.RegistryException;
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;

public class XBayaWorkflowExperiment {
	private List<XBayaWorkflow> workflows;
	private String experimentId;
	private AiravataAPI airavataAPI;
	
	public XBayaWorkflowExperiment(String experimentId, AiravataAPI airavataAPI) {
		setExperimentId(experimentId);
		setAiravataAPI(airavataAPI);
	}

	public List<XBayaWorkflow> getWorkflows() {
		if (workflows==null){
			workflows=new ArrayList<XBayaWorkflow>();
			try {
				List<WorkflowExecution> experimentWorkflowInstances = getAiravataAPI().getProvenanceManager().getExperimentWorkflowInstances(getExperimentId());
				for (WorkflowExecution workflowInstance : experimentWorkflowInstances) {
					workflows.add(new XBayaWorkflow(workflowInstance, getAiravataAPI()));
				}
			}  catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            }
        }
		return workflows;
	}

	public void setWorkflows(List<XBayaWorkflow> workflows) {
		this.workflows = workflows;
	}
	
	public void add(XBayaWorkflow workflow){
		getWorkflows().add(workflow);
	}

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }
}
