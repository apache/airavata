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

package org.apache.airavata.sample.gateway.executor;

import junit.framework.Assert;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.client.api.exception.DescriptorAlreadyExistsException;
import org.apache.airavata.client.api.exception.WorkflowAlreadyExistsException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.sample.gateway.ExecutionParameters;
import org.apache.airavata.schemas.gfac.*;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.EventData;
import org.apache.airavata.ws.monitor.EventDataListenerAdapter;
import org.apache.airavata.ws.monitor.EventDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/26/13
 * Time: 9:21 AM
 */

public class WorkflowExecutor {

    private final Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);

    private String airavataServerUrl;
    private String airavataServerUser;

    private PasswordCallbackImpl passwordCallback;

    private String gatewayName;

    public WorkflowExecutor(String gwName) throws IOException {
        loadConfigurations();
        this.gatewayName = gwName;
    }

    private void loadConfigurations () throws IOException {

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("gateway.properties");

        Properties properties = new Properties();
        properties.load(inputStream);

        this.airavataServerUrl = properties.getProperty("airavata.server.url");
        this.airavataServerUser = properties.getProperty("airavata.server.user");

        this.passwordCallback = new PasswordCallbackImpl(properties);

        log.info("Airavata server url - " + this.airavataServerUrl);
        log.info("Airavata server user - " + this.airavataServerUser);
        log.info("Workflow executor successfully initialized");

    }


    public String getAiravataServerUrl() {
        return airavataServerUrl;
    }

    public String getAiravataServerUser() {
        return airavataServerUser;
    }

    public PasswordCallbackImpl getPasswordCallback() {
        return passwordCallback;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public Workflow setupExperiment(ExecutionParameters executionParameters) throws Exception {

        // Create airavata API
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getAiravataServerUrl()), getGatewayName(),
                getAiravataServerUser(),
                getPasswordCallback());

        // Initialize descriptor builder
        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();

        // Create host description
        HostDescription hostDescription = descriptorBuilder.buildHostDescription(GlobusHostType.type,
                executionParameters.getHostName(), executionParameters.getHostAddress());

        ((GlobusHostType)hostDescription.getType()).
                setGlobusGateKeeperEndPointArray(new String[]{executionParameters.getGateKeeperAddress()});
        ((GlobusHostType)hostDescription.getType()).
                setGridFTPEndPointArray(new String[]{executionParameters.getGridftpAddress()}); //TODO do we really need this ?

        log("Adding host description ....");

        try {
            airavataAPI.getApplicationManager().addHostDescription(hostDescription);
        } catch (DescriptorAlreadyExistsException e) {
            airavataAPI.getApplicationManager().updateHostDescription(hostDescription);
        }


        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo input", DataType.STRING));

        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_output", "Echo output", DataType.STRING));

        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("Echo", "Echo service",
                inputParameters, outputParameters);

        log("Adding service description ...");

        try {
            airavataAPI.getApplicationManager().addServiceDescription(serviceDescription);
        } catch (DescriptorAlreadyExistsException e) {
            airavataAPI.getApplicationManager().updateServiceDescription(serviceDescription);
        }


        // =========================================================================================================== //
        // Deployment descriptor creation
        ApplicationDescription applicationDeploymentDescription =
                new ApplicationDescription(HpcApplicationDeploymentType.type);
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType
                = applicationDeploymentDescription.getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue(executionParameters.getApplicationName());

        applicationDeploymentDescriptionType.setExecutableLocation(executionParameters.getExecutableLocation());
        applicationDeploymentDescriptionType.setScratchWorkingDirectory(executionParameters.getWorkingDirectory());

        ProjectAccountType projectAccountType = ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).addNewProjectAccount();
        projectAccountType.setProjectAccountNumber(executionParameters.getProjectNumber());

        log("Adding application deployment description ...");

        QueueType queueType = ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).addNewQueue();
        queueType.setQueueName(executionParameters.getQueueName());

        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setJobType(executionParameters.getJobType());
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setMaxWallTime(executionParameters.getMaxWallTime());
        //((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setMaxMemory(executionParameters.getMaxMemory());
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setCpuCount(executionParameters.getCpuCount());
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setNodeCount(executionParameters.getMaxNodeCount());
        ((HpcApplicationDeploymentType) applicationDeploymentDescriptionType).setProcessorsPerNode(executionParameters.getMaxProcessorsPerNode());

        try {
            airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription, hostDescription,
                    applicationDeploymentDescription);
        } catch (DescriptorAlreadyExistsException e) {
            airavataAPI.getApplicationManager().updateApplicationDescription(serviceDescription, hostDescription,
                    applicationDeploymentDescription);
        }





        // =========================================================================================================== //


        log("Saving workflow ...");
        Workflow workflow = new Workflow(getWorkflowComposeContent());

        try {
            airavataAPI.getWorkflowManager().addWorkflow(workflow);
        } catch (WorkflowAlreadyExistsException e) {
            airavataAPI.getWorkflowManager().updateWorkflow(workflow);
        }

        log("Workflow setting up completed ...");

        return workflow;

    }

    protected String getWorkflowComposeContent() throws IOException {

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("EchoWorkflow.xwf");

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        log.debug("Workflow compose - " + buffer.toString());
        return buffer.toString();
    }

    public String runWorkflow (Workflow workflow, List<String> inputValues, String tokenId, String portalUser) throws Exception {

        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getAiravataServerUrl()), getGatewayName(), getAiravataServerUser(),
                this.getPasswordCallback());

        // Set inputs
        List<WorkflowInput> workflowInputs = airavataAPI.getWorkflowManager().getWorkflowInputs(workflow.getName());

        int i = 0;
        for (String valueString : inputValues) {
            workflowInputs.get(i).setValue(valueString);
            ++i;
        }

        String workflowName = workflow.getName();
        ExperimentAdvanceOptions options = airavataAPI.getExecutionManager().createExperimentAdvanceOptions(
                workflowName, getAiravataServerUser(), null);


        //========================= Setting token id =========================//

        if (tokenId != null) {
            log("Setting token id to " + tokenId);
            options.getCustomSecuritySettings().getCredentialStoreSecuritySettings().setTokenId(tokenId);
            options.getCustomSecuritySettings().getCredentialStoreSecuritySettings().setPortalUser(portalUser);
            options.getCustomSecuritySettings().getCredentialStoreSecuritySettings().setGatewayId(getGatewayName());
        } else {
            log("Token id is not set ....");
        }

        //====================================================================//


        String experimentId = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, options,
                new EventDataListenerAdapter() {
                    public void notify(EventDataRepository eventDataRepo, EventData eventData) {
                        // do nothing
                    }
                });

        airavataAPI.getExecutionManager().waitForExperimentTermination(experimentId);

        log.info("Run workflow completed ....");

        return getWorkflowOutput(experimentId, airavataAPI);

    }

    public String runWorkflow (Workflow workflow, List<String> inputValues) throws Exception {

        return runWorkflow(workflow, inputValues, null, null);

    }

    protected String getWorkflowOutput(String experimentId, AiravataAPI airavataAPI) throws Exception {

        log.info("Experiment ID Returned : " + experimentId);

        ExperimentData experimentData = airavataAPI.getProvenanceManager().getExperimentData(experimentId);

        log.info("Verifying output ...");

        List<NodeExecutionData> nodeExecutionDataList = experimentData.getNodeDataList(WorkflowNodeType.WorkflowNode.OUTPUTNODE);

        for (NodeExecutionData nodeExecutionData : nodeExecutionDataList) {
            return nodeExecutionData.getOutputData().get(0).getValue();
        }

        throw new Exception("Experiment did not generate any output");
    }

    protected void log(String message) {
        log.info(message);
    }




}
