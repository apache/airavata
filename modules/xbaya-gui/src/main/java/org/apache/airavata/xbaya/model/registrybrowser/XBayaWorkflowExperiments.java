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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;
import org.apache.airavata.schemas.gfac.Parameter;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.xml.sax.SAXException;

public class XBayaWorkflowExperiments {
	private AiravataRegistry registry;
	
	public XBayaWorkflowExperiments(AiravataRegistry registry) {
		setRegistry(registry);
	}
	
	public List<XBayaWorkflowExperiment> getAllExperiments(){
		Map<String, XBayaWorkflowExperiment> experiments=new HashMap<String,XBayaWorkflowExperiment>();
    	try {
			List<WorkflowServiceIOData> workflowInput = getRegistry().searchWorkflowExecutionServiceInput(null, null, null);
			List<WorkflowServiceIOData> workflowOutput = getRegistry().searchWorkflowExecutionServiceOutput(null, null, null);
			createChildren(experiments, workflowInput, true);
			createChildren(experiments, workflowOutput, false);
		} catch (RegistryException e) {
			e.printStackTrace();
		}
    	return Arrays.asList(experiments.values().toArray(new XBayaWorkflowExperiment[]{}));
	}
	private void createChildren(
			Map<String, XBayaWorkflowExperiment> experiments,
			List<WorkflowServiceIOData> workflowIO, boolean inputData) {
		for (WorkflowServiceIOData workflowIOData : workflowIO) {
			if (!experiments.containsKey(workflowIOData.getExperimentId())){
				experiments.put(workflowIOData.getExperimentId(),new XBayaWorkflowExperiment(workflowIOData.getExperimentId(), null));
			}
			XBayaWorkflowExperiment xBayaWorkflowExperiment = experiments.get(workflowIOData.getExperimentId());
			XBayaWorkflow xbayaWorkflow=null;
			for(XBayaWorkflow workflow:xBayaWorkflowExperiment.getWorkflows()){
				if (workflow.getWorkflowId().equals(workflowIOData.getWorkflowId())){
					xbayaWorkflow=workflow;
					break;
				}
			}
			if (xbayaWorkflow==null){
				xbayaWorkflow=new XBayaWorkflow(workflowIOData.getWorkflowId(),workflowIOData.getWorkflowName(),null);
				xBayaWorkflowExperiment.add(xbayaWorkflow);
			}
			
			XBayaWorkflowService workflowService=null;
			for(XBayaWorkflowService service:xbayaWorkflow.getWorkflowServices()){
				if (service.getServiceNodeId().equals(workflowIOData.getNodeId())){
					workflowService=service;
					break;
				}
			}
			
			if (workflowService==null){
				workflowService=new XBayaWorkflowService(workflowIOData.getNodeId(),null,null);
				xbayaWorkflow.add(workflowService);
			}
			try {
				List<NameValue> parameterData = XBayaUtil.getIOParameterData(workflowIOData.getValue());
				for (NameValue pair : parameterData) {
					Parameter parameter = Parameter.Factory.newInstance();
					parameter.setParameterName(pair.getName());
					ServiceParameter serviceParameter = new ServiceParameter(parameter, pair.getValue());
					if (inputData) {
						workflowService.getInputParameters().getParameters()
								.add(serviceParameter);
					}else{
						workflowService.getOutputParameters().getParameters()
						.add(serviceParameter);
					}
				}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//TODO setup parameters
		}
	}
		
	public AiravataRegistry getRegistry() {
		return registry;
	}
	public void setRegistry(AiravataRegistry registry) {
		this.registry = registry;
	}

}
