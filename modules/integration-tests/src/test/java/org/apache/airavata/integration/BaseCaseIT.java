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

import junit.framework.Assert;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.ApplicationManager;
import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.api.exception.DescriptorAlreadyExistsException;
import org.apache.airavata.client.api.exception.WorkflowAlreadyExistsException;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.workflow.ApplicationJob;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.ws.monitor.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Integration test class.
 */
public class BaseCaseIT extends WorkflowIntegrationTestBase {

    public BaseCaseIT() throws Exception {
        setUpEnvironment();
    }

    @BeforeTest
    public void setUp() throws Exception {

        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        this.airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                passwordCallback);
    }

    @Test(groups = {"setupTests"})
    public void testSetup() {

        Version version = this.airavataAPI.getVersion();

        Assert.assertNotNull(version);

        log("Airavata version - " + version.getFullVersion());

    }

    @Test(groups = {"setupTests"}, dependsOnMethods = {"testSetup"})
    public void testURLs() throws AiravataAPIInvocationException {
        URI eventingServiceURL = this.airavataAPI.getAiravataManager().getEventingServiceURL();
        Assert.assertNotNull(eventingServiceURL);

        URI messageBoxServiceURL = this.airavataAPI.getAiravataManager().getMessageBoxServiceURL();
        Assert.assertNotNull(messageBoxServiceURL);
    }

    @Test(groups = {"echoGroup"}, dependsOnGroups = {"setupTests"})
    public void testEchoService() throws Exception {

        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();

        HostDescription hostDescription = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost",
                "127.0.0.1");

        log("Adding host description ....");

        addHostDescriptor(hostDescription);
        ApplicationManager applicationManager = airavataAPI.getApplicationManager();
        Assert.assertTrue(applicationManager.isHostDescriptorExists(
                hostDescription.getType().getHostName()));

        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo input", DataType.STRING));

        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_output", "Echo output", DataType.STRING));

        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("Echo", "Echo service",
                inputParameters, outputParameters);

        log("Adding service description ...");

        addServiceDescriptor(serviceDescription, "Echo");

        Assert.assertTrue(applicationManager.isServiceDescriptorExists(
                serviceDescription.getType().getName()));

        // Deployment descriptor
        ApplicationDescription applicationDeploymentDescription = descriptorBuilder
                .buildApplicationDeploymentDescription("EchoApplication", OsUtils.getEchoExecutable(), OsUtils.getTempFolderPath());

        log("Adding deployment description ...");

        addApplicationDescriptor(applicationDeploymentDescription, serviceDescription, hostDescription, "EchoApplication");
        Assert.assertTrue(applicationManager.isApplicationDescriptorExists(
                serviceDescription.getType().getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));

        log("Saving workflow ...");
        Workflow workflow = new Workflow(getWorkflowComposeContent("src/test/resources/EchoWorkflow.xwf"));

        boolean workflowExists = airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName());
//        Workflow worflowToBeAdded = airavataAPI.getWorkflowManager().getWorkflow(workflow.getName());
        if (!workflowExists) {
            airavataAPI.getWorkflowManager().addWorkflow(workflow);
        }
        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));

        log("Workflow setting up completed ...");

        runWorkFlow(workflow, Arrays.asList("echo_output=Airavata_Test"));
    }

    @Test(groups = {"performanceTesting"})
    public void testExistsHostDescriptor() throws AiravataAPIInvocationException {

        airavataAPI.getApplicationManager().isHostDescriptorExists("localhost");
    }

    @Test(groups = {"echoGroup"}/* , dependsOnMethods = { "testEchoService" } */)
    public void testUpdateEchoService() throws Exception {

        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();

        HostDescription hostDescription = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost",
                "127.0.0.1");

        log("Trying to add host description ....");
        try {
            airavataAPI.getApplicationManager().addHostDescription(hostDescription);
            Assert.fail("Host Descriptor should already exists and should go to update.");
        } catch (DescriptorAlreadyExistsException e) {

            log("Updating host description ....");
            airavataAPI.getApplicationManager().updateHostDescription(hostDescription);
        }

        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(
                hostDescription.getType().getHostName()));

        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo input", DataType.STRING));

        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_output", "Echo output", DataType.STRING));

        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("Echo", "Echo service",
                inputParameters, outputParameters);

        log("Adding service description ...");
        try {
            airavataAPI.getApplicationManager().addServiceDescription(serviceDescription);
            Assert.fail("Service Descriptor should already exists and should go to update.");
        } catch (DescriptorAlreadyExistsException e) {

            log("Updating service description ....");
            airavataAPI.getApplicationManager().updateServiceDescription(serviceDescription);
        }

        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
                serviceDescription.getType().getName()));

        // Deployment descriptor
        ApplicationDescription applicationDeploymentDescription = descriptorBuilder
                .buildApplicationDeploymentDescription("EchoApplication", OsUtils.getEchoExecutable(), OsUtils.getTempFolderPath());

        log("Adding deployment description ...");
        try {
            airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription, hostDescription,
                    applicationDeploymentDescription);
            Assert.fail("Application Descriptor should already exists and should go to update.");
        } catch (DescriptorAlreadyExistsException e) {

            log("Updating application description ....");
            airavataAPI.getApplicationManager().updateApplicationDescription(serviceDescription, hostDescription,
                    applicationDeploymentDescription);
        }

        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(
                serviceDescription.getType().getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));

        log("Saving workflow ...");
        Workflow workflow = new Workflow(getWorkflowComposeContent("src/test/resources/EchoWorkflow.xwf"));

        try {
            airavataAPI.getWorkflowManager().addWorkflow(workflow);
            Assert.fail("Workflow should already exists and should go to update.");
        } catch (WorkflowAlreadyExistsException e) {

            log("Updating workflow...");
            airavataAPI.getWorkflowManager().updateWorkflow(workflow);
        }

        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));

        log("Workflow setting up completed ...");

        try {
            runWorkFlowWithoutMonitor(workflow, Arrays.asList("echo_output=Airavata_Test"));
        } catch (Exception e) {
            log.error("An error occurred while invoking workflow", e);
            throw e;
        }
    }

    protected void runWorkFlow(Workflow workflow, List<String> inputValues) throws Exception {

        String experimentId = executeWorkflow(workflow, inputValues);
        monitor(experimentId);
    }

    protected void runWorkFlowWithoutMonitor(Workflow workflow, List<String> inputValues) throws Exception {

        String experimentId = executeWorkflow(workflow, inputValues);

        verifyOutput(experimentId, "echo_output=Airavata_Test");

        log.info("Verifying application jobs ....");
        List<ApplicationJob> applicationJobs = airavataAPI.getProvenanceManager().getApplicationJobs(experimentId, null, null);
        Assert.assertEquals(applicationJobs.size(), 1);
    }

    protected String getWorkflowComposeContent(String fileName) throws IOException {
        File f = new File(".");
        log.debug(f.getAbsolutePath());

        File echoWorkflow = new File(fileName);
        if (!echoWorkflow.exists()) {
            fileName = "modules/integration-tests/src/test/resources/EchoWorkflow.xwf";
        }

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        log.debug("Workflow compose - " + buffer.toString());
        return buffer.toString();
    }

    public void monitor(final String experimentId) throws Exception {
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(), getUserName(),
                new PasswordCallbackImpl());
        final Monitor experimentMonitor = airavataAPI.getExecutionManager().getExperimentMonitor(experimentId,
                new EventDataListenerAdapter() {

                    public void notify(EventDataRepository eventDataRepo, EventData eventData) {
                        Assert.assertNotNull(eventDataRepo);
                        Assert.assertNotNull(eventData);
                        if (MonitorUtil.EventType.WORKFLOW_TERMINATED.equals(eventData.getType())) {
                            try {
                                BaseCaseIT.this.verifyOutput(experimentId, "echo_output=Airavata_Test");
                            } catch (Exception e) {
                                log.error("Error verifying output", e);
                                Assert.fail("Error occurred while verifying output.");
                            } finally {
                                getMonitor().stopMonitoring();
                            }
                        }
                        log.info("No of events: " + eventDataRepo.getEvents().size());
                    }
                });
        experimentMonitor.startMonitoring();
        experimentMonitor.waitForCompletion();
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

}
