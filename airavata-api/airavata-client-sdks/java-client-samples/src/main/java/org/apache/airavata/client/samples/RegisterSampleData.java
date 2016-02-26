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

package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.tools.RegisterSampleApplicationsUtils;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.thrift.TException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RegisterSampleData {

    private static final String THRIFT_SERVER_HOST = "127.0.0.1";
    private static final int THRIFT_SERVER_PORT = 8930;

    private Airavata.Client airavataClient;
    private String localhost_ip = "127.0.0.1";
    private String localhostId ;
    private String echoModuleId;
    private String addModuleId;
    private String multiplyModuleId;
    private String subtractModuleId;
    private String sampleScriptDir;
    private String monteXModuleId;
    private String gaussianModuleId;

    private String gatewayId;

    public static void main(String[] args) throws AiravataClientException, TException {
        RegisterSampleData registerSampleData = new RegisterSampleData();
        registerSampleData.init();
        registerSampleData.register();
    }

    public void init() {
        String airavataHome = System.getenv("AIRAVATA_HOME");
        if (airavataHome == null) {
            sampleScriptDir = new File("").getAbsolutePath() +
                    "/modules/distribution/server/src/main/resources/samples/scripts";
        } else {
            sampleScriptDir = airavataHome + "/samples/scripts";
        }
        System.out.println(sampleScriptDir);
    }

    public void register() throws AiravataClientException, TException {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        gatewayId = registerGateway();
        registerLocalhost();
        registerGatewayProfile();
        registerApplicationModules();
        registerApplicationDeployments();
        registerApplicationInterfaces();
    }

    private String registerGateway() throws TException {
        Gateway gateway = new Gateway();
        gateway.setGatewayName("Sample");
        gateway.setGatewayId("sample");
        return airavataClient.addGateway(new AuthzToken(""), gateway);
    }

    private void registerGatewayProfile() throws TException {
        ComputeResourcePreference localhostResourcePreference = RegisterSampleApplicationsUtils.
                createComputeResourcePreference(localhostId, "Sample", false, null, null, null, sampleScriptDir + "/..");
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(gatewayId);
        gatewayResourceProfile.addToComputeResourcePreferences(localhostResourcePreference);
        airavataClient.registerGatewayResourceProfile(new AuthzToken(""), gatewayResourceProfile);
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
//            if (!localSubmission) throw new AiravataClientException();
            System.out.println(localSubmission);
            System.out.println("LocalHost Resource Id is " + localhostId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private void registerApplicationInterfaces() {
        registerAddApplicationInterface();
        registerSubtractApplicationInterface();
        registerMultiplyApplicationInterface();
        registerEchoInterface();
        registerTinkerMonteInterface();
        registerGaussianInterface();
    }

    private void registerGaussianInterface() {
        try {
            System.out.println("#### Registering Gaussian Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(gaussianModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("MainInputFile", null,
                    DataType.URI, null, 1,true,true, false, "Gaussian main input file", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("gaussian.out",
                    "", DataType.URI, true,true, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Gaussian", "Gaussian application",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Gaussian Application Interface Id " + addApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private void registerTinkerMonteInterface() {
        try {
            System.out.println("#### Registering Tinker Monte Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(monteXModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("xyzf", "O16.xyz",
                    DataType.STRING, null, 1, true,true, false, "Tinker monte input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("keyf", "O16.key",
                    DataType.STRING, "-k", 2, true,true, false, "Tinker monte input_2", null);
            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("stps", "20000",
                    DataType.STRING, null, 3, true,true, false, "Tinker monte input_3", null);
            InputDataObjectType input4 = RegisterSampleApplicationsUtils.createAppInput("Ctc", "C",
                    DataType.STRING, null, 4, true,true, false, "Tinker monte input_4", null);
            InputDataObjectType input5 = RegisterSampleApplicationsUtils.createAppInput("stpsZ", "3.0",
                    DataType.STRING, null, 5, true,true, false, "Tinker monte input_5", null);
            InputDataObjectType input6 = RegisterSampleApplicationsUtils.createAppInput("temp", "298",
                    DataType.STRING, null, 6, true,true, false, "Tinker monte input_6", null);
            InputDataObjectType input7 = RegisterSampleApplicationsUtils.createAppInput("Rconv", "0.01",
                    DataType.STRING, null, 7, true,true, false, "Tinker monte input_7", null);


            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);
            applicationInputs.add(input4);
            applicationInputs.add(input5);
            applicationInputs.add(input6);
            applicationInputs.add(input7);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Diskoutputfile_with_dir",
                    "", DataType.URI, true,false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Tinker_Monte", "Monte application",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Monte Application Interface Id " + addApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    private void registerApplicationDeployments() throws TException {
        System.out.println("#### Registering Application Deployments on Localhost ####");
        //Register Echo
        String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, localhostId,
                        sampleScriptDir + "/echo.sh", ApplicationParallelismType.SERIAL, "Echo application description",
                        null, null, null));
        System.out.println("Successfully registered Echo application on localhost, application Id = " + echoAppDeployId);

        //Register Add application
        String addAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(addModuleId, localhostId,
                        sampleScriptDir + "/add.sh", ApplicationParallelismType.SERIAL, "Add application description",
                        null, null, null));
        System.out.println("Successfully registered Add application on localhost, application Id = " + addAppDeployId);

        //Register Multiply application
        String multiplyAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(multiplyModuleId, localhostId,
                        sampleScriptDir + "/multiply.sh", ApplicationParallelismType.SERIAL, "Multiply application description",
                        null, null, null));
        System.out.println("Successfully registered Multiply application on localhost, application Id = " + multiplyAppDeployId);

        //Register Subtract application
        String subtractAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(subtractModuleId, localhostId,
                        sampleScriptDir + "/subtract.sh", ApplicationParallelismType.SERIAL, "Subtract application description ",
                        null, null, null));
        System.out.println("Successfully registered Subtract application on localhost, application Id = " + subtractAppDeployId);

        //Register Tinker monte application
        String tinkerMonteAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(monteXModuleId, localhostId,
                        sampleScriptDir + "/monte.x", ApplicationParallelismType.SERIAL, "Grid chem tinker monte application description ",
                        null, null, null));
        System.out.println("Successfully registered tinker monte application on localhost, application Id = " + tinkerMonteAppDeployId);

        //Register Tinker monte application
        String gaussianAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationDeployment(gaussianModuleId, localhostId,
                        sampleScriptDir + "/gaussian.sh", ApplicationParallelismType.SERIAL, "Grid chem Gaussian application description ",
                        null, null, null));
        System.out.println("Successfully registered Gaussian application on localhost, application Id = " + gaussianAppDeployId);
    }

    private void registerApplicationModules() throws TException {
        //Register Echo
        echoModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Echo", "1.0", "Echo application description"));
        //Register Echo
        addModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Add", "1.0", "Add application description"));
        //Register Echo
        multiplyModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Multiply", "1.0", "Multiply application description"));
        //Register Echo
        subtractModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Subtract", "1.0", "Subtract application description"));
        //Register Monte
        monteXModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Tinker Monte", "1.0", "Grid chem tinker monte application description"));

        // Register gaussian application
        gaussianModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), gatewayId,
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Gaussian", "1.0", "Grid Chem Gaussian application description"));

    }


    public void registerEchoInterface() {
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

            String echoInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Echo", "Echo application description",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Echo Application Interface Id " + echoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerAddApplicationInterface() {
        try {
            System.out.println("#### Registering Add Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(addModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("x", "2",
                    DataType.STRING, null, 1, true,true, false, "Add operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "3",
                    DataType.STRING, null, 2, true,true, false, "Add operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING, true,false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Add", "Add two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Add Application Interface Id " + addApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerMultiplyApplicationInterface() {
        try {
            System.out.println("#### Registering Multiply Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(multiplyModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("x", "4",
                    DataType.STRING, null, 1,true,true, false, "Multiply operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "5",
                    DataType.STRING, null, 2, true,true, false, "Multiply operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING,true,false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String multiplyApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Multiply", "Multiply two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Multiply Application Interface Id " + multiplyApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerSubtractApplicationInterface() {
        try {
            System.out.println("#### Registering Subtract Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(subtractModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("x", "6",
                    DataType.STRING, null, 1,true,true, false, "Subtract operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "7",
                    DataType.STRING, null, 2,true,true, false, "Subtract operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING, true,false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String subtractApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), gatewayId,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Subtract", "Subtract two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Subtract Application Interface Id " + subtractApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
