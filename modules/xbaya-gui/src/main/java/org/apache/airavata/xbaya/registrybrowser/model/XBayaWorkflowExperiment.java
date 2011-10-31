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

package org.apache.airavata.xbaya.registrybrowser.model;

import java.util.ArrayList;
import java.util.List;

public class XBayaWorkflowExperiment {
	private List<XBayaWorkflow> workflows;
	private String experimentId;
	
	public XBayaWorkflowExperiment(String experimentId, List<XBayaWorkflow> workflows) {
		setWorkflows(workflows);
		setExperimentId(experimentId);
	}

	public List<XBayaWorkflow> getWorkflows() {
		if (workflows==null){
			workflows=new ArrayList<XBayaWorkflow>();
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
}
