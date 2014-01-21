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
package org.apache.airavata.samples;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.exception.worker.ExperimentLazyLoadedException;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.InvalidDataFormatException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

public class SimpleGateway {
    private final Logger log = LoggerFactory.getLogger(SimpleGateway.class);

    private int port;
    private String serverUrl;
    private String serverContextName;

    private String registryURL;

    private String gatewayName;
    private String userName;
    private String password;

    private static final int TIME_OUT = 20000;

    private static final int TRIES = 3;

    private AiravataAPI airavataAPI;

    private ApplicationManager applicationManager;


    @BeforeTest
    public void setupTests() throws URISyntaxException, AiravataAPIInvocationException {

        //get the registry URLs and the credentials from the gateway.properties file
        setGatewayProperties();

        //get the Airavata API
        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        this.airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                passwordCallback);
        this.applicationManager = airavataAPI.getApplicationManager();

        log("Setting Up Tests: Complete ...");

    }

    @Test
    public void runGatewayTest() throws Exception {
        String experimentId = submitJob();
        monitorJob(experimentId);
        getResults(experimentId);
    }

    private String submitJob() throws Exception, IOException, ComponentException, GraphException, InvalidDataFormatException {
        //setup the descriptors to run the workflow
        setupDescriptors();

        // ---------------load and add the workflow to the server ----------------
        Workflow workflow = new Workflow(getWorkflowContent("src/main/resources/SimpleEcho.xwf"));

        //check if a workflow with the same name exists already, if not add it to the server
        if (!airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName())) {
            airavataAPI.getWorkflowManager().addWorkflow(workflow);
        }

        // add the inputs

        //List<WorkflowInput> workflowInputs = setupInputs(workflow, Arrays.asList("Hello Developer!"));
        List<WorkflowInput> workflowInputs = airavataAPI.getWorkflowManager().getWorkflowInputs(workflow.getName());
        workflowInputs.get(0).setValue("Hello Scientist ---!!");
        log("Workfllow Setting up complete ...");

        String experimentId = airavataAPI.getExecutionManager().runExperiment(workflow.getName(), workflowInputs);
        Assert.assertNotNull(experimentId);

        log("Job submitted ....");
        return experimentId;
    }

    // provides the current details of the job
    private void monitorJob(String experimentId) throws AiravataAPIInvocationException {
        MonitorListener monitorListener = new MonitorListener();
        Monitor monitor = airavataAPI.getExecutionManager().getExperimentMonitor(experimentId, monitorListener);
        log("Started monitoring the job " + experimentId);
        monitor.startMonitoring();
        airavataAPI.getExecutionManager().waitForExperimentTermination(experimentId);

    }

    private void getResults(String experimentId) throws AiravataAPIInvocationException, ExperimentLazyLoadedException, URISyntaxException {

        log("Fetching the results ...");
        //get the experiment data through the provenance manager
        ExperimentData experimentData = airavataAPI.getProvenanceManager().getExperimentData(experimentId);
        List<WorkflowExecutionDataImpl> workflowInstanceData = experimentData.getWorkflowExecutionDataList();
        Assert.assertFalse("Workflow instance data cannot be empty !", workflowInstanceData.isEmpty());

        for (WorkflowExecutionDataImpl data : workflowInstanceData) {
            List<NodeExecutionData> nodeDataList = data.getNodeDataList(WorkflowNodeType.WorkflowNode.OUTPUTNODE);
            for (NodeExecutionData nodeData : nodeDataList) {
                log("Output: " + nodeData.getOutput());
            }
        }
    }


    protected List<WorkflowInput> setupInputs(Workflow workflow, List<String> inputValues) throws Exception {
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

    private String getWorkflowContent(String filename) throws IOException {
        File file = getFile(filename);

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

    private void setupDescriptors() throws AiravataAPIInvocationException {
        // get the descriptor builder to build the necessary descriptors
        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();

        // ------------------Adding the host description ----------------------------------------
        HostDescription hostDescription = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost",
                "127.0.0.1");
        applicationManager.addHostDescription(hostDescription);

        //test that the host description was added properly
        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(
                hostDescription.getType().getHostName()));
        log("Added Application Description ...");


        // ------------------Adding the service description ----------------------------------------
        //Input parameter description
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo_input", DataType.STRING));

        //Output parameter description
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_out", "echo_out", DataType.STRING));

        //add the service descriptor
        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("echo_app", "echo_app",
                inputParameters, outputParameters);
        applicationManager.addServiceDescription(serviceDescription);

        //test that the service description was added properly
        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
                serviceDescription.getType().getName()));

        log("Added Service Description ...");

        //------------------Adding the Application Description ---------------------------------------
        ApplicationDescription applicationDescription = descriptorBuilder.
                buildApplicationDeploymentDescription("echo_app", "/bin/echo", "/tmp");
        applicationManager.addApplicationDescription(serviceDescription, hostDescription, applicationDescription);
        log("Added Application Deployment Description ...");

    }

    private File getFile(String fileName) {
        File f = new File(".");
        log.debug(f.getAbsolutePath());

        File file = new File(fileName);
        if (!file.exists()) {
            file = new File("samples/gateway-developer-guide/" + fileName);
        }
        return file;
    }

    private void log(String message) {
        log.info(message);
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
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

    public void setPort(int port) {
        this.port = port;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public void setServerContextName(String serverContextName) {
        this.serverContextName = serverContextName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    //get the registry URL and the credentials from the property file
    private void setGatewayProperties() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("gateway.properties");

        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String username = properties.getProperty("airavata.server.user");
        String password = properties.getProperty("airavata.server.password");
        String gatewayName = properties.getProperty("gateway.name");
        String registryURL = properties.getProperty("airavata.server.url");
        setRegistryURL(registryURL);
        setUserName(username);
        setPassword(password);
        setGatewayName(gatewayName);
        log("loaded the properties ...");
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
