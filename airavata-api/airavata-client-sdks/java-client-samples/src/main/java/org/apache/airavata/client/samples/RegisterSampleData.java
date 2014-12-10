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
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.thrift.TException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RegisterSampleData {

    private static final String THRIFT_SERVER_HOST = "127.0.0.1";
    private static final int THRIFT_SERVER_PORT = 8930;
    private static final String DEFAULT_GATEWAY = "Sample";

    private Airavata.Client airavataClient;
    private String localhost_ip = "127.0.0.1";
    private String localhostId ;
    private String echoModuleId;
    private String addModuleId;
    private String multiplyModuleId;
    private String subtractModuleId;
    private String sampleScriptDir;

    public static void main(String[] args) throws AiravataClientConnectException, TException {
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

    public void register() throws AiravataClientConnectException, TException {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        registerLocalhost();
//        registerGatewayProfile();
        registerApplicationModules();
        registerApplicationDeployments();
        registerApplicationInterfaces();
    }

    private void registerGatewayProfile() throws TException {
        ComputeResourcePreference localhostResourcePreference = RegisterSampleApplicationsUtils.
                createComputeResourcePreference(localhostId, "Sample", false, null, null, null, sampleScriptDir + "/..");
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(DEFAULT_GATEWAY);
        gatewayResourceProfile.setGatewayName(DEFAULT_GATEWAY);
        gatewayResourceProfile.addToComputeResourcePreferences(localhostResourcePreference);
        airavataClient.registerGatewayResourceProfile(gatewayResourceProfile);
    }

    private void registerLocalhost() {
        try {
            System.out.println("\n #### Registering Localhost Computational Resource #### \n");

            ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils.
                    createComputeResourceDescription("localhost", "LocalHost", null, null);
            localhostId = airavataClient.registerComputeResource(computeResourceDescription);
            ResourceJobManager resourceJobManager = RegisterSampleApplicationsUtils.
                    createResourceJobManager(ResourceJobManagerType.FORK, null, null, null);
            LOCALSubmission submission = new LOCALSubmission();
            submission.setResourceJobManager(resourceJobManager);
            String localSubmission = airavataClient.addLocalSubmissionDetails(localhostId, 1, submission);
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
    }

    private void registerApplicationDeployments() throws TException {
        System.out.println("#### Registering Application Deployments on Localhost ####");
        //Register Echo
        String echoAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, localhostId,
                        sampleScriptDir + "/echo.sh", ApplicationParallelismType.SERIAL, "Echo application description"));
        System.out.println("Successfully registered Echo application on localhost, application Id = " + echoAppDeployId);

        //Register Add application
        String addAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(addModuleId, localhostId,
                        sampleScriptDir + "/add.sh", ApplicationParallelismType.SERIAL, "Add application description"));
        System.out.println("Successfully registered Add application on localhost, application Id = " + addAppDeployId);

        //Register Multiply application
        String multiplyAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(multiplyModuleId, localhostId,
                        sampleScriptDir + "/multiply.sh", ApplicationParallelismType.SERIAL, "Multiply application description"));
        System.out.println("Successfully registered Multiply application on localhost, application Id = " + multiplyAppDeployId);

        //Register Subtract application
        String subtractAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(subtractModuleId, localhostId,
                        sampleScriptDir + "/subtract.sh", ApplicationParallelismType.SERIAL, "Subtract application description "));
        System.out.println("Successfully registered Subtract application on localhost, application Id = " + subtractAppDeployId);
    }

    private void registerApplicationModules() throws TException {
        //Register Echo
        echoModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Echo", "1.0", "Echo application description"));
        //Register Echo
        addModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Add", "1.0", "Add application description"));
        //Register Echo
        multiplyModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Multiply", "1.0", "Multiply application description"));
        //Register Echo
        subtractModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Subtract", "1.0", "Subtract application description"));

    }


    public void registerEchoInterface() {
        try {
            System.out.println("#### Registering Echo Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo", "Hello World",
                    DataType.STRING, null, false, "A test string to Echo", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String echoInterfaceId = airavataClient.registerApplicationInterface(
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
                    DataType.STRING, null, false, "Add operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "3",
                    DataType.STRING, null, false, "Add operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(
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
                    DataType.STRING, null, false, "Multiply operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "5",
                    DataType.STRING, null, false, "Multiply operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String multiplyApplicationInterfaceId = airavataClient.registerApplicationInterface(
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
                    DataType.STRING, null, false, "Subtract operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "7",
                    DataType.STRING, null, false, "Subtract operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String subtractApplicationInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Subtract", "Subtract two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Subtract Application Interface Id " + subtractApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
