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
import org.apache.airavata.client.tools.RegisterSampleApplications;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "149.165.228.109";
    public static final int THRIFT_SERVER_PORT = 9930;
    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static Airavata.Client airavataClient;
    private static String echoAppId = "Echo_c6e6aaac-7d9d-44fc-aba2-63b5100528e8";
    private static String wrfAppId = "WRF_5f097c9c-7066-49ec-aed7-4e39607b3adc";
    private static String amberAppId = "Amber_89906be6-5678-49a6-9d04-a0604fbdef2e";

    private static String localHost = "149.165.228.109";
    private static String trestlesHostName = "trestles.sdsc.xsede.org";
    private static String stampedeHostName = "stampede.tacc.xsede.org";
    private static String br2HostName = "bigred2.uits.iu.edu";

    public static void main(String[] args) {
        try {
            airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavataClient.getAPIVersion());
//            registerApplications();
            Date date = new Date();
            long time = date.getTime();
            int numberOfRequests = 1;
            for (int i = 0; i < numberOfRequests; i++) {
//                (new Thread() {
//                    @Override
//                    public void run() {
                        try {
                            launchExperiment(airavataClient, createExperimentForBR2(airavataClient));
                        } catch (Exception e) {
                            logger.error("Error while connecting with server", e.getMessage());
                            e.printStackTrace();
                        }
                    }
            long time1 = (date.getTime()-time);
            System.out.println("Number of requests: " + numberOfRequests + " time taken Miliseconds: " + time1);

//                }).start();
//            }

        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void registerApplications() {
        RegisterSampleApplications registerSampleApplications = new RegisterSampleApplications(airavataClient);

        // register localhost compute host
        registerSampleApplications.registerLocalHost();

        //Register all compute hosts
        registerSampleApplications.registerXSEDEHosts();

        //Register Gateway Resource Preferences
        registerSampleApplications.registerGatewayResourceProfile();

        //Register all application modules
        registerSampleApplications.registerAppModules();

        //Register all application deployments
        registerSampleApplications.registerAppDeployments();

        //Register all application interfaces
        registerSampleApplications.registerAppInterfaces();
    }

    public static String createEchoExperimentForTrestles(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Echoed_Output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("echo_output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "echoExperiment", "SimpleEcho2", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, "sds128");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static String createExperimentWRFStampede(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Config_Namelist_File");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/namelist.input");

            DataObjectType input1 = new DataObjectType();
            input1.setKey("WRF_Initial_Conditions");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/wrfinput_d01");

            DataObjectType input2 = new DataObjectType();
            input2.setKey("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/wrf_sample_inputs/wrfbdy_d01");

            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("WRF_Output");
            output.setType(DataType.URI);
            output.setValue("");

            DataObjectType output1 = new DataObjectType();
            output1.setKey("WRF_Execution_Log");
            output1.setType(DataType.URI);
            output1.setValue("");


            exOut.add(output);
            exOut.add(output1);


            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "WRFExperiment", "Testing", wrfAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(wrfAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 2, 32, 1, "development", 90, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static String createExperimentWRFTrestles(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("WRF_Namelist");
            input.setType(DataType.URI);
            input.setValue("/Users/chathuri/Downloads/wrf_sample_inputs/namelist.input");

            DataObjectType input1 = new DataObjectType();
            input1.setKey("WRF_Input_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/chathuri/Downloads/wrf_sample_inputs/wrfinput_d01");

            DataObjectType input2 = new DataObjectType();
            input2.setKey("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/chathuri/Downloads/wrf_sample_inputs/wrfbdy_d01");

            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("WRF_Output");
            output.setType(DataType.URI);
            output.setValue("");

            DataObjectType output1 = new DataObjectType();
            output1.setKey("WRF_Execution_Log");
            output1.setType(DataType.URI);
            output1.setValue("");


            exOut.add(output);
            exOut.add(output1);


            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "WRFExperiment", "Testing", wrfAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(wrfAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, "sds128");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static String cloneExperiment(Airavata.Client client, String expId) throws TException {
        try {
            return client.cloneExperiment(expId, "cloneExperiment1");
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }


    public static void updateExperiment(Airavata.Client client, String expId) throws TException {
        try {
            Experiment experiment = client.getExperiment(expId);
            experiment.setDescription("updatedDescription");
            client.updateExperiment(expId, experiment);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }


    public static String createExperimentEchoForLocalHost(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Echoed_Output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("Echoed_Output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "Echo Test", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(localHost)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, "");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

//    public static String createExperimentForSSHHost(Airavata.Client client) throws TException {
//        try {
//            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
//            DataObjectType input = new DataObjectType();
//            input.setKey("echo_input");
//            input.setType(DataType.STRING);
//            input.setValue("echo_output=Hello World");
//            exInputs.add(input);
//
//            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
//            DataObjectType output = new DataObjectType();
//            output.setKey("echo_output");
//            output.setType(DataType.STRING);
//            output.setValue("");
//            exOut.add(output);
//
//            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
//            String projectId = client.createProject(project);
//
//            Experiment simpleExperiment =
//                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SSHEcho1", sshHostAppId.split(",")[1], exInputs);
//            simpleExperiment.setExperimentOutputs(exOut);
//
//            ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(sshHostAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "sds128");
//            scheduling.setResourceHostId("gw111.iu.xsede.org");
//            UserConfigurationData userConfigurationData = new UserConfigurationData();
//            userConfigurationData.setAiravataAutoSchedule(false);
//            userConfigurationData.setOverrideManualScheduledParams(false);
//            userConfigurationData.setComputationalResourceScheduling(scheduling);
//            simpleExperiment.setUserConfigurationData(userConfigurationData);
//            return client.createExperiment(simpleExperiment);
//        } catch (AiravataSystemException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new AiravataSystemException(e);
//        } catch (InvalidRequestException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new InvalidRequestException(e);
//        } catch (AiravataClientException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new AiravataClientException(e);
//        } catch (TException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new TException(e);
//        }
//    }

    public static String createEchoExperimentForStampede(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Echoed_Output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("Echoed_Output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho3", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

//    public static String createEchoExperimentForLonestar(Airavata.Client client) throws TException {
//        try {
//            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
//            DataObjectType input = new DataObjectType();
//            input.setKey("echo_input");
//            input.setType(DataType.STRING);
//            input.setValue("echo_output=Hello World");
//            exInputs.add(input);
//
//            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
//            DataObjectType output = new DataObjectType();
//            output.setKey("echo_output");
//            output.setType(DataType.STRING);
//            output.setValue("");
//            exOut.add(output);
//
//            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
//            String projectId = client.createProject(project);
//
//            Experiment simpleExperiment =
//                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho4", echoAppId, exInputs);
//            simpleExperiment.setExperimentOutputs(exOut);
//
//            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
//            if (computeResources != null && computeResources.size() != 0){
//                for (String id : computeResources.keySet()){
//                    String resourceName = computeResources.get(id);
//                    if (resourceName.equals(stampedeHostName)){
//                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
//                        UserConfigurationData userConfigurationData = new UserConfigurationData();
//                        userConfigurationData.setAiravataAutoSchedule(false);
//                        userConfigurationData.setOverrideManualScheduledParams(false);
//                        userConfigurationData.setComputationalResourceScheduling(scheduling);
//                        simpleExperiment.setUserConfigurationData(userConfigurationData);
//                        return client.createExperiment(simpleExperiment);
//                    }
//                }
//            }
//            ComputationalResourceScheduling scheduling =
//                    ExperimentModelUtil.createComputationResourceScheduling(sgeAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
//            UserConfigurationData userConfigurationData = new UserConfigurationData();
//            userConfigurationData.setAiravataAutoSchedule(false);
//            userConfigurationData.setOverrideManualScheduledParams(false);
//            userConfigurationData.setComputationalResourceScheduling(scheduling);
//            simpleExperiment.setUserConfigurationData(userConfigurationData);
//            return client.createExperiment(simpleExperiment);
//        } catch (AiravataSystemException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new AiravataSystemException(e);
//        } catch (InvalidRequestException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new InvalidRequestException(e);
//        } catch (AiravataClientException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new AiravataClientException(e);
//        } catch (LaunchValidationException e) {
//            logger.error("Validation failed" + e.getErrorMessage());
//            org.apache.airavata.model.error.ValidationResults validationResult = e.getValidationResult();
//            for (org.apache.airavata.model.error.ValidatorResult vResult : validationResult.getValidationResultList()) {
//                if (!vResult.isSetResult()) {
//                    System.out.println("Error:" + vResult.getErrorDetails());
//                }
//            }
//            throw e;
//        } catch (TException e) {
//            logger.error("Error occured while creating the experiment...", e.getMessage());
//            throw new TException(e);
//        }
//    }

    public static String createExperimentForBR2(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Echoed_Output=Hello World");
            exInputs.add(input);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("Echoed_Output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);

            Project project = ProjectModelUtil.createProject("default", "lahiru", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "lahiru", "sshEchoExperiment", "SimpleEchoBR", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(br2HostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, null);
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static String createExperimentForBR2Amber(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Heat_Restart_File");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
            exInputs.add(input);

            DataObjectType input1 = new DataObjectType();
            input1.setKey("Production_Control_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
            exInputs.add(input1);

            DataObjectType input2 = new DataObjectType();
            input2.setKey("Parameter_Topology_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
            exInputs.add(input2);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("AMBER_Execution_Summary");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            DataObjectType output1 = new DataObjectType();
            output1.setKey("AMBER_Execution_log");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            DataObjectType output2 = new DataObjectType();
            output2.setKey("AMBER_Trajectory_file");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            DataObjectType output3 = new DataObjectType();
            output3.setKey("AMBER_Restart_file");
            output3.setType(DataType.URI);
            output3.setValue("");
            exOut.add(output3);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);


            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(br2HostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "cpu", 20, 0, 1, null);
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static String createExperimentForStampedeAmber(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Heat_Restart_File");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
            exInputs.add(input);

            DataObjectType input1 = new DataObjectType();
            input1.setKey("Production_Control_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
            exInputs.add(input1);

            DataObjectType input2 = new DataObjectType();
            input2.setKey("Parameter_Topology_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
            exInputs.add(input2);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("AMBER_Execution_Summary");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            DataObjectType output1 = new DataObjectType();
            output1.setKey("AMBER_Execution_Summary");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            DataObjectType output2 = new DataObjectType();
            output2.setKey("AMBER_Trajectory_file");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            DataObjectType output3 = new DataObjectType();
            output3.setKey("AMBER_Restart_file");
            output3.setType(DataType.URI);
            output3.setValue("");
            exOut.add(output3);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "development", 20, 0, 1, null);
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static String createExperimentForTrestlesAmber(Airavata.Client client) throws TException {
        try {
            List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
            DataObjectType input = new DataObjectType();
            input.setKey("Heat_Restart_File");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
            exInputs.add(input);

            DataObjectType input1 = new DataObjectType();
            input1.setKey("Production_Control_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
            exInputs.add(input1);

            DataObjectType input2 = new DataObjectType();
            input2.setKey("Production_Control_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
            exInputs.add(input2);

            List<DataObjectType> exOut = new ArrayList<DataObjectType>();
            DataObjectType output = new DataObjectType();
            output.setKey("AMBER_Execution_Summary");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            DataObjectType output1 = new DataObjectType();
            output1.setKey("AMBER_Execution_log");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            DataObjectType output2 = new DataObjectType();
            output2.setKey("AMBER_Trajectory_file");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            DataObjectType output3 = new DataObjectType();
            output3.setKey("AMBER_Restart_file");
            output3.setType(DataType.URI);
            output3.setValue("");
            exOut.add(output3);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 0, 1, null);
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(simpleExperiment);
                    }
                }
            }
        } catch (AiravataSystemException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
        return null;
    }

    public static void launchExperiment(Airavata.Client client, String expId)
            throws TException {
        try {
            String sshTokenId = "2c308fa9-99f8-4baa-92e4-d062e311483c";
            String gsisshTokenId = "61abd2ff-f92b-4901-a077-07b51abe2c5d";
            client.launchExperiment(expId, sshTokenId);
        } catch (ExperimentNotFoundException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new ExperimentNotFoundException(e);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new AiravataSystemException(e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new InvalidRequestException(e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new AiravataClientException(e);
        } catch (TException e) {
            logger.error("Error occured while launching the experiment...", e.getMessage());
            throw new TException(e);
        }
    }

    public static List<Experiment> getExperimentsForUser(Airavata.Client client, String user) {
        try {
            return client.getAllUserExperiments(user);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> getAllUserProject(Airavata.Client client, String user) {
        try {
            return client.getAllUserProjects(user);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> searchProjectsByProjectName(Airavata.Client client, String user, String projectName) {
        try {
            return client.searchProjectsByProjectName(user, projectName);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Project> searchProjectsByProjectDesc(Airavata.Client client, String user, String desc) {
        try {
            return client.searchProjectsByProjectDesc(user, desc);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<ExperimentSummary> searchExperimentsByName(Airavata.Client client, String user, String expName) {
        try {
            return client.searchExperimentsByName(user, expName);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> searchExperimentsByDesc(Airavata.Client client, String user, String desc) {
        try {
            return client.searchExperimentsByDesc(user, desc);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ExperimentSummary> searchExperimentsByApplication(Airavata.Client client, String user, String app) {
        try {
            return client.searchExperimentsByApplication(user, app);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getExperiment(Airavata.Client client, String expId) throws Exception {
        try {
            Experiment experiment = client.getExperiment(expId);
            List<ErrorDetails> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (ErrorDetails error : errors) {
                    System.out.println("ERROR MESSAGE : " + error.getActualErrorMessage());
                }
            }

        } catch (ExperimentNotFoundException e) {
            logger.error("Experiment does not exist", e);
            throw new ExperimentNotFoundException("Experiment does not exist");
        } catch (AiravataSystemException e) {
            logger.error("Error while retrieving experiment", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        } catch (InvalidRequestException e) {
            logger.error("Error while retrieving experiment", e);
            throw new InvalidRequestException("Error while retrieving experiment");
        } catch (AiravataClientException e) {
            logger.error("Error while retrieving experiment", e);
            throw new AiravataClientException(AiravataErrorType.INTERNAL_ERROR);
        }
    }
}
