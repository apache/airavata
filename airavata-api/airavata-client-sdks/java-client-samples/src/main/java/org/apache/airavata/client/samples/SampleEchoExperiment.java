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
package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.tools.RegisterSampleApplicationsUtils;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.model.workspace.Project;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class SampleEchoExperiment {

    private static final String THRIFT_SERVER_HOST = "127.0.0.1";
    private static final int THRIFT_SERVER_PORT = 8930;

    private Airavata.Client airavataClient;
    private String localhostId ;
    private String echoModuleId;
    private String echoInterfaceId;
    private String echoExperimentId;

    private String gatewayId = "default";
    private String userId = "default-user";

    public static void main(String[] args) throws AiravataClientException, TException {
        SampleEchoExperiment sampleEchoExperiment = new SampleEchoExperiment();
        sampleEchoExperiment.register();
    }

    public void register() throws TException {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        gatewayId = registerGateway();
        registerLocalhost();
        registerGatewayProfile();
        registerEchoModule();
        registerEchoDeployment();
        registerEchoInterface();
        createEchoExperiment();
    }

    private String registerGateway() throws TException {
        Gateway gateway = new Gateway();
        gateway.setGatewayName(gatewayId);
        gateway.setGatewayId(gatewayId);
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
        return airavataClient.addGateway(new AuthzToken(""), gateway);
    }


    private void registerLocalhost() {
        try {
            System.out.println("\n #### Registering Localhost Computational Resource #### \n");

            ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils.
                    createComputeResourceDescription("localhost", "LocalHost", null, null);
            DataMovementInterface dataMovementInterface = new DataMovementInterface("localhost_data_movement_interface", DataMovementProtocol.LOCAL, 1);
            computeResourceDescription.addToDataMovementInterfaces(dataMovementInterface);
            JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface("localhost_job_submission_interface", JobSubmissionProtocol.LOCAL, 1);
            computeResourceDescription.addToJobSubmissionInterfaces(jobSubmissionInterface);

            localhostId = airavataClient.registerComputeResource(new AuthzToken(""), computeResourceDescription);
            ResourceJobManager resourceJobManager = RegisterSampleApplicationsUtils.
                    createResourceJobManager(ResourceJobManagerType.FORK, null, null, null);
            LOCALSubmission submission = new LOCALSubmission();
            submission.setResourceJobManager(resourceJobManager);
            String localSubmission = airavataClient.addLocalSubmissionDetails(new AuthzToken(""), localhostId, 1, submission);
            System.out.println(localSubmission);
            System.out.println("LocalHost Resource Id is " + localhostId);
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private void registerGatewayProfile() throws TException {
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        ComputeResourcePreference localhostResourcePreference = RegisterSampleApplicationsUtils.
                createComputeResourcePreference(localhostId, gatewayId, false, null, null, null, "/tmp");
        gatewayResourceProfile.setGatewayID(gatewayId);
        gatewayResourceProfile.addToComputeResourcePreferences(localhostResourcePreference);
        airavataClient.registerGatewayResourceProfile(new AuthzToken(""), gatewayResourceProfile);
    }


    private void registerEchoModule() throws TException {
        //Register Echo
        echoModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Echo", "1.0", "Echo application description"));
    }

    private void registerEchoDeployment() throws TException {
        System.out.println("#### Registering Application Deployments on Localhost ####");
        //Register Echo
        String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, localhostId, "/bin/echo",
                        ApplicationParallelismType.SERIAL, "Echo application description",
                        null, null, null));
        System.out.println("Successfully registered Echo application on localhost, application Id = " + echoAppDeployId);
    }

    private void registerEchoInterface() {
        try {
            System.out.println("#### Registering Echo Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo", "Hello World",
                    DataType.STRING, null, 1, true,true, false, "A test string to Echo", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING, true, false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            echoInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Echo", "Echo application description",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Echo Application Interface Id " + echoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private void createEchoExperiment() throws TException {
        Project project = new Project();
        project.setName("default-project");
        project.setOwner(userId);
        String projectId = airavataClient.createProject(new AuthzToken(""), gatewayId, project);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setExperimentName("dummy-echo-experiment");
        experimentModel.setProjectId(projectId);
        experimentModel.setUserName(userId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExecutionId(echoInterfaceId);

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        computationalResourceSchedulingModel.setNodeCount(1);
        computationalResourceSchedulingModel.setTotalCPUCount(1);
        computationalResourceSchedulingModel.setTotalPhysicalMemory(512);
        computationalResourceSchedulingModel.setResourceHostId(localhostId);
        userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);

        experimentModel.setUserConfigurationData(userConfigurationDataModel);

        List<InputDataObjectType> experimentInputs = new ArrayList<>();
        experimentInputs.add(RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo", "Hello World",
                DataType.STRING, null, 1, true,true, false, "A test string to Echo", null));
        experimentModel.setExperimentInputs(experimentInputs);
        experimentModel.setExperimentOutputs(airavataClient.getApplicationOutputs(new AuthzToken(""), echoInterfaceId));

        echoExperimentId = airavataClient.createExperiment(new AuthzToken(""), gatewayId, experimentModel);
    }

}
