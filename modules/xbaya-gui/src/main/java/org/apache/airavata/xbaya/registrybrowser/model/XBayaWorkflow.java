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

public class XBayaWorkflow {
	private List<XBayaWorkflowService> workflowServices;
	private String workflowId;
	private String workflowName;
	
	public XBayaWorkflow(String workflowId, String workflowName, List<XBayaWorkflowService> workflowServices) {
		setWorkflowId(workflowId);
		setWorkflowName(workflowName);
		setWorkflowServices(workflowServices);
	}

	public List<XBayaWorkflowService> getWorkflowServices() {
		if (workflowServices==null){
			workflowServices=new ArrayList<XBayaWorkflowService>();
		}
		return workflowServices;
	}

	public void setWorkflowServices(List<XBayaWorkflowService> workflowServices) {
		this.workflowServices = workflowServices;
	}
	
	public void add(XBayaWorkflowService workflowService){
		getWorkflowServices().add(workflowService);
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}
}
