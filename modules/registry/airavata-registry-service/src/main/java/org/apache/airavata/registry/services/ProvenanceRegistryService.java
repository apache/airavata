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

package org.apache.airavata.registry.services;

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.NodeExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeGramData;

import javax.ws.rs.core.Response;
import java.util.Date;

public interface ProvenanceRegistryService {
    /*------------------------------------------- Experiment data ---------------------------------------------*/

    /**
     * Returns true if the experiment exists
     * 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response isExperimentExists(String experimentId) throws RegistryException;

    public Response isExperimentExistsThenCreate(String experimentId, boolean createIfNotPresent)
            throws RegistryException;

    /**
     * Save the username of the user who runs this experiment
     * 
     * @param experimentId
     * @param user
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateExperimentExecutionUser(String experimentId, String user) throws RegistryException;

    /**
     * Retrieve the user who is runing the experiment
     * 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperimentExecutionUser(String experimentId) throws RegistryException;

    /**
     * Get the name of the workflow intance
     * 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperimentName(String experimentId) throws RegistryException;

    /**
     * Save a name for this workflow execution
     * 
     * @param experimentId
     * @param experimentName
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateExperimentName(String experimentId, String experimentName) throws RegistryException;

    /**
     * Return the metadata information saved for the experiment
     * 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperimentMetadata(String experimentId) throws RegistryException;

    /**
     * Save the metadata for the experiment
     * 
     * @param experimentId
     * @param metadata
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateExperimentMetadata(String experimentId, String metadata) throws RegistryException;

    /**
     * Return the template name of the workflow that this intance was created from
     * 
     * @param workflowInstanceId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getWorkflowExecutionTemplateName(String workflowInstanceId) throws RegistryException;

    /**
     * Save the template name of the workflow that this intance was created from
     * 
     * @param workflowInstanceId
     * @param templateName
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName)
            throws RegistryException;

    public Response getExperimentWorkflowInstances(String experimentId) throws RegistryException;

    /*-------------------------------------- Experiment Workflow instance node data ----------------------------------------*/

    public Response isWorkflowInstanceExists(String instanceId) throws RegistryException;

    public Response isWorkflowInstanceExistsThenCreate(String instanceId, boolean createIfNotPresent)
            throws RegistryException;

    /**
     * Save a status for this workflow execution with the current time at the moment
     * 
     * @param instanceId
     * @param executionStatus
     *            - contains the status
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateWorkflowInstanceStatusByInstance(String instanceId, String executionStatus)
            throws RegistryException;

    /**
     * Save a status for this workflow execution
     * 
     * @param experimentID
     * @param workflowInstanceID
     * @param executionStatus
     * @param statusUpdateTime
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateWorkflowInstanceStatusByExperiment(String experimentID, String workflowInstanceID,
            String executionStatus, Date statusUpdateTime) throws RegistryException;

    /**
     * Return the status of the execution
     * 
     * @param instanceId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getWorkflowInstanceStatus(String instanceId) throws RegistryException;

    /**
     * Save the input data of a node in the workflow instance of an experiment
     * 
     * @param experimentID
     * @param nodeID
     * @param workflowInstanceID
     * @param data
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateWorkflowNodeInput(String experimentID, String nodeID, String workflowInstanceID, String data)
            throws RegistryException;

    /**
     * Save the output data of a node in the workflow instance of an experiment
     * 
     * @param experimentID
     * @param nodeID
     * @param workflowInstanceID
     * @param data
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateWorkflowNodeOutput(String experimentID, String nodeID, String workflowInstanceID, String data)
            throws RegistryException;

    /**
     * Return a list of data passed as input for service node which regex matched nodeId, workflow template id &
     * experiment id
     * 
     * @param experimentIdRegEx
     * @param workflowNameRegEx
     *            - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx
     *            - nodeId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response searchWorkflowInstanceNodeInput(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx) throws RegistryException;

    /**
     * Return a list of data returned as output from service node which regex matched nodeId, workflow template id &
     * experiment id
     * 
     * @param experimentIdRegEx
     * @param workflowNameRegEx
     *            - this is the workflowName or workflow template Id of an experiment
     * @param nodeNameRegEx
     *            - nodeId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response searchWorkflowInstanceNodeOutput(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx) throws RegistryException;

    public Response getWorkflowInstanceNodeInput(String workflowInstanceId, String nodeType) throws RegistryException;

    public Response getWorkflowInstanceNodeOutput(String workflowInstanceId, String nodeType) throws RegistryException;

    /*---------------------------------------  Retrieving Experiment ------------------------------------------*/
    /**
     * Return workflow execution object fully populated with data currently avaialble for that experiment
     * 
     * @param experimentId
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperiment(String experimentId) throws RegistryException;

    /**
     * Return experiment ids of experiments launched by the given user
     * 
     * @param user
     *            - a regex user id
     * @return - experiment id list
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperimentIdByUser(String user) throws RegistryException;

    /**
     * Return experiments launched by the given user
     * 
     * @param user
     * @return experiment object list each populated by current data of that experiment
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperimentByUser(String user) throws RegistryException;

    /**
     * Return the pageNo set of experiments launched by the given user if grouped in to pages of size pageSize
     * 
     * @param user
     * @param pageSize
     * @param pageNo
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response getExperimentByUser(String user, int pageSize, int pageNo) throws RegistryException;

    /**
     * This will update the workflowStatus for given experimentID,workflowInstanceID combination.
     * 
     * @param workflowStatusNode
     * @return
     * @throws org.apache.airavata.registry.api.exception.RegistryException
     */
    public Response updateWorkflowNodeStatus(NodeExecutionStatus workflowStatusNode) throws RegistryException;

    public Response updateWorkflowNodeStatus(String workflowInstanceId, String nodeId, String executionStatus)
            throws RegistryException;

    public Response updateWorkflowNodeStatus(String workflowInstanceId, String executionStatus)
            throws RegistryException;

    public Response getWorkflowNodeStatus(String workflowInstanceId, String nodeId) throws RegistryException;

    public Response getWorkflowNodeStartTime(String workflowInstanceId, String nodeId) throws RegistryException;

    public Response getWorkflowStartTime(String workflowInstanceId) throws RegistryException;

    /**
     * This will store the gram specific data in to repository, this can be called before submitting the workflow in to
     * Grid
     * 
     * @param workflowNodeGramData
     * @return
     */
    public Response updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData) throws RegistryException;

    public Response getWorkflowInstanceData(String workflowInstanceId) throws RegistryException;

    public Response isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId) throws RegistryException;

    public Response isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId, boolean createIfNotPresent)
            throws RegistryException;

    public Response getWorkflowInstanceNodeData(String workflowInstanceId, String nodeId) throws RegistryException;

    public Response addWorkflowInstance(String experimentId, String workflowInstanceId, String templateName)
            throws RegistryException;

    public Response updateWorkflowNodeType(String workflowInstanceId, String nodeId, String nodeType)
            throws RegistryException;

    public Response addWorkflowInstanceNode(String workflowInstance, String nodeId) throws RegistryException;
}
