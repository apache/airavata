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

package org.apache.airavata.client.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.client.AiravataClient;
import org.apache.airavata.client.AiravataClientConfiguration;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.ExecutionManager;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.stub.interpretor.NameValue;
import org.apache.airavata.client.stub.interpretor.WorkflowInterpretorStub;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.Monitor;
import org.apache.airavata.ws.monitor.MonitorEventListener;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;

public class ExecutionManagerImpl implements ExecutionManager {
	private AiravataClient client;

	public ExecutionManagerImpl(AiravataClient client) {
		setClient(client);
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs) throws AiravataAPIInvocationException {
		return runExperiment(workflowTemplateId, inputs ,getClient().getCurrentUser(),null, workflowTemplateId+"_"+Calendar.getInstance().getTime().toString());
	}

	@Override
	public String runExperiment(Workflow workflow, List<WorkflowInput> inputs)
			throws AiravataAPIInvocationException {
		return runExperiment(workflow,inputs, getClient().getCurrentUser(),null);
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata, String workflowInstanceName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflowTemplateId, inputs, user, metadata, workflowInstanceName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}

	}

	@Override
	public String runExperiment(Workflow workflow, List<WorkflowInput> inputs,
			String user, String metadata) throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflow, inputs, user, metadata,workflow.getName()+"_"+Calendar.getInstance().getTime().toString());
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public Monitor getExperimentMonitor(String experimentId)
			throws AiravataAPIInvocationException {
		return getClient().getWorkflowExecutionMonitor(experimentId);
	}

	@Override
	public Monitor getExperimentMonitor(String experimentId,
			MonitorEventListener listener)
			throws AiravataAPIInvocationException {
		return getClient().getWorkflowExecutionMonitor(experimentId,listener);
	}

	public AiravataClient getClient() {
		return client;
	}
	public void setClient(AiravataClient client) {
		this.client = client;
	}

	@Override
	public String runExperiment(String workflowTemplateId,
			List<WorkflowInput> inputs, String user, String metadata,
			String workflowInstanceName, WorkflowContextHeaderBuilder builder)
			throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflowTemplateId, inputs, user, metadata, workflowInstanceName,builder);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public WorkflowContextHeaderBuilder createWorkflowContextHeader()
			throws AiravataAPIInvocationException {
		AiravataClientConfiguration config = getClient().getClientConfiguration();
		try {
			return new WorkflowContextHeaderBuilder(config.getMessagebrokerURL().toString(),
					config.getGfacURL().toString(),config.getRegistryURL().toString(),null,null,
					config.getMessageboxURL().toString());
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String runExperiment(String workflowName,
			List<WorkflowInput> inputs, String user, String metadata,
			String workflowInstanceName, String experimentName)
			throws AiravataAPIInvocationException {
		try {
			return getClient().runWorkflow(workflowName, inputs, user, metadata, workflowInstanceName,experimentName);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}

	@Override
	public String runExperiment(String workflow,
			List<WorkflowInput> inputs, ExperimentAdvanceOptions options)
			throws AiravataAPIInvocationException {
		return runWorkflow(workflow, inputs, options);
	}
	
	private String runWorkflow(String workflowName, List<WorkflowInput> inputs, ExperimentAdvanceOptions options) throws AiravataAPIInvocationException {
		Workflow workflowObj = extractWorkflow(workflowName);
		return runWorkflow(workflowName, workflowObj, inputs, options);
	}
	
	private String runWorkflow(String workflowName, Workflow workflowObj, List<WorkflowInput> inputs, ExperimentAdvanceOptions options) throws AiravataAPIInvocationException {
		try {
	        
			String workflowString = XMLUtil.xmlElementToString(workflowObj.toXML());
			List<WSComponentPort> ports = getWSComponentPortInputs(workflowObj);
			for (WorkflowInput input : inputs) {
				WSComponentPort port = getWSComponentPort(input.getName(),
						ports);
				if (port != null) {
					port.setValue(input.getValue());
				}
			}
			List<NameValue> inputValues = new ArrayList<NameValue>();
			for (WSComponentPort port : ports) {
				NameValue nameValue = new NameValue();
				nameValue.setName(port.getName());
				if (port.getValue() == null) {
					nameValue.setValue(port.getDefaultValue());
				} else {
					nameValue.setValue(port.getValue().toString());
				}
				inputValues.add(nameValue);
			}
			String experimentID=options.getCustomExperimentId();
			String workflowTemplateName = workflowObj.getName();
			if (experimentID == null || experimentID.isEmpty()) {
				experimentID = workflowTemplateName + "_" + UUID.randomUUID();
			}
	        getClient().getProvenanceManager().setWorkflowInstanceTemplateName(experimentID,workflowTemplateName);
	        
			WorkflowContextHeaderBuilder builder=options.getCustomWorkflowContext();
			if (builder==null){
				builder=options.newCustomWorkflowContext();
			}
			
			//TODO - fix user passing
	        String submissionUser = getClient().getUserManager().getAiravataUser();
			builder.setUserIdentifier(submissionUser);
			String executionUser=options.getExperimentExecutionUser();
			if (executionUser==null){
				executionUser=submissionUser;
			}
			runPreWorkflowExecutionTasks(experimentID, executionUser, options.getExperimentMetadata(), options.getExperimentName());
			NameValue[] inputVals = inputValues.toArray(new NameValue[] {});
			launchWorkflow(experimentID, workflowString, inputVals, builder);
			return experimentID;
			
		}  catch (GraphException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (ComponentException e) {
			throw new AiravataAPIInvocationException(e);
		} catch (Exception e) {
	        throw new AiravataAPIInvocationException("Error working with Airavata Registry: " + e.getLocalizedMessage(), e);
	    }
	}
    private Workflow extractWorkflow(String workflowName) throws AiravataAPIInvocationException {
        Workflow workflowObj = null;
        if(getClient().getWorkflowManager().isWorkflowExists(workflowName)) {
            workflowObj = getClient().getWorkflowManager().getWorkflow(workflowName);
        }else {
            try{
                workflowObj = getClient().getWorkflowManager().getWorkflowFromString(workflowName);
            }catch (AiravataAPIInvocationException e){
            	getClient().getWorkflowManager().getWorkflow(workflowName);
            }

        }
        return workflowObj;
    }
    
	private List<WSComponentPort> getWSComponentPortInputs(Workflow workflow)
			throws GraphException, ComponentException {
		workflow.createScript();
		List<WSComponentPort> inputs = workflow.getInputs();
		return inputs;
	}

	private WSComponentPort getWSComponentPort(String name,
			List<WSComponentPort> ports) {
		for (WSComponentPort port : ports) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}
	
	private void launchWorkflow(String experimentId, String workflowGraph, NameValue[] inputs,
			WorkflowContextHeaderBuilder builder) throws AiravataAPIInvocationException {
		try {
			builder.getWorkflowMonitoringContext().setExperimentId(experimentId);
			WorkflowInterpretorStub stub = new WorkflowInterpretorStub(getClient().getAiravataManager().getWorkflowInterpreterServiceURL().toString());
			stub._getServiceClient().addHeader(
					AXIOMUtil.stringToOM(XMLUtil.xmlElementToString(builder
							.getXml())));
			stub.launchWorkflow(workflowGraph, experimentId, inputs);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void runPreWorkflowExecutionTasks(String experimentId, String user,
			String metadata, String experimentName) throws AiravataAPIInvocationException {
		if (user != null) {
			getClient().getProvenanceManager().setExperimentUser(experimentId, user);
		}
		if (metadata != null) {
			getClient().getProvenanceManager().setExperimentMetadata(experimentId, metadata);
		}
		if (experimentName == null) {
			experimentName = experimentId;
		}
		getClient().getProvenanceManager().setExperimentName(experimentId, experimentName);
	}
	
	@Override
	public ExperimentAdvanceOptions createExperimentAdvanceOptions()
			throws AiravataAPIInvocationException {
		return new ExperimentAdavanceOptionsImpl(getClient());
	}

}
