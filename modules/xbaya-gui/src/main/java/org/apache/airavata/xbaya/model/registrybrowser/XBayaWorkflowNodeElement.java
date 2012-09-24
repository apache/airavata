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

import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodeData;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNodePortData;
import org.apache.airavata.schemas.gfac.Parameter;

public class XBayaWorkflowNodeElement {
	private InputParameters inputParameters;
	private OutputParameters outputParameters;
	private WorkflowInstanceNodeData nodeData;
	private String nodeId;
	
	public XBayaWorkflowNodeElement(String nodeId, WorkflowInstanceNodeData nodeData) {
		setNodeId(nodeId);
		setNodeData(nodeData);
	}

	public OutputParameters getOutputParameters() {
		if (outputParameters==null){
			outputParameters=new OutputParameters((NodeParameter[])null);
			outputParameters.getParameters().addAll(generateParameterList(nodeData.getOutputData()));
		}
		return outputParameters;
	}

	private List<NodeParameter> generateParameterList(
			List<WorkflowInstanceNodePortData> outputData) {
		List<NodeParameter> params=new ArrayList<NodeParameter>();
		for (WorkflowInstanceNodePortData portData : outputData) {
			Parameter parameter = Parameter.Factory.newInstance();
			parameter.setParameterName(portData.getName());
			NodeParameter serviceParameter = new NodeParameter(parameter, portData.getValue());
			params.add(serviceParameter);
		}
		return params;
	}

	public void setOutputParameters(OutputParameters outputParameters) {
		this.outputParameters = outputParameters;
	}

	public InputParameters getInputParameters() {
		if (inputParameters==null){
			inputParameters=new InputParameters((NodeParameter[])null);
			inputParameters.getParameters().addAll(generateParameterList(nodeData.getInputData()));
		}
		return inputParameters;
	}

	public void setInputParameters(InputParameters inputParameters) {
		this.inputParameters = inputParameters;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public WorkflowInstanceNodeData getNodeData() {
		return nodeData;
	}

	public void setNodeData(WorkflowInstanceNodeData nodeData) {
		this.nodeData = nodeData;
	}
}
