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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.api.exception.WorkflowAlreadyExistsException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.InputData;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType.WorkflowNode;
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
public class WorkflowSamplesCaseIT {

    private final Logger log = LoggerFactory.getLogger(WorkflowSamplesCaseIT.class);

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

    public WorkflowSamplesCaseIT() throws Exception {
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
    }

    @Test(groups = { "workflowSamplesGroup" }/*, dependsOnGroups = { "forEachGroup" }*/)
    public void testWorkflowSamples() throws Exception {
        log("Running tests .............................");
        executeExperiment("target/samples/workflows/SimpleEcho.xwf", Arrays.asList("Test_Value"), "Test_Value");
        executeExperiment("target/samples/workflows/LevenshteinDistance.xwf", Arrays.asList("abc","def"), Arrays.asList("3"));
        executeExperiment("target/samples/workflows/SimpleForEach.xwf", Arrays.asList("1,2","3,4"), Arrays.asList("4","6"));
        executeExperiment("target/samples/workflows/ComplexMath.xwf", Arrays.asList("15","16","18","21","25","30","36","43"), "5554");
//		executeExperiment("target/samples/workflows/SimpleMath.xwf", Arrays.asList("15","16","18","21","25","30","36","43"), "204");
//		executeExperiment("target/samples/workflows/ComplexForEach.xwf", Arrays.asList("1,2","3,4","5,6","7,8","9,10","11,12","13,14","15,16"), Arrays.asList("2027025","10321920"));
    }

	private void executeExperiment(String workflowFilePath,
			List<String> inputs, Object outputs) throws GraphException,
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

    protected void runWorkFlow(Workflow workflow, List<String> inputValues, Object outputValue) throws Exception {
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

    protected void verifyOutput(String experimentId, Object outputVerifyingString) throws Exception {
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
                	if (outputVerifyingString instanceof List){
                		@SuppressWarnings("unchecked")
						List<String> outputs=(List<String>)outputVerifyingString;
                		String[] outputValues = StringUtil.getElementsFromString(inputData.getValue());
                    	Assert.assertEquals(outputs.size(), outputValues.length);
                    	for(int i=0;i<outputValues.length;i++){
                    		Assert.assertEquals(outputs.get(i), outputValues[i]);	
                    	}	
                	}else{
                		Assert.assertEquals(outputVerifyingString.toString(), inputData.getValue());
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

    protected String getWorkflowComposeContent(String fileName) throws IOException, URISyntaxException {
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

	private File getFile(String fileName) throws URISyntaxException {
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
