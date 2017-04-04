/**
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
 */
package org.apache.airavata.integration;

import junit.framework.Assert;
import org.apache.airavata.integration.tools.DocumentCreatorNew;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.client.tools.DocumentCreatorNew;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.ws.monitor.EventData;
//import org.apache.airavata.ws.monitor.EventDataListenerAdapter;
//import org.apache.airavata.ws.monitor.EventDataRepository;
//import org.apache.airavata.ws.monitor.Monitor;
//import org.apache.airavata.ws.monitor.MonitorUtil;

/**
 * Integration test class.
 */
public class BaseCaseIT extends WorkflowIntegrationTestBase {

    private AuthzToken authzToken;
    public BaseCaseIT() throws Exception {
        setUpEnvironment();
    }

    @BeforeTest
    public void setUp() throws Exception {
        this.client = getClient();
    }

    @Test(groups = {"setupTests"})
    public void testSetup() throws Exception {
        String version = this.client.getAPIVersion(null);
        Assert.assertNotNull(version);
        log("Airavata version - " + version);


    }

//    @Test(groups = {"setupTests"}, dependsOnMethods = {"testSetup"})
//    public void testURLs() throws AiravataAPIInvocationException {
//        URI eventingServiceURL = this.airavataAPI.getAiravataManager().getEventingServiceURL();
//        Assert.assertNotNull(eventingServiceURL);
//
//        URI messageBoxServiceURL = this.airavataAPI.getAiravataManager().getMessageBoxServiceURL();
//        Assert.assertNotNull(messageBoxServiceURL);
//    }

    @Test(groups = {"echoGroup"}, dependsOnGroups = {"setupTests"})
    public void testEchoService() throws Exception {
        log.info("Running job in trestles...");
        DocumentCreatorNew documentCreator = new DocumentCreatorNew(client);
        documentCreator.createPBSDocsForOGCE_Echo();
        List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
        InputDataObjectType input = new InputDataObjectType();
        input.setName("echo_input");
        input.setType(DataType.STRING);
        input.setValue("echo_output=Hello World");
        exInputs.add(input);

        List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
        OutputDataObjectType output = new OutputDataObjectType();
        output.setName("echo_output");
        output.setType(DataType.STRING);
        output.setValue("");
        exOut.add(output);

        Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
        String projectId = getClient().createProject(authzToken, "default", project);

        ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(gatewayName, projectId, "admin", "echoExperiment", "SimpleEcho2", "SimpleEcho2", exInputs);
        simpleExperiment.setExperimentOutputs(exOut);

        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.edu", 1, 1, 1, "normal", 0, 0);
        scheduling.setResourceHostId("gsissh-trestles");
        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
        userConfigurationData.setAiravataAutoSchedule(false);
        userConfigurationData.setOverrideManualScheduledParams(false);
        userConfigurationData.setComputationalResourceScheduling(scheduling);
        simpleExperiment.setUserConfigurationData(userConfigurationData);
        final String expId = createExperiment(simpleExperiment);
        System.out.println("Experiment Id returned : " + expId);
        log.info("Experiment Id returned : " + expId );
        launchExperiment(expId);
        System.out.println("Launched successfully");

        Thread monitor = (new Thread(){
            public void run() {
                Map<String, JobStatus> jobStatuses = null;
                while (true) {
                    try {
                        jobStatuses = client.getJobStatuses(authzToken, expId);
                        Set<String> strings = jobStatuses.keySet();
                        for (String key : strings) {
                            JobStatus jobStatus = jobStatuses.get(key);
                            if(jobStatus == null){
                                return;
                            }else {
                                if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
                                    log.info("Job completed Job ID: " + key);
                                    return;
                                }else{
                                    log.info("Job ID:" + key + "  Job Status : " + jobStatuses.get(key).getJobState().toString());
                                }
                            }
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.error("Thread interrupted", e.getMessage());
                    }
                }
            }
        });
            monitor.start();
        try {
            monitor.join();
        } catch (InterruptedException e) {
            log.error("Thread interrupted..", e.getMessage());
        }

    }

    @Test(groups = {"echoGroup"}, dependsOnGroups = {"setupTests"})
    public void testEchoServiceStampede() throws Exception {
        log.info("Running job in Stampede...");
        DocumentCreatorNew documentCreator = new DocumentCreatorNew(client);
        documentCreator.createSlurmDocs();
        List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
        InputDataObjectType input = new InputDataObjectType();
        input.setName("echo_input");
        input.setType(DataType.STRING);
        input.setValue("echo_output=Hello World");
        exInputs.add(input);

        List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
        OutputDataObjectType output = new OutputDataObjectType();
        output.setName("echo_output");
        output.setType(DataType.STRING);
        output.setValue("");
        exOut.add(output);

        Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
        String projectId = getClient().createProject(authzToken, "default", project);

        ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(gatewayName, projectId, "admin", "echoExperiment", "SimpleEcho3", "SimpleEcho3", exInputs);
        simpleExperiment.setExperimentOutputs(exOut);

        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling("stampede.tacc.xsede.org", 1, 1, 1, "normal", 0, 0);
        scheduling.setResourceHostId("stampede-host");
        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
        userConfigurationData.setAiravataAutoSchedule(false);
        userConfigurationData.setOverrideManualScheduledParams(false);
        userConfigurationData.setComputationalResourceScheduling(scheduling);
        simpleExperiment.setUserConfigurationData(userConfigurationData);
        final String expId = createExperiment(simpleExperiment);
        System.out.println("Experiment Id returned : " + expId);
        log.info("Experiment Id returned : " + expId );
        launchExperiment(expId);
        System.out.println("Launched successfully");

        Thread monitor = (new Thread(){
            public void run() {
                Map<String, JobStatus> jobStatuses = null;
                while (true) {
                    try {
                        jobStatuses = client.getJobStatuses(authzToken, expId);
                        Set<String> strings = jobStatuses.keySet();
                        for (String key : strings) {
                            JobStatus jobStatus = jobStatuses.get(key);
                            if(jobStatus == null){
                                return;
                            }else {
                                if (JobState.COMPLETE.equals(jobStatus.getJobState())) {
                                    log.info("Job completed Job ID: " + key);
                                    return;
                                }else{
                                    log.info("Job ID:" + key + "  Job Status : " + jobStatuses.get(key).getJobState().toString());
                                }
                            }
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        log.error("Thread interrupted", e.getMessage());
                    }
                }
            }
        });
        monitor.start();
        try {
            monitor.join();
        } catch (InterruptedException e) {
            log.error("Thread interrupted..", e.getMessage());
        }

    }

//    @Test(groups = {"performanceTesting"})
//    public void testExistsHostDescriptor() throws AiravataAPIInvocationException {
//        airavataAPI.getApplicationManager().isHostDescriptorExists("trestles.sdsc.edu");
//    }

//    @Test(groups = {"echoGroup"}/* , dependsOnMethods = { "testEchoService" } */)
//    public void testUpdateEchoService() throws Exception {
//
//        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();
//
//        HostDescription hostDescription = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost",
//                "127.0.0.1");
//
//        log("Trying to add host description ....");
//        try {
//            airavataAPI.getApplicationManager().addHostDescription(hostDescription);
//            Assert.fail("Host Descriptor should already exists and should go to update.");
//        } catch (DescriptorAlreadyExistsException e) {
//
//            log("Updating host description ....");
//            airavataAPI.getApplicationManager().updateHostDescription(hostDescription);
//        }
//
//        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(
//                hostDescription.getType().getHostName()));
//
//        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
//        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo input", DataType.STRING));
//
//        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
//        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_output", "Echo output", DataType.STRING));
//
//        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("Echo", "Echo service",
//                inputParameters, outputParameters);
//
//        log("Adding service description ...");
//        try {
//            airavataAPI.getApplicationManager().addServiceDescription(serviceDescription);
//            Assert.fail("Service Descriptor should already exists and should go to update.");
//        } catch (DescriptorAlreadyExistsException e) {
//
//            log("Updating service description ....");
//            airavataAPI.getApplicationManager().updateServiceDescription(serviceDescription);
//        }
//
//        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(
//                serviceDescription.getType().getName()));
//
//        // Deployment descriptor
//        ApplicationDescription applicationDeploymentDescription = descriptorBuilder
//                .buildApplicationDeploymentDescription("EchoApplication", OsUtils.getEchoExecutable(), OsUtils.getTempFolderPath());
//
//        log("Adding deployment description ...");
//        try {
//            airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription, hostDescription,
//                    applicationDeploymentDescription);
//            Assert.fail("Application Descriptor should already exists and should go to update.");
//        } catch (DescriptorAlreadyExistsException e) {
//
//            log("Updating application description ....");
//            airavataAPI.getApplicationManager().updateApplicationDescription(serviceDescription, hostDescription,
//                    applicationDeploymentDescription);
//        }
//
//        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(
//                serviceDescription.getType().getName(), hostDescription.getType().getHostName(),
//                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));
//
//        log("Saving workflow ...");
//        Workflow workflow = new Workflow(getWorkflowComposeContent("src/test/resources/EchoWorkflow.xwf"));
//
//        try {
//            airavataAPI.getWorkflowManager().addWorkflow(workflow);
//            Assert.fail("Workflow should already exists and should go to update.");
//        } catch (WorkflowAlreadyExistsException e) {
//
//            log("Updating workflow...");
//            airavataAPI.getWorkflowManager().updateWorkflow(workflow);
//        }
//
//        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));
//
//        log("Workflow setting up completed ...");
//
//        try {
//            /**
//             * FIXME : Saving to GFAC_JOB_DATA is commented out due to new orchestrator changes. Due to that, this test will fail. Once it is fixed we need to uncomment this too.
//             **/
////            runWorkFlowWithoutMonitor(workflow, Arrays.asList("echo_output=Airavata_Test"));
//        } catch (Exception e) {
//            log.error("An error occurred while invoking workflow", e);
//            throw e;
//        }
//    }

//    protected void runWorkFlow(Workflow workflow, List<String> inputValues) throws Exception {
//
//        String experimentId = executeWorkflow(workflow, inputValues);
//        monitor(experimentId);
//    }

//    protected void runWorkFlowWithoutMonitor(Workflow workflow, List<String> inputValues) throws Exception {
//
//        String experimentId = executeWorkflow(workflow, inputValues);
//
//        verifyOutput(experimentId, "echo_output=Airavata_Test");
//
//        log.info("Verifying application jobs ....");
//
//        List<ApplicationJob> applicationJobs = airavataAPI.getProvenanceManager().getApplicationJobs(experimentId, null, null);
//        Assert.assertEquals(applicationJobs.size(), 1);
//    }

//    protected String getWorkflowComposeContent(String fileName) throws IOException {
//        File f = new File(".");
//        log.debug(f.getAbsolutePath());
//
//        File echoWorkflow = new File(fileName);
//        if (!echoWorkflow.exists()) {
//            fileName = "modules/integration-tests/src/test/resources/EchoWorkflow.xwf";
//        }
//
//        BufferedReader reader = new BufferedReader(new FileReader(fileName));
//        String line;
//        StringBuilder buffer = new StringBuilder();
//        while ((line = reader.readLine()) != null) {
//            buffer.append(line);
//        }
//        reader.close();
//        log.debug("Workflow compose - " + buffer.toString());
//        return buffer.toString();
//    }

//    public void monitor(final String experimentId) throws Exception {
//        final Monitor experimentMonitor = airavataAPI.getExecutionManager().getExperimentMonitor(experimentId,
//                new EventDataListenerAdapter() {
//
//                    public void notify(EventDataRepository eventDataRepo, EventData eventData) {
//                        Assert.assertNotNull(eventDataRepo);
//                        Assert.assertNotNull(eventData);
//                        if (MonitorUtil.EventType.WORKFLOW_TERMINATED.equals(eventData.getType())) {
//                            try {
//                                BaseCaseIT.this.verifyOutput(experimentId, "echo_output=Airavata_Test");
//                            } catch (Exception e) {
//                                log.error("Error verifying output", e);
//                                Assert.fail("Error occurred while verifying output.");
//                            } finally {
//                                getMonitor().stopMonitoring();
//                            }
//                        }
//                        log.info("No of events: " + eventDataRepo.getEvents().size());
//                    }
//                });
//        experimentMonitor.startMonitoring();
//        experimentMonitor.waitForCompletion();
//    }
}
