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

package org.apache.airavata.samples.registry.proveranance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.registry.api.AiravataProvenanceRegistry;
import org.apache.airavata.registry.api.impl.WorkflowExecutionImpl;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;

public class CustomProvenanceDataHandler extends AiravataProvenanceRegistry {
	private Map<String,List<WorkflowServiceIOData>> iData=new HashMap<String, List<WorkflowServiceIOData>>();
	private Map<String,List<WorkflowServiceIOData>> oData=new HashMap<String, List<WorkflowServiceIOData>>();
	
	private Map<String,String> metadata=new HashMap<String, String>();
	private Map<String,String> users=new HashMap<String, String>();
	
	private Map<String,WorkflowInstanceStatus> status=new HashMap<String, WorkflowInstanceStatus>();
	private Map<String,List<ActualParameter>> output=new HashMap<String, List<ActualParameter>>();
	private Map<String,Map<String,String>> outputValue=new HashMap<String, Map<String,String>>();

	private Map<String,String> name=new HashMap<String, String>();
	
	private List<WorkflowServiceIOData> getData(String expId,Map<String,List<WorkflowServiceIOData>> data){
		if (!data.containsKey(expId)){
			data.put(expId, new ArrayList<WorkflowServiceIOData>());
		}
		return data.get(expId);
	}
	
	private WorkflowInstanceStatus getStatus(String expId){
		if (status.containsKey(expId)){
			return status.get(expId);
		}
		return null;
	}
	
	private String getUser(String expId){
		return getString(expId,users);
	}
	
	private String getMetadata(String expId){
		return getString(expId,metadata);
	}
	
	private String getName(String expId){
		return getString(expId,name);
	}
	
	private String getString(String expId,Map<String,String> data){
		if (data.containsKey(expId)){
			return data.get(expId);
		}
		return null;
	}
	
	private List<ActualParameter> getOutput(String expId){
		if (!output.containsKey(expId)){
			output.put(expId, new ArrayList<ActualParameter>());
		}
		return output.get(expId);
	}
	
	private Map<String,String> getOutputValues(String expId){
		if (!outputValue.containsKey(expId)){
			outputValue.put(expId, new HashMap<String,String>());
		}
		return outputValue.get(expId);
	}
	
	private List<WorkflowServiceIOData> getIData(String expId){
		return getData(expId,iData);
	}
	
	private List<WorkflowServiceIOData> getOData(String expId){
		return getData(expId,oData);
	}
	
	public CustomProvenanceDataHandler(String user) {
		super(user);
	}

	private String makeId(WorkflowServiceIOData data){
		return data.getExperimentId()+":"+data.getWorkflowId()+":"+data.getNodeId();
	}
	
	@Override
	public String saveOutput(String workflowId, List<ActualParameter> parameters)
			throws RegistryException {
		output.put(workflowId,parameters);
		return workflowId;
	}

	@Override
	public List<ActualParameter> loadOutput(String workflowId)
			throws RegistryException {
		return getOutput(workflowId);
	}

	@Override
	public boolean saveWorkflowExecutionServiceInput(
			WorkflowServiceIOData workflowInputData) throws RegistryException {
		getIData(makeId(workflowInputData)).add(workflowInputData);
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionServiceOutput(
			WorkflowServiceIOData workflowOutputData) throws RegistryException {
		getOData(makeId(workflowOutputData)).add(workflowOutputData);
		return true;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		//right now just returning the first element
		return iData.values().iterator().next();
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		//right now just returning the first element
		return oData.values().iterator().next();
	}

	@Override
	public boolean saveWorkflowExecutionName(String experimentId,
			String workflowIntanceName) throws RegistryException {
		name.put(experimentId, workflowIntanceName);
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String experimentId,
			WorkflowInstanceStatus status) throws RegistryException {
		this.status.put(experimentId, status);
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String experimentId,
			ExecutionStatus status) throws RegistryException {
		this.status.put(experimentId, new WorkflowInstanceStatus(new WorkflowInstance(experimentId,experimentId),status));
		return true;
	}

	@Override
	public WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)
			throws RegistryException {
		return getStatus(experimentId);
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			String outputNodeName, String output) throws RegistryException {
		getOutputValues(experimentId).put(outputNodeName, output);
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			WorkflowIOData data) throws RegistryException {
		getOutputValues(experimentId).put(data.getNodeId(), data.getValue());
		return false;
	}

	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
			String outputNodeName) throws RegistryException {
		if (getOutputValues(experimentId).containsKey(outputNodeName)){
			return new WorkflowIOData(outputNodeName, getOutputValues(experimentId).get(outputNodeName));
		}
		return null;
	}

	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId)
			throws RegistryException {
		List<WorkflowIOData> result=new ArrayList<WorkflowIOData>();
		Map<String, String> outputValues = getOutputValues(experimentId);
		for (String v : outputValues.keySet()) {
			result.add(new WorkflowIOData(v,outputValues.get(v)));
		}
		return result;
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String exeperimentId)
			throws RegistryException {
		return getOutputValues(exeperimentId).keySet().toArray(new String[]{});
	}

	@Override
	public boolean saveWorkflowExecutionUser(String experimentId, String user)
			throws RegistryException {
		users.put(experimentId, user);
		return true;
	}

	@Override
	public String getWorkflowExecutionUser(String experimentId)
			throws RegistryException {
		return getUser(experimentId);
	}

	@Override
	public String getWorkflowExecutionName(String experimentId)
			throws RegistryException {
		return getName(experimentId);
	}

	@Override
	public WorkflowExecution getWorkflowExecution(String experimentId)
			throws RegistryException {
		WorkflowExecutionImpl w = new WorkflowExecutionImpl();
		w.setExperimentId(experimentId);
		w.setWorkflowInstanceName(getWorkflowExecutionName(experimentId));
		w.setExecutionStatus(getWorkflowExecutionStatus(experimentId));
		w.setMetadata(getWorkflowExecutionMetadata(experimentId));
		w.setOutput(getWorkflowExecutionOutput(experimentId));
		w.setServiceInput(searchWorkflowExecutionServiceInput(experimentId, ".*", ".*"));
		w.setServiceOutput(searchWorkflowExecutionServiceOutput(experimentId, ".*", ".*"));
		w.setUser(getWorkflowExecutionUser(experimentId));
		w.setTopic(experimentId);
		return w;
	}

	@Override
	public List<String> getWorkflowExecutionIdByUser(String user)
			throws RegistryException {
		//proper filtering needs to be done based on given the user
		return Arrays.asList(status.keySet().toArray(new String[]{}));
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String user)
			throws RegistryException {
		List<WorkflowExecution> result=new ArrayList<WorkflowExecution>();
		List<String> workflowExecutionIdByUser = getWorkflowExecutionIdByUser(user);
		for (String id : workflowExecutionIdByUser) {
			result.add(getWorkflowExecution(id));
		}
		return result;
	}

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
		List<WorkflowExecution> result=new ArrayList<WorkflowExecution>();
		List<String> workflowExecutionIdByUser = getWorkflowExecutionIdByUser(user);
		int startIndex=pageSize*(pageNo-1);
		int endIndex=startIndex+pageSize-1;
		for(int i=startIndex;i<workflowExecutionIdByUser.size() && i<=endIndex;i++){
			result.add(getWorkflowExecution(workflowExecutionIdByUser.get(i)));
		}
		return result;
	}

	@Override
	public String getWorkflowExecutionMetadata(String experimentId)
			throws RegistryException {
		return getMetadata(experimentId);
	}

	@Override
	public boolean saveWorkflowExecutionMetadata(String experimentId,
			String metadata) throws RegistryException {
		this.metadata.put(experimentId, metadata);
		return true;
	}

}
