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
import org.apache.airavata.client.tools.RegisterSampleApplicationsUtils;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
//	public static final String THRIFT_SERVER_HOST = "gw111.iu.xsede.org";
//	public static final int THRIFT_SERVER_PORT = 9930;

    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default";
    private static Airavata.Client airavataClient;

    private static String echoAppId = "Echo_066691af-2507-4bf1-905a-d6bb7a2d2bd1";
    private static String mpiAppId = "HelloMPI_bfd56d58-6085-4b7f-89fc-646576830518";
    private static String wrfAppId = "WRF_7ad5da38-c08b-417c-a9ea-da9298839762";
    private static String amberAppId = "Amber_cb54b269-cf79-4276-8dbb-2ec16b759cc6";
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
        System.out.println("API version is " + airavataClient.getAPIVersion());
//        testCredentialStore();
//        registerApplications(); // run this only the first time
//        createAndLaunchExp();
    }

    private static String fsdResourceId;


    public static void  testCredentialStore() throws Exception {
//        String token = airavataClient.generateAndRegisterSSHKeys(DEFAULT_GATEWAY, "admin");
//        System.out.println(token);
        String sshPubKey = airavataClient.getSSHPubKey("90438471-507d-4a9f-9287-081fc816e0f2", DEFAULT_GATEWAY);
        System.out.println(sshPubKey);
        Map<String, String> sshPubKeys = airavataClient.getAllUserSSHPubKeys("admin");
        for (String token : sshPubKeys.keySet()){
            System.out.println(token + " : " + sshPubKeys.get(token));
        }
    }

    public static void getAvailableAppInterfaceComputeResources(String appInterfaceId) {
        try {
            Map<String, String> availableAppInterfaceComputeResources = airavataClient.getAvailableAppInterfaceComputeResources(appInterfaceId);
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
            gatewayId = airavataClient.addGateway(gateway);
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
            Gateway gateway = airavataClient.getGateway(gatewayId);
            gateway.setDomain("testDomain");
            airavataClient.updateGateway(gatewayId, gateway);
            List<Gateway> allGateways = airavataClient.getAllGateways();
            System.out.println(allGateways.size());
            if (airavataClient.isGatewayExist(gatewayId)) {
                Gateway gateway1 = airavataClient.getGateway(gatewayId);
                System.out.println(gateway1.getGatewayName());
            }
            boolean b = airavataClient.deleteGateway("testGatewayId2");
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
               final String expId = createEchoExperimentForStampede(airavataClient);
//                final String expId = createEchoExperimentForTrestles(airavataClient);
//                final String expId = createExperimentEchoForLocalHost(airavataClient);
//                final String expId = createExperimentWRFTrestles(airavataClient);
//                final String expId = createExperimentForBR2(airavataClient);
//                final String expId = createExperimentForBR2Amber(airavataClient);
//                final String expId = createExperimentWRFStampede(airavataClient);
//                final String expId = createExperimentForStampedeAmber(airavataClient);
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
                    Experiment experiment = airavataClient.getExperiment(exId);
                    if(!experiment.getExperimentStatus().getExperimentState().equals(ExperimentState.COMPLETED)&&
                            !experiment.getExperimentStatus().getExperimentState().equals(ExperimentState.FAILED)
                            &&!experiment.getExperimentStatus().getExperimentState().equals(ExperimentState.CANCELED)){
                        allNotFinished = true;
                    }
                    System.out.println(experiment.getExperimentID() + " " + experiment.getExperimentStatus().getExperimentState().name());
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

        fsdResourceId = airavataClient.registerComputeResource(computeResourceDescription);

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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho3", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo2")) {
                    inputDataObjectType.setValue("http://www.textfiles.com/100/ad.txt");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo3")) {
                    inputDataObjectType.setValue("file:///tmp/test.txt");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "echoExperiment", "SimpleEcho2", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);


            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(unicoreHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1048576, "sds128");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        
                        userConfigurationData.setGenerateCert(false);
                        userConfigurationData.setUserDN("");

                        // set output directory 
                        AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
                        dataHandling.setOutputDataDir("/tmp/airavata/output/" + UUID.randomUUID().toString() + "/");
                        userConfigurationData.setAdvanceOutputDataHandling(dataHandling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);

                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(mpiAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Sample_Input")) {
                    inputDataObjectType.setValue("");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(mpiAppId);


            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "mpiExperiment", "HelloMPI", mpiAppId, null);
            simpleExperiment.setExperimentOutputs(exOut);


            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(mpiAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(unicoreHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 2, 1, 2, "normal", 30, 0, 1048576, "sds128");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        
                        userConfigurationData.setGenerateCert(false);
                        userConfigurationData.setUserDN("");

                        // set output directory 
                        AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
                        dataHandling.setOutputDataDir("/tmp/airavata/output/" + UUID.randomUUID().toString() + "/");
                        userConfigurationData.setAdvanceOutputDataHandling(dataHandling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);

                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(wrfAppId);
            setWRFInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(wrfAppId);

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
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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

            List<InputDataObjectType> exInputs = client.getApplicationInputs(gromacsAppId);
            setGROMACSInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(gromacsAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "GromacsExperiment", "Testing", gromacsAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(gromacsAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(espressoAppId);
            setESPRESSOInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(espressoAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "EspressoExperiment", "Testing", espressoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(espressoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(trinityAppId);
            setTRINITYInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(trinityAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "TrinityExperiment", "Testing", trinityAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(trinityAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(lammpsAppId);
            setLAMMPSInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(lammpsAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "LAMMPSExperiment", "Testing", lammpsAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(lammpsAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(nwchemAppId);
            setNWCHEMInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(nwchemAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "NWchemExperiment", "Testing", nwchemAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(nwchemAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(nwchemAppId);
            setAUTODOCKInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(nwchemAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "AutoDockExperiment", "Testing", autodockAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(autodockAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 16, 1, "development", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(wrfAppId);
            setWRFInputs(exInputs);
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(wrfAppId);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "WRFExperiment", "Testing", wrfAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(wrfAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(trestlesHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, "sds128");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);

            Project project = ProjectModelUtil.createProject("project1", "admin", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "Echo Test", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(localHost)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, "");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "echoExperiment", "SimpleEcho3", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(echoAppId);
            for (InputDataObjectType inputDataObjectType : exInputs) {
                if (inputDataObjectType.getName().equalsIgnoreCase("Input_to_Echo")) {
                    inputDataObjectType.setValue("Hello World");
                }
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);


            Project project = ProjectModelUtil.createProject("default", "lahiru", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "lahiru", "sshEchoExperiment", "SimpleEchoBR", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(br2HostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, null);
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(echoAppId);

            for (InputDataObjectType inputDataObjectType : exInputs) {
                inputDataObjectType.setValue("Hello World");
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);
            Project project = ProjectModelUtil.createProject("default", "lg11w", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);


            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "lg11w", "sshEchoExperiment", "StressMem", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);
            simpleExperiment.setExperimentInputs(exInputs);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(umassrcHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 10, 1, 1, "long", 60, 0, 1000, "airavata");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        simpleExperiment.setEmailAddresses(Arrays.asList(new String[]{"test@umassmed.edu"}));
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(lammpsAppId);

            for (InputDataObjectType inputDataObjectType : exInputs) {
                inputDataObjectType.setName("Friction_Simulation_Input");
                inputDataObjectType.setValue("/Users/lginnali/Downloads/data/in.friction");
                inputDataObjectType.setType(DataType.URI);
            }
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(echoAppId);

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

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "lg11w", "LAMMPSExperiment", "Testing", lammpsAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(lammpsAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(umassrcHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 10, 16, 1, "long", 60, 0, 1000, "airavata");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(amberAppId);
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

            List<OutputDataObjectType> exOut = client.getApplicationOutputs(amberAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

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
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            List<InputDataObjectType> exInputs = client.getApplicationInputs(amberAppId);
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
                    inputDataObjectType.setValue("/Users/lginnali/Downloads/02_Heat.rst");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Production_Control_File")) {
                    inputDataObjectType.setValue("/Users/lginnali/Downloads/03_Prod.in");
                } else if (inputDataObjectType.getName().equalsIgnoreCase("Parameter_Topology_File")) {
                    inputDataObjectType.setValue("/Users/lginnali/Downloads/prmtop");
                }
            }

            List<OutputDataObjectType> exOut = client.getApplicationOutputs(amberAppId);


            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(amberAppId);
            if (computeResources != null && computeResources.size() != 0) {
                for (String id : computeResources.keySet()) {
                    String resourceName = computeResources.get(id);
                    if (resourceName.equals(stampedeHostName)) {
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "development", 20, 0, 1, "TG-STA110014S");
                        UserConfigurationData userConfigurationData = new UserConfigurationData();
                        userConfigurationData.setAiravataAutoSchedule(false);
                        userConfigurationData.setOverrideManualScheduledParams(false);
                        userConfigurationData.setComputationalResourceScheduling(scheduling);
                        simpleExperiment.setUserConfigurationData(userConfigurationData);
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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

            List<InputDataObjectType> exInputs = client.getApplicationInputs(amberAppId);
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
            List<OutputDataObjectType> exOut = client.getApplicationOutputs(amberAppId);

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(DEFAULT_GATEWAY, project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);
            simpleExperiment.setEnableEmailNotification(true);
//            simpleExperiment.addToEmailAddresses("raman@ogce.org");
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
                        return client.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
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
            String tokenId = "-0bbb-403b-a88a-42b6dbe198e9";
            client.launchExperiment(expId, tokenId);
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
            return client.getAllUserExperiments(DEFAULT_GATEWAY, user);
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
            return client.getAllUserProjects(DEFAULT_GATEWAY, user);
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
            return client.searchProjectsByProjectName(DEFAULT_GATEWAY, user, projectName);
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
            return client.searchProjectsByProjectDesc(DEFAULT_GATEWAY, user, desc);
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
            return client.searchExperimentsByName(DEFAULT_GATEWAY, user, expName);
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
            return client.searchExperimentsByDesc(DEFAULT_GATEWAY, user, desc);
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
            return client.searchExperimentsByApplication(DEFAULT_GATEWAY, user, app);
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
