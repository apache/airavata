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
import org.apache.airavata.client.tools.RegisterSampleApplications;
import org.apache.airavata.client.tools.RegisterSampleApplicationsUtils;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "gw77.iu.xsede.org";
    public static final int THRIFT_SERVER_PORT = 8930;
//	public static final String THRIFT_SERVER_HOST = "gw111.iu.xsede.org";
//	public static final int THRIFT_SERVER_PORT = 9930;

    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);
    public static final String DEFAULT_USER = "default.registry.user";
    public static final String DEFAULT_GATEWAY = "default";
    private static Airavata.Client airavataClient;

    private static String echoAppId = "Python_Echo_069ea651-4937-4b89-9684-fb0682ac52f5";
    private static String mpiAppId = "HelloMPI_71b6f45e-40c2-46e9-a417-160b2640fcb9";
    private static String wrfAppId = "WRF_7ad5da38-c08b-417c-a9ea-da9298839762";
    private static String amberAppId = "Amber_74ad818e-7633-476a-b861-952de9b0a529";
    private static String gromacsAppId = "GROMACS_05622038-9edd-4cb1-824e-0b7cb993364b";
    private static String espressoAppId = "ESPRESSO_10cc2820-5d0b-4c63-9546-8a8b595593c1";
    private static String lammpsAppId = "LAMMPS_2472685b-8acf-497e-aafe-cc66fe5f4cb6";
    private static String nwchemAppId = "NWChem_2c8fee64-acf9-4a89-b6d3-91eb53c7640c";
    private static String trinityAppId = "Trinity_e894acf5-9bca-46e8-a1bd-7e2d5155191a";
    private static String autodockAppId = "AutoDock_43d9fdd0-c404-49f4-b913-3abf9080a8c9";

    private static String localHost = "localhost";
    private static String trestlesHostName = "trestles.sdsc.xsede.org";
    private static String unicoreHostName = "fsd-cloud15.zam.kfa-juelich.de";
    private static String stampedeHostName = "stampede.tacc.xsede.org";
    private static String br2HostName = "bigred2.uits.iu.edu";
    private static String umassrcHostName = "ghpcc06.umassrc.org";

    private static String gatewayId;

    // unicore service endpoint url
    private static final String unicoreEndPointURL = "https://fsd-cloud15.zam.kfa-juelich.de:7000/INTEROP1/services/BESFactory?res=default_bes_factory";


    public static void main(String[] args) throws Exception {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
        AuthzToken token = new AuthzToken("empty_token");
        System.out.println("API version is " + airavataClient.getAPIVersion());
//        registerApplications(); // run this only the first time
//        Map<String, String> master = airavataClient.getAllUserSSHPubKeys(token, "master");
//        System.out.println(master.size());
        Map<String, JobStatus> jobStatuses = airavataClient.getJobStatuses(token, "SLM3-QEspresso-Stampede_dc2af008-a832-4fba-ab0a-4b61fa79f5b9");
        for (String jobId : jobStatuses.keySet()){
            JobStatus jobStatus = jobStatuses.get(jobId);
            System.out.println(jobId);
            System.out.println(jobStatus.getJobState().toString());
        }
//        createAndLaunchExp();
    }

    private static String fsdResourceId;


    public static void getAvailableAppInterfaceComputeResources(String appInterfaceId) {
        try {
            Map<String, String> availableAppInterfaceComputeResources = airavataClient.
                    getAvailableAppInterfaceComputeResources(new AuthzToken(""), appInterfaceId);
            for (String key : availableAppInterfaceComputeResources.keySet()) {
                System.out.println("id : " + key);
                System.out.println("name : " + availableAppInterfaceComputeResources.get(key));
            }
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

    }


    public static void createGateway() {
        try {
            Gateway gateway = new Gateway();
            gateway.setGatewayId("testGatewayId2");
            gateway.setGatewayName("testGateway2");
            gatewayId = airavataClient.addGateway(new AuthzToken(""), gateway);
            System.out.println(gatewayId);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public static void getGateway(String gatewayId) {
        try {
            Gateway gateway = airavataClient.getGateway(new AuthzToken(""), gatewayId);
            gateway.setDomain("testDomain");
            airavataClient.updateGateway(new AuthzToken(""), gatewayId, gateway);
            List<Gateway> allGateways = airavataClient.getAllGateways(new AuthzToken(""));
            System.out.println(allGateways.size());
            if (airavataClient.isGatewayExist(new AuthzToken(""), gatewayId)) {
                Gateway gateway1 = airavataClient.getGateway(new AuthzToken(""), gatewayId);
                System.out.println(gateway1.getGatewayName());
            }
            boolean b = airavataClient.deleteGateway(new AuthzToken(""), "testGatewayId2");
            System.out.println(b);
        } catch (AiravataSystemException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        } catch (AiravataClientException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

    }


    public static void createAndLaunchExp() throws TException {
        List<String> experimentIds = new ArrayList<String>();
        try {
            for (int i = 0; i < 1; i++) {
//                final String expId = createExperimentForSSHHost(airavata);
//                final String expId = createEchoExperimentForFSD(airavataClient);
//                final String expId = createMPIExperimentForFSD(airavataClient);
//               final String expId = createEchoExperimentForStampede(airavataClient);
//                final String expId = createEchoExperimentForTrestles(airavataClient);
//                final String expId = createExperimentEchoForLocalHost(airavataClient);
//                final String expId = createExperimentWRFTrestles(airavataClient);
//                final String expId = createExperimentForBR2(airavataClient);
//                final String expId = createExperimentForBR2Amber(airavataClient);
//                final String expId = createExperimentWRFStampede(airavataClient);
                final String expId = createExperimentForStampedeAmber(airavataClient);
//                String expId = createExperimentForTrestlesAmber(airavataClient);
//                final String expId = createExperimentGROMACSStampede(airavataClient);
//                final String expId = createExperimentESPRESSOStampede(airavataClient);
//                final String expId = createExperimentLAMMPSStampede(airavataClient);
//                final String expId = createExperimentNWCHEMStampede(airavataClient);
//                final String expId = createExperimentTRINITYStampede(airavataClient);
//                final String expId = createExperimentAUTODOCKStampede(airavataClient); // this is not working , we need to register AutoDock app on stampede
//                final String expId = createExperimentForLSF(airavataClient);
//                final String expId = createExperimentLAMMPSForLSF(airavataClient);
//            	  final String expId = "Ultrascan_ln_eb029947-391a-4ccf-8ace-9bafebe07cc0";
                experimentIds.add(expId);
                System.out.println("Experiment ID : " + expId);
//                updateExperiment(airavata, expId);

                launchExperiment(airavataClient, expId);
            }

            boolean allNotFinished = true;
            while(allNotFinished) {
                allNotFinished = false;
                for (String exId : experimentIds) {
                    ExperimentModel experiment = airavataClient.getExperiment(new AuthzToken(""), exId);
                    if(!experiment.getExperimentStatus().get(0).getState().equals(ExperimentState.COMPLETED)&&
                            !experiment.getExperimentStatus().get(0).getState().equals(ExperimentState.FAILED)
                            &&!experiment.getExperimentStatus().get(0).getState().equals(ExperimentState.CANCELED)){
                        allNotFinished = true;
                    }
                    System.out.println(experiment.getExperimentId() + " " + experiment.getExperimentStatus().get(0).getState().name());
                }
                System.out.println("----------------------------------------------------");
                Thread.sleep(10000);
            }


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

        registerSampleApplications.registerNonXSEDEHosts();

        //Register Gateway Resource Preferences
        registerSampleApplications.registerGatewayResourceProfile();

        //Register all application modules
        registerSampleApplications.registerAppModules();

        //Register all application deployments
        registerSampleApplications.registerAppDeployments();

        //Register all application interfaces
        registerSampleApplications.registerAppInterfaces();
    }

    public static String registerUnicoreEndpoint(String hostName, String hostDesc, JobSubmissionProtocol protocol, SecurityProtocol securityProtocol) throws TException {

        ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils
                .createComputeResourceDescription(hostName, hostDesc, null, null);

        fsdResourceId = airavataClient.registerComputeResource(new AuthzToken(""), computeResourceDescription);

        if (fsdResourceId.isEmpty())
            throw new AiravataClientException();

        System.out.println("FSD Compute ResourceID: " + fsdResourceId);

        JobSubmissionInterface jobSubmission = RegisterSampleApplicationsUtils.createJobSubmissionInterface(fsdResourceId, protocol, 2);
        UnicoreJobSubmission ucrJobSubmission = new UnicoreJobSubmission();
        ucrJobSubmission.setSecurityProtocol(securityProtocol);
        ucrJobSubmission.setUnicoreEndPointURL(unicoreEndPointURL);

        return jobSubmission.getJobSubmissionInterfaceId();
    }

    public static String createEchoExperimentForTrestles(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "admin", "echoExperiment",
					        "SimpleEcho3", echoAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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


    public static String createEchoExperimentForFSD(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo2")) {
                    inputDataObjectType.setValue("http://www.textfiles.com/100/ad.txt");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo3")) {
                    inputDataObjectType.setValue("file:///tmp/test.txt");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin", "echoExperiment",
					        "SimpleEcho2", echoAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);


            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(unicoreHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 1048576);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        userConfigurationData.setGenerateCert(false);
                        userConfigurationData.setUserDN("");
                        simpleExperiment.setUserConfigurationData(userConfigurationData);

                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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


    public static String createMPIExperimentForFSD(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), mpiAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Sample_Input")) {
                    inputDataObjectType.setValue("");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), mpiAppId);


	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin", "mpiExperiment",
					        "HelloMPI", mpiAppId, null);
	        simpleExperiment.setExperimentOutputs(exOut);


            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), mpiAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(unicoreHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 2, 1, 2, "normal", 30, 1048576);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        
                        userConfigurationData.setGenerateCert(false);
                        userConfigurationData.setUserDN("");

                        simpleExperiment.setUserConfigurationData(userConfigurationData);

                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), wrfAppId);
            setWRFInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), wrfAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin", "WRFExperiment",
					        "Testing", wrfAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), wrfAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 2, 32, 1, "development", 90, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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


    private static void setWRFInputs(List<InputDataObjectType> exInputs) {
        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("Config_Namelist_File")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/namelist.input");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("WRF_Initial_Conditions")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/wrfinput_d01");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("WRF_Boundary_File")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/wrfbdy_d01");
            }
        }
    }


    public static String createExperimentGROMACSStampede(Airavata.Client client) throws TException {
        try {

            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), gromacsAppId);
            setGROMACSInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), gromacsAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin",
					        "GromacsExperiment", "Testing", gromacsAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), gromacsAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    private static void setGROMACSInputs(List<InputDataObjectType> exInputs) {
        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("GROMOS_Coordinate_File")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/GROMMACS_FILES/pdb1y6l-EM-vacuum.gro");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("Portable_Input_Binary_File")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/GROMMACS_FILES/pdb1y6l-EM-vacuum.tpr");
            }
        }
    }

    public static String createExperimentESPRESSOStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), espressoAppId);
            setESPRESSOInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), espressoAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin",
					        "EspressoExperiment", "Testing", espressoAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), espressoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    private static void setESPRESSOInputs(List<InputDataObjectType> exInputs) {
        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("AI_Pseudopotential_File")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/ESPRESSO_FILES/Al.sample.in");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("AI_Primitive_Cell")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/ESPRESSO_FILES/Al.pz-vbc.UPF");
            }
        }
    }

    public static String createExperimentTRINITYStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), trinityAppId);
            setTRINITYInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), trinityAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin",
					        "TrinityExperiment", "Testing", trinityAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), trinityAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    private static void setTRINITYInputs(List<InputDataObjectType> exInputs) {
        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("RNA_Seq_Left_Input")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/TRINITY_FILES/reads.left.fq");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("RNA_Seq_Right_Input")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/TRINITY_FILES/reads.right.fq");
            }
        }
    }

    public static String createExperimentLAMMPSStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), lammpsAppId);
            setLAMMPSInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), lammpsAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin",
					        "LAMMPSExperiment", "Testing", lammpsAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), lammpsAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    private static void setLAMMPSInputs(List<InputDataObjectType> exInputs) {
        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("Friction_Simulation_Input")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/LAMMPS_FILES/in.friction");
            }
        }
    }

    public static String createExperimentNWCHEMStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), nwchemAppId);
            setNWCHEMInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), nwchemAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin",
					        "NWchemExperiment", "Testing", nwchemAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), nwchemAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    private static void setNWCHEMInputs(List<InputDataObjectType> exInputs) {
        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("Water_Molecule_Input")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/NWCHEM_FILES/water.nw");
            }
        }
    }

    public static String createExperimentAUTODOCKStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), nwchemAppId);
            setAUTODOCKInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), nwchemAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin",
					        "AutoDockExperiment", "Testing", autodockAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), autodockAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    private static void setAUTODOCKInputs(List<InputDataObjectType> exInputs) {

        for (InputDataObjectType inputDataObjectType : exInputs) {
            if (inputDataObjectType.getName().equalsIgnoreCase("AD4_parameters.dat")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/AD4_parameters.dat");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.A.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.A.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.C.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.C.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.d.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.d.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.e.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.e.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.HD.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.HD.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.maps.fld")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.maps.fld");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.NA.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.NA.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.N.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.N.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("hsg1.OA.map")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.OA.map");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("ind.dpf")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/ind.dpf");
            } else if (inputDataObjectType.getName().equalsIgnoreCase("ind.pdbqt")) {
                inputDataObjectType.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/ind.pdbqt");
            }
        }
    }

    public static String createExperimentWRFTrestles(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), wrfAppId);
            setWRFInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), wrfAppId);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "admin", "WRFExperiment",
					        "Testing", wrfAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), wrfAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
            return client.cloneExperiment(new AuthzToken(""), expId, "cloneExperiment1", null);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }


    public static void updateExperiment(Airavata.Client client, String expId) throws TException {
        try {
            ExperimentModel experiment = client.getExperiment(new AuthzToken(""), expId);
            experiment.setDescription("updatedDescription");
            client.updateExperiment(new AuthzToken(""), expId, experiment);
        } catch (TException e) {
            logger.error("Error occured while creating the experiment...", e.getMessage());
            throw new TException(e);
        }
    }


    public static String createExperimentEchoForLocalHost(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "admin", "echoExperiment",
					        "Echo Test", echoAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(localHost)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
//            List<OutputDataObjectType> exInputs = new ArrayList<OutputDataObjectType>();
//            OutputDataObjectType input = new OutputDataObjectType();
//            input.setName("echo_input");
//            input.setType(DataType.STRING);
//            input.setValue("Echoed_Output=Hello World");
//            exInputs.add(input);
//
//            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
//            OutputDataObjectType output = new OutputDataObjectType();
//            output.setName("Echoed_Output");
//            output.setType(DataType.STRING);
//            output.setValue("");
//            exOut.add(output);
//
//            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
//            String projectId = client.createProject(project);
//
//            ExperimentModel simpleExperiment =
//                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SSHEcho1", sshHostAppId.split(",")[1], exInputs);
//            simpleExperiment.setExperimentOutputs(exOut);
//
//            ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(sshHostAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "sds128");
//            scheduling.setResourceHostId("gw111.iu.xsede.org");
//            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), echoAppId);
//            for (InputDataObjectType inputDataObjectType : exInputs) {
//                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
//                    inputDataObjectType.setValue("Hello World");
//                }
//            }
//            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
	        String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

            ExperimentModel simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY,projectId, "admin", "echoExperiment", "SimpleEcho3", echoAppId, exInputs);
//            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
//            List<OutputDataObjectType> exInputs = new ArrayList<OutputDataObjectType>();
//            OutputDataObjectType input = new OutputDataObjectType();
//            input.setName("echo_input");
//            input.setType(DataType.STRING);
//            input.setValue("Echoed_Output=Hello World");
//            exInputs.add(input);
//
//            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
//            OutputDataObjectType output = new OutputDataObjectType();
//            output.setName("Echoed_Output");
//            output.setType(DataType.STRING);
//            output.setValue("");
//            exOut.add(output);
//
//            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
//            String projectId = client.createProject(project);
//
//            ExperimentModel simpleExperiment =
//                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho4", echoAppId, exInputs);
//            simpleExperiment.setExperimentOutputs(exOut);
//
//            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
//            if (computeResources != null && computeResources.size() != 0){
//                for (String id : computeResources.keySet()){
//                    String resourceName = computeResources.get(id);
//                    if (resourceName.equals(stampedeHostName)){
//                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
//                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
//                        userConfigurationData.setAiravataAutoSchedule(false);
//                        userConfigurationData.setOverrideManualScheduledParams(false);
//                        userConfigurationData.setComputationalResourceScheduling(scheduling);
//                        simpleExperiment.setUserConfigurationData(userConfigurationData);
//                        return client.createExperiment(simpleExperiment);
//                    }
//                }
//            }
//            ComputationalResourceSchedulingModel scheduling =
//                    ExperimentModelUtil.createComputationResourceScheduling(sgeAppId.split(",")[0], 1, 1, 1, "normal", 1, 0, 1, "TG-STA110014S");
//            UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);


            Project project = ProjectModelUtil.createProject("default", "lahiru", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "lahiru",
					        "sshEchoExperiment", "SimpleEchoBR", echoAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(br2HostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    public static String createExperimentForLSF(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), echoAppId);

            for (InputDataObjectType inputDataObjectType : exInputs) {
                inputDataObjectType.setValue("Hello World");
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);
            Project project = ProjectModelUtil.createProject("default", "lg11w", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);


	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "lg11w",
					        "sshEchoExperiment", "StressMem", echoAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);
            simpleExperiment.setExperimentInputs(exInputs);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(umassrcHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 10, 1, 1, "long", 60,1000);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        simpleExperiment.setEmailAddresses(Arrays.asList(new String[]{"test@umassmed.edu"}));
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

    public static String createExperimentLAMMPSForLSF(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), lammpsAppId);

            for (InputDataObjectType inputDataObjectType : exInputs) {
                inputDataObjectType.setName("Friction_Simulation_Input");
                inputDataObjectType.setValue("/Users/lginnali/Downloads/data/in.friction");
                inputDataObjectType.setType(DataType.URI);
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), echoAppId);

            /*OutputDataObjectType outputDataObjectType = exOut.get(0);
            outputDataObjectType.setName("LAMMPS_Simulation_Log");
            outputDataObjectType.setType(DataType.URI);
            outputDataObjectType.setValue("");

            OutputDataObjectType output1 = exOut.get(1);
            output1.setName("LAMMPS.oJobID");
            output1.setType(DataType.URI);
            output1.setValue("");

            exOut.add(outputDataObjectType);
            exOut.add(output1);*/

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, "default", "lg11w",
					        "LAMMPSExperiment", "Testing", lammpsAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), lammpsAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(umassrcHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 10, 16, 1, "long", 60, 1000);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), amberAppId);
//			for (InputDataObjectType inputDataObjectType : exInputs) {
//				if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/02_Heat.rst");
//				} else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/03_Prod.in");
//				} else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/prmtop");
//				}
//
//			}
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
                    inputDataObjectType.setValue("file://root@test-drive.airavata.org:/var/www/experimentData/admin101a290e6330f15a91349159553ae8b6bb1/02_Heat.rst");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
                    inputDataObjectType.setValue("file://root@test-drive.airavata.org:/var/www/experimentData/admin101a290e6330f15a91349159553ae8b6bb1/03_Prod.in");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
                    inputDataObjectType.setValue("file://root@test-drive.airavata.org:/var/www/experimentData/admin101a290e6330f15a91349159553ae8b6bb1/prmtop");
                }
            }

            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), amberAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "admin",
					        "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);


            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(br2HostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "cpu", 20, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), amberAppId);
//			for (InputDataObjectType inputDataObjectType : exInputs) {
//				if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/02_Heat.rst");
//				} else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/03_Prod.in");
//				} else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/prmtop");
//				}
//
//			}
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
                    inputDataObjectType.setValue("file://ogce@stampede.xsede.org:/scratch/01437/ogce/gta-work-dirs/PROCESS_e0610a6c-5778-4a69-a004-f440e29194af/02_Heat.rst");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
                    inputDataObjectType.setValue("file://ogce@stampede.xsede.org:/scratch/01437/ogce/gta-work-dirs/PROCESS_e0610a6c-5778-4a69-a004-f440e29194af/03_Prod.in");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
                    inputDataObjectType.setValue("file://ogce@stampede.xsede.org:/scratch/01437/ogce/gta-work-dirs/PROCESS_e0610a6c-5778-4a69-a004-f440e29194af/prmtop");
                }
            }

            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), amberAppId);


            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "admin",
					        "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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

            List<InputDataObjectType> exInputs = client.getApplicationInputs(new AuthzToken(""), amberAppId);
//			for (InputDataObjectType inputDataObjectType : exInputs) {
//				if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/02_Heat.rst");
//				} else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/03_Prod.in");
//				} else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
//					inputDataObjectType.setValue("/Users/raminder/Documents/Sample/Amber/prmtop");
//				}
//
//			}
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Heat_Restart_File")) {
                    inputDataObjectType.setValue("/Users/chathuri/dev/airavata/source/php/inputs/AMBER_FILES/02_Heat.rst");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
                    inputDataObjectType.setValue("/Users/chathuri/dev/airavata/source/php/inputs/AMBER_FILES/03_Prod.in");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
                    inputDataObjectType.setValue("/Users/chathuri/dev/airavata/source/php/inputs/AMBER_FILES/prmtop");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(new AuthzToken(""), amberAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(new AuthzToken(""), DEFAULT_GATEWAY, project);

	        ExperimentModel simpleExperiment =
			        ExperimentModelUtil.createSimpleExperiment(DEFAULT_GATEWAY, projectId, "admin",
					        "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
	        simpleExperiment.setExperimentOutputs(exOut);
            simpleExperiment.setEnableEmailNotification(true);
//            simpleExperiment.addToEmailAddresses("raman@ogce.org");
            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(new AuthzToken(""), amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceSchedulingModel scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "normal", 20, 1);
                        UserConfigurationDataModel userConfigurationData = new UserConfigurationDataModel();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(new AuthzToken(""), DEFAULT_GATEWAY, simpleExperiment);
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
            client.launchExperiment(new AuthzToken(""), expId, DEFAULT_GATEWAY);
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

    public static List<ExperimentModel> getExperimentsForUser(Airavata.Client client, String user) {
        try {
            return client.getUserExperiments(new AuthzToken(""), DEFAULT_GATEWAY, user, -1, 0);
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
            return client.getUserProjects(new AuthzToken(""), DEFAULT_GATEWAY, user, -1, 0);
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
            ExperimentModel experiment = client.getExperiment(new AuthzToken(""), expId);
            List<ErrorModel> errors = experiment.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (ErrorModel error : errors) {
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

/*
*  #### Registering Application Interfaces ####

#### Registering Echo Interface ####

Local Echo Application Interface Id Echo_57a8f843-4298-46a7-bdac-801adeb77e9f
#### Registering Echo Interface ####

Echo Application Interface Id Echo_b730bf47-6a22-44ac-91f3-91bd463bd627
#### Registering MPI Interface ####

MPI Application Interface Id HelloMPI_71b6f45e-40c2-46e9-a417-160b2640fcb9
#### Registering Amber Interface ####

Amber Application Interface Id Amber_74ad818e-7633-476a-b861-952de9b0a529
#### Registering AutoDock Interface ####

AutoDock Application Interface Id AutoDock_6e1032f4-389c-4e35-bdc8-2c3955f51721
#### Registering Espresso Interface ####

Espresso Application Interface Id ESPRESSO_af507be5-5ab8-499d-b1c6-51c543968bb6
#### Registering Gromacs Interface ####

Gromacs Application Interface Id GROMACS_9476b548-72ce-4550-bde6-959fd513ead8
#### Registering LAMMPS Interface ####

LAMMPS Application Interface Id LAMMPS_73d62891-a0d6-4407-9e89-6cd7d02e5a16
#### Registering Gamess Interface ####

GAMESS Application Interface Id Gamess_2de57c7d-f806-4bb2-956a-c42bfa7851cc
#### Registering NWChem Interface ####

NWChem Application Interface Id NWChem_57cdc71b-b47b-4b88-b949-b35d57c40e0c
#### Registering Trinity Interface ####

Trinity Application Interface Id Trinity_fe1e411a-39e1-497d-b988-3b4159ce3331
#### Registering WRF Interface ####

WRF Application Interface Id WRF_434f8543-91ac-4f59-bca1-bf40be8d6f5a
#### Registering Tinker Monte Application Interface ####
Monte Application Interface Id Tinker_Monte_f3949619-faf5-43d8-9c52-3ace4ca9f258
#### Registering Gaussian Application Interface ####
Gaussian Application Interface Id Gaussian_a212c1d7-75ca-46c1-b11a-c8d431b5f05f
#### Registering Ultrascan Application Interface ####
Ultrascan Application Interface Id Ultrascan_3749018d-2cee-407b-a7cd-fdf86d525ca5
*/
