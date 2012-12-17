package org.apache.airavata.registry.api.workflow;/*
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

import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType.WorkflowNode;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.Date;
import java.util.List;

@WebService
@XmlSeeAlso(WorkflowExecutionDataImpl.class)
public interface WorkflowExecutionData {
	/**
	 * Get workflow execution id
	 * @return
	 */
	public String getId();
	
    /**
     * Retrieve all node data in this workflow execution
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public List<NodeExecutionData> getNodeDataList() throws ExperimentLazyLoadedException;
    
    /**
     * Retrieve all node data of the type <code>type</code>
     * @param type
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public List<NodeExecutionData> getNodeDataList(WorkflowNode type)throws ExperimentLazyLoadedException;
    
    /**
     * Retrieve the node data with the given node Id
     * @param nodeId
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public NodeExecutionData getNodeData(String nodeId) throws ExperimentLazyLoadedException;

    /**
     * Add node data to the workflow execution
     * @param nodeData
     * @throws ExperimentLazyLoadedException
     */
    public void addNodeData(NodeExecutionData...nodeData) throws ExperimentLazyLoadedException;
    
    /**
     * Get id of the experiment which this workflow execution belongs to 
     * @return
     */
    public String getExperimentId();
    
    /**
     * Get the workflow template name corresponding to this workflow execution
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public String getTemplateName() throws ExperimentLazyLoadedException;
    
    /**
     * Get current state of the execution of this workflow
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public WorkflowExecutionStatus.State getState() throws ExperimentLazyLoadedException;
    
    /**
     * Get current state updated time
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public Date getStatusUpdateTime() throws ExperimentLazyLoadedException;
    
    /**
     * Retrieve inputs to the workflow execution
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public List<InputData> getWorkflowInputs () throws ExperimentLazyLoadedException;
    
    /**
     * Retrieve outputs to the workflow execution
     * @return
     * @throws ExperimentLazyLoadedException
     */
    public List<OutputData> getWorkflowOutputs ()throws ExperimentLazyLoadedException;

    @Deprecated
    public WorkflowExecution getWorkflowExecution() throws ExperimentLazyLoadedException;
    @Deprecated
    public ExperimentData getExperimentData() throws ExperimentLazyLoadedException;
    @Deprecated
    public void setExperimentData(ExperimentDataImpl experimentData) throws ExperimentLazyLoadedException;
}
