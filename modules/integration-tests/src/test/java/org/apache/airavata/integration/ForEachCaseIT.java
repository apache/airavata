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

package org.apache.airavata.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.api.exception.DescriptorAlreadyExistsException;
import org.apache.airavata.client.api.exception.WorkflowAlreadyExistsException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.InputData;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType.WorkflowNode;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Integration test class.
 */
public class ForEachCaseIT {

    private final Logger log = LoggerFactory.getLogger(ForEachCaseIT.class);

    private int port;
    private String serverUrl;
    private String serverContextName;

    private String registryURL;

    private String gatewayName = "default";
    private String userName = "admin";
    private String password = "admin";

    private static final int TIME_OUT = 20000;

    private static final int TRIES = 3;

    private AiravataAPI airavataAPI;

    protected void log(String message) {
        log.info(message);
    }

    public Logger getLog() {
        return log;
    }

    public int getPort() {
        return port;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getServerContextName() {
        return serverContextName;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getUserName() {
        return userName;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public String getPassword() {
        return password;
    }

    public ForEachCaseIT() throws Exception {
        setUpEnvironment();
    }

    public void setUpEnvironment() throws Exception {

        log("..................Validating server logs .............................");
        // TODO validate logs

		setRegistryURL(createRegistryURL());

        log("Configurations - Registry URL : " + getRegistryURL());

        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                passwordCallback);

        checkServerStartup(airavataAPI);

        log("Server successfully started .............................");
        log("Running tests .............................");
    }

	private String createRegistryURL() {
		log("Reading test server configurations ...");

        String strPort = System.getProperty("test.server.port");

        if (strPort == null) {
            strPort = "8080";
        }

        String strHost = System.getProperty("test.server.url");

        if (strHost == null) {
            strHost = "localhost";
        }

        String strContext = System.getProperty("test.server.context");

        if (strContext == null) {
            strContext = "airavata";
        }

        port = Integer.parseInt(strPort);
        serverUrl = strHost;
        serverContextName = strContext;

        log("Configurations - port : " + port);
        log("Configurations - serverUrl : " + serverUrl);
        log("Configurations - serverContext : " + serverContextName);

        String registryURL = "http://" + serverUrl + ":" + port + "/" + serverContextName + "/services/registry";
		return registryURL;
	}

    protected void checkServerStartup(AiravataAPI airavataAPI) throws Exception {

        int tries = 0;

        while (true) {

            if (tries == TRIES) {
                log("Server not responding. Cannot continue with integration tests ...");
                throw new Exception("Server not responding !");
            }

            log("Checking server is running, try - " + tries);

            URI eventingServiceURL = airavataAPI.getAiravataManager().getEventingServiceURL();

            URI messageBoxServiceURL = airavataAPI.getAiravataManager().getMessageBoxServiceURL();

            URI workflowInterpreterServiceURL = airavataAPI.getAiravataManager().getWorkflowInterpreterServiceURL();

            if (eventingServiceURL == null || messageBoxServiceURL == null
                    || workflowInterpreterServiceURL == null) {

                log.info("Waiting till server initializes ........");
                Thread.sleep(TIME_OUT);
            } else {
                break;
            }

            ++tries;
        }

    }

    @BeforeTest
    public void setUp() throws Exception {

        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        this.airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                passwordCallback);
        setupDescriptors();
    }

    @Test(groups = { "forEachGroup" }, dependsOnGroups = { "echoGroup" })
    public void testEchoService() throws Exception {
		executeExperiment("src/test/resources/ForEachBasicWorkflow.xwf", Arrays.asList("10","20"), Arrays.asList("10 20"));
		executeExperiment("src/test/resources/ForEachBasicWorkflow.xwf", Arrays.asList("10","20,30"), Arrays.asList("10 20","10 30"));
		executeExperiment("src/test/resources/ForEachBasicWorkflow.xwf", Arrays.asList("10,20","30,40"), Arrays.asList("10 30","20 40"));
		
		executeExperiment("src/test/resources/ForEachEchoWorkflow.xwf", Arrays.asList("10","20"), Arrays.asList("10,20"));
		executeExperiment("src/test/resources/ForEachEchoWorkflow.xwf", Arrays.asList("10","20,30"), Arrays.asList("10,20","10,30"));
		executeExperiment("src/test/resources/ForEachEchoWorkflow.xwf", Arrays.asList("10,20","30,40"), Arrays.asList("10,30","20,40"));
    }

	private void executeExperiment(String workflowFilePath,
			List<String> inputs, List<String> outputs) throws GraphException,
			ComponentException, IOException, WorkflowAlreadyExistsException,
			AiravataAPIInvocationException, Exception {
        log("Saving workflow ...");

		Workflow workflow = new Workflow(getWorkflowComposeContent(workflowFilePath));
		if (!airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName())){
			airavataAPI.getWorkflowManager().addWorkflow(workflow);
		}
		Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));

        log("Workflow setting up completed ...");

		runWorkFlow(workflow, inputs,outputs);
	}

	private void setupDescriptors() throws AiravataAPIInvocationException,
			DescriptorAlreadyExistsException, IOException {
		DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();
		HostDescription hostDescription = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost2",
                "127.0.0.1");

        log("Adding host description ....");
        airavataAPI.getApplicationManager().addHostDescription(hostDescription);
        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(hostDescription.getType().getHostName()));
        
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("data1", "data1", DataType.STRING));
        inputParameters.add(descriptorBuilder.buildInputParameterType("data2", "data2", DataType.STRING));

        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("out", "out", DataType.STD_OUT));

        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("comma_app", "comma_app",
                inputParameters, outputParameters);
        
        ServiceDescription serviceDescription2 = descriptorBuilder.buildServiceDescription("echo_app", "echo_app",
                inputParameters, outputParameters);

        log("Adding service description ...");
        airavataAPI.getApplicationManager().addServiceDescription(serviceDescription);
        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
                serviceDescription.getType().getName()));
        
        airavataAPI.getApplicationManager().addServiceDescription(serviceDescription2);
        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
                serviceDescription2.getType().getName()));

        // Deployment descriptor
        File executable = getFile("src/test/resources/comma_data.sh");
        Runtime.getRuntime().exec("chmod +x "+executable.getAbsolutePath());
		ApplicationDescription applicationDeploymentDescription = descriptorBuilder
                .buildApplicationDeploymentDescription("comma_app_localhost", executable.getAbsolutePath(), "/tmp");
		ApplicationDescription applicationDeploymentDescription2 = descriptorBuilder
                .buildApplicationDeploymentDescription("echo_app_localhost", "/bin/echo", "/tmp");

        log("Adding deployment description ...");
        airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription, hostDescription,
                applicationDeploymentDescription);

        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(
                serviceDescription.getType().getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));
        
        airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription2, hostDescription,
                applicationDeploymentDescription2);

        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(
                serviceDescription2.getType().getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription2.getType().getApplicationName().getStringValue()));
	}

    protected void runWorkFlow(Workflow workflow, List<String> inputValues, List<String> outputValue) throws Exception {
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                new PasswordCallbackImpl());
        List<WorkflowInput> workflowInputs = setupInputs(workflow, inputValues);
        String workflowName = workflow.getName();
        ExperimentAdvanceOptions options = airavataAPI.getExecutionManager().createExperimentAdvanceOptions(
                workflowName, getUserName(), null);

        String experimentId = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, options);

        Assert.assertNotNull(experimentId);

        log.info("Run workflow completed ....");

        airavataAPI.getExecutionManager().waitForExperimentTermination(experimentId);
        verifyOutput(experimentId, outputValue);
    }

    protected void verifyOutput(String experimentId, List<String> outputVerifyingString) throws Exception {
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                new PasswordCallbackImpl());
        log.info("Experiment ID Returned : " + experimentId);

        ExperimentData experimentData = airavataAPI.getProvenanceManager().getExperimentData(experimentId);

        log.info("Verifying output ...");

        List<WorkflowExecutionDataImpl> workflowInstanceData = experimentData.getWorkflowExecutionDataList();

        Assert.assertFalse("Workflow instance data cannot be empty !", workflowInstanceData.isEmpty());

        for (WorkflowExecutionDataImpl data : workflowInstanceData) {
            List<NodeExecutionData> nodeDataList = data.getNodeDataList(WorkflowNode.OUTPUTNODE);
            Assert.assertFalse("Node execution data list cannot be empty !", nodeDataList.isEmpty());
            for (NodeExecutionData nodeData : nodeDataList) {
                for (InputData inputData : nodeData.getInputData()) {
                	String[] outputValues = StringUtil.getElementsFromString(inputData.getValue());
                	Assert.assertEquals(outputVerifyingString.size(), outputValues.length);
                	for(int i=0;i<outputValues.length;i++){
                		Assert.assertEquals(outputVerifyingString.get(i), outputValues[i]);	
                	}
                }
            }
        }
    }

    private List<WorkflowInput> setupInputs(Workflow workflow, List<String> inputValues) throws Exception {
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                new PasswordCallbackImpl());
        List<WorkflowInput> workflowInputs = airavataAPI.getWorkflowManager().getWorkflowInputs(workflow.getName());

        Assert.assertEquals(workflowInputs.size(), inputValues.size());

        int i = 0;
        for (String valueString : inputValues) {
            workflowInputs.get(i).setValue(valueString);
            ++i;
        }
        return workflowInputs;
    }

    protected String getWorkflowComposeContent(String fileName) throws IOException {
        File file = getFile(fileName);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        log.debug("Workflow compose - " + buffer.toString());
        return buffer.toString();
    }

	private File getFile(String fileName) {
		File f = new File(".");
        log.debug(f.getAbsolutePath());

        File file = new File(fileName);
        if (!file.exists()) {
        	file = new File("modules/integration-tests/"+fileName);
        }
		return file;
	}

	public void setRegistryURL(String registryURL) {
		this.registryURL = registryURL;
	}

}
