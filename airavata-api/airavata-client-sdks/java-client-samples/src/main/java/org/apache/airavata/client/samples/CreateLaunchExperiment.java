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
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.SecurityProtocol;
import org.apache.airavata.model.appcatalog.computeresource.UnicoreJobSubmission;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.util.ProjectModelUtil;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateLaunchExperiment {

    //FIXME: Read from a config file
    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
//	public static final String THRIFT_SERVER_HOST = "gw127.iu.xsede.org";
//	public static final int THRIFT_SERVER_PORT = 9930;	
	
    private final static Logger logger = LoggerFactory.getLogger(CreateLaunchExperiment.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static Airavata.Client airavataClient;

    private static String echoAppId = "Echo_2e539083-665d-40fd-aaa2-4a751028326b";
    private static String mpiAppId = "HelloMPI_720e159f-198f-4daa-96ca-9f5eafee92c9";
    private static String wrfAppId = "WRF_7ad5da38-c08b-417c-a9ea-da9298839762";
    private static String amberAppId = "Amber_eda074ea-223d-49d7-a942-6c8742249f36";
    private static String gromacsAppId = "GROMACS_05622038-9edd-4cb1-824e-0b7cb993364b";
    private static String espressoAppId = "ESPRESSO_10cc2820-5d0b-4c63-9546-8a8b595593c1";
    private static String lammpsAppId = "LAMMPS_10893eb5-3840-438c-8446-d26c7ecb001f";
    private static String nwchemAppId = "NWChem_2c8fee64-acf9-4a89-b6d3-91eb53c7640c";
    private static String trinityAppId = "Trinity_e894acf5-9bca-46e8-a1bd-7e2d5155191a";
    private static String autodockAppId = "AutoDock_43d9fdd0-c404-49f4-b913-3abf9080a8c9";


    private static String localHost = "localhost";
    private static String trestlesHostName = "trestles.sdsc.xsede.org";
    private static String unicoreHostName = "fsd-cloud15.zam.kfa-juelich.de";
    private static String stampedeHostName = "stampede.tacc.xsede.org";
    private static String br2HostName = "bigred2.uits.iu.edu";
    
 // unicore service endpoint url
    private static final String unicoreEndPointURL = "https://fsd-cloud15.zam.kfa-juelich.de:7000/INTEROP1/services/BESFactory?res=default_bes_factory";
    
    
    public static void main(String[] args) throws Exception {
                airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
                System.out.println("API version is " + airavataClient.getAPIVersion());
//                registerApplications(); // run this only the first time
                createAndLaunchExp();
    }
    
    private static String fsdResourceId;


    public static void createAndLaunchExp() throws TException {
//        final String expId = createEchoExperimentForFSD(airavataClient);
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
//                final String expId = createExperimentForStampedeAmber(airavataClient);
                final String expId = createExperimentForTrestlesAmber(airavataClient);
//                final String expId = createExperimentGROMACSStampede(airavataClient);
//                final String expId = createExperimentESPRESSOStampede(airavataClient);
//                final String expId = createExperimentLAMMPSStampede(airavataClient);
//                final String expId = createExperimentNWCHEMStampede(airavataClient);
//                final String expId = createExperimentTRINITYStampede(airavataClient);
//                final String expId = createExperimentAUTODOCKStampede(airavataClient); // this is not working , we need to register AutoDock app on stampede
//            	  final String expId = "Ultrascan_ln_eb029947-391a-4ccf-8ace-9bafebe07cc0";
            	System.out.println("Experiment ID : " + expId);
//                updateExperiment(airavata, expId);
                
                launchExperiment(airavataClient, expId);
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
		
		System.out.println("FSD Compute ResourceID: "+fsdResourceId);
		
		JobSubmissionInterface jobSubmission = RegisterSampleApplicationsUtils.createJobSubmissionInterface(fsdResourceId, protocol, 2);
		UnicoreJobSubmission ucrJobSubmission = new UnicoreJobSubmission();
		ucrJobSubmission.setSecurityProtocol(securityProtocol);
		ucrJobSubmission.setUnicoreEndPointURL(unicoreEndPointURL);
		
		return jobSubmission.getJobSubmissionInterfaceId();
	}
    
    public static String createEchoExperimentForTrestles(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Echo_Input");
            input.setType(DataType.STRING);
            input.setValue("Hello World");
            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("STDOUT");
            output.setType(DataType.STDOUT);
            output.setValue("");
            exOut.add(output);
            
//            OutputDataObjectType output2 = new OutputDataObjectType();
//            output2.setName("Echoed_Output2");
//            output2.setType(DataType.URI);
//            output2.setValue("file:///tmp/test.txt");
//            exOut.add(output2);
//            
            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "echoExperiment", "SimpleEcho2", echoAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);

            Map<String, String> computeResources = airavataClient.getAvailableAppInterfaceComputeResources(echoAppId);
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
    
    
    public static String createEchoExperimentForFSD(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Hello World");
            
            
            InputDataObjectType i2 = new InputDataObjectType();
            i2.setName("Input_to_Echo2");
            i2.setType(DataType.URI);
            i2.setValue("http://www.textfiles.com/100/ad.txt");
            
            InputDataObjectType i3 = new InputDataObjectType();
            i3.setName("Input_to_Echo3");
            i3.setType(DataType.URI);
            i3.setValue("file:///tmp/test.txt");
            
            exInputs.add(input);
            exInputs.add(i2);
            exInputs.add(i3);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("Echoed_Output");
            output.setType(DataType.STRING);
            output.setValue("test.txt");
            exOut.add(output);
            
            
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
                        
                        // set output directory 
                        AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
                        dataHandling.setOutputDataDir("/tmp/airavata/output/"+UUID.randomUUID().toString()+"/");
                        userConfigurationData.setAdvanceOutputDataHandling(dataHandling);
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
    
    
    public static String createMPIExperimentForFSD(Airavata.Client client) throws TException {
        try {
           
        	List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Sample_Input");
            input.setType(DataType.STRING);
            input.setValue("");
        	exInputs.add(input);
            
            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("Sample_Output");
            output.setType(DataType.STRING);
            output.setValue("");
            exOut.add(output);
            
            Experiment simpleExperiment = 
                    ExperimentModelUtil.createSimpleExperiment("default", "admin", "mpiExperiment", "HelloMPI", mpiAppId, null);
//          simpleExperiment.setExperimentOutputs(exOut);
            
            
            
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
                        
                        // set output directory 
                        AdvancedOutputDataHandling dataHandling = new AdvancedOutputDataHandling();
                        dataHandling.setOutputDataDir("/tmp/airavata/output/"+UUID.randomUUID().toString()+"/");
                        userConfigurationData.setAdvanceOutputDataHandling(dataHandling);
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
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Config_Namelist_File");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/namelist.input");

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("WRF_Initial_Conditions");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/wrfinput_d01");

            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/wrfbdy_d01");

            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("WRF_Output");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("WRF_Execution_Log");
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


    public static String createExperimentGROMACSStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("GROMOS_Coordinate_File");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/GROMMACS_FILES/pdb1y6l-EM-vacuum.gro");

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("Portable_Input_Binary_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/GROMMACS_FILES/pdb1y6l-EM-vacuum.tpr");

            exInputs.add(input);
            exInputs.add(input1);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("pdb1y6l-EM-vacuum.tpr.trr");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("pdb1y6l-EM-vacuum.tpr.edr");
            output1.setType(DataType.URI);
            output1.setValue("");

            OutputDataObjectType output2 = new OutputDataObjectType();
            output2.setName("pdb1y6l-EM-vacuum.tpr.log");
            output2.setType(DataType.URI);
            output2.setValue("");

            OutputDataObjectType output3 = new OutputDataObjectType();
            output3.setName("pdb1y6l-EM-vacuum.gro");
            output3.setType(DataType.URI);
            output3.setValue("");

            OutputDataObjectType output4 = new OutputDataObjectType();
            output4.setName("GROMACS.oJobID");
            output4.setType(DataType.URI);
            output4.setValue("");


            exOut.add(output);
            exOut.add(output1);
            exOut.add(output2);
            exOut.add(output3);
            exOut.add(output4);


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

    public static String createExperimentESPRESSOStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("AI_Pseudopotential_File");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/ESPRESSO_FILES/Al.sample.in");

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("AI_Primitive_Cell");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/ESPRESSO_FILES/Al.pz-vbc.UPF");

            exInputs.add(input);
            exInputs.add(input1);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("ESPRESSO_Execution_Log");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("ESPRESSO_WFC_Binary_file");
            output1.setType(DataType.URI);
            output1.setValue("");

            OutputDataObjectType output2 = new OutputDataObjectType();
            output2.setName("Al_exc3.wfc1");
            output2.setType(DataType.URI);
            output2.setValue("");

            OutputDataObjectType output3 = new OutputDataObjectType();
            output3.setName("Al_exc3.wfc2");
            output3.setType(DataType.URI);
            output3.setValue("");

            OutputDataObjectType output4 = new OutputDataObjectType();
            output4.setName("Al_exc3.wfc3");
            output4.setType(DataType.URI);
            output4.setValue("");

            OutputDataObjectType output5 = new OutputDataObjectType();
            output5.setName("Al_exc3.wfc4");
            output5.setType(DataType.URI);
            output5.setValue("");

            OutputDataObjectType output6 = new OutputDataObjectType();
            output6.setName("ESPRESSO.oJobID");
            output6.setType(DataType.URI);
            output6.setValue("");


            exOut.add(output);
            exOut.add(output1);
            exOut.add(output2);
            exOut.add(output3);
            exOut.add(output4);
            exOut.add(output5);
            exOut.add(output6);


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

    public static String createExperimentTRINITYStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("RNA_Seq_Left_Input");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/TRINITY_FILES/reads.left.fq");

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("RNA_Seq_Right_Input");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/TRINITY_FILES/reads.right.fq");

            exInputs.add(input);
            exInputs.add(input1);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("Trinity_Execution_Log");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("Trinity_FASTA_File");
            output1.setType(DataType.URI);
            output1.setValue("");

            OutputDataObjectType output2 = new OutputDataObjectType();
            output2.setName("Trinity.oJobID");
            output2.setType(DataType.URI);
            output2.setValue("");


            exOut.add(output);
            exOut.add(output1);
            exOut.add(output2);

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

    public static String createExperimentLAMMPSStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Friction_Simulation_Input");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/LAMMPS_FILES/in.friction");

            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("LAMMPS_Simulation_Log");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("LAMMPS.oJobID");
            output1.setType(DataType.URI);
            output1.setValue("");

            exOut.add(output);
            exOut.add(output1);

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

    public static String createExperimentNWCHEMStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Water_Molecule_Input");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/NWCHEM_FILES/water.nw");

            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("NWChem_Execution_Log");
            output.setType(DataType.URI);
            output.setValue("");

            exOut.add(output);

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
    public static String createExperimentAUTODOCKStampede(Airavata.Client client) throws TException {
        try {
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("AD4_parameters.dat");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/AD4_parameters.dat");

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("hsg1.A.map");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.A.map");

            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("hsg1.C.map");
            input2.setType(DataType.URI);
            input2.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.C.map");

            InputDataObjectType input3 = new InputDataObjectType();
            input3.setName("hsg1.d.map");
            input3.setType(DataType.URI);
            input3.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.d.map");

            InputDataObjectType input4 = new InputDataObjectType();
            input4.setName("hsg1.e.map");
            input4.setType(DataType.URI);
            input4.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.e.map");

            InputDataObjectType input5 = new InputDataObjectType();
            input5.setName("hsg1.HD.map");
            input5.setType(DataType.URI);
            input5.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.HD.map");

            InputDataObjectType input6 = new InputDataObjectType();
            input6.setName("hsg1.maps.fld");
            input6.setType(DataType.URI);
            input6.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.maps.fld");

            InputDataObjectType input7 = new InputDataObjectType();
            input7.setName("hsg1.NA.map");
            input7.setType(DataType.URI);
            input7.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.NA.map");

            InputDataObjectType input8 = new InputDataObjectType();
            input8.setName("hsg1.N.map");
            input8.setType(DataType.URI);
            input8.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.N.map");

            InputDataObjectType input9 = new InputDataObjectType();
            input9.setName("hsg1.OA.map");
            input9.setType(DataType.URI);
            input9.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/hsg1.OA.map");

            InputDataObjectType input10 = new InputDataObjectType();
            input10.setName("ind.dpf");
            input10.setType(DataType.URI);
            input10.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/ind.dpf");

            InputDataObjectType input11 = new InputDataObjectType();
            input11.setName("ind.pdbqt");
            input11.setType(DataType.URI);
            input11.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AUTODOCK_FILES/ind.pdbqt");


            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);
            exInputs.add(input3);
            exInputs.add(input4);
            exInputs.add(input5);
            exInputs.add(input6);
            exInputs.add(input7);
            exInputs.add(input8);
            exInputs.add(input9);
            exInputs.add(input10);
            exInputs.add(input11);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("ind.dlg");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("Autodock.oJobID");
            output1.setType(DataType.URI);
            output1.setValue("");

            exOut.add(output);
            exOut.add(output1);

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
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("WRF_Namelist");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/namelist.input");

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("WRF_Input_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/wrfinput_d01");

            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("WRF_Boundary_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/WRF_FILES/wrfbdy_d01");

            exInputs.add(input);
            exInputs.add(input1);
            exInputs.add(input2);


            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("WRF_Output");
            output.setType(DataType.URI);
            output.setValue("");

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("WRF_Execution_Log");
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
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, "sds128");
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
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Echoed_Output=Hello World");
            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("Echoed_Output");
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
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, "");
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
            String projectId = client.createProject(project);

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
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Input_to_Echo");
            input.setType(DataType.STRING);
            input.setValue("Echoed_Output=Hello World");
            exInputs.add(input);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("Echoed_Output");
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
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 1, 1, 1, "normal", 30, 0, 1, null);
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
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Heat_Restart_File");
            input.setType(DataType.URI);
            input.setValue("/Users/lahirugunathilake/Downloads/02_Heat.rst");
            exInputs.add(input);

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("Production_Control_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/lahirugunathilake/Downloads/03_Prod.in");
            exInputs.add(input1);

            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("Parameter_Topology_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/lahirugunathilake/Downloads/prmtop");
            exInputs.add(input2);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("AMBER_Execution_Summary");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("AMBER_Execution_log");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            OutputDataObjectType output2 = new OutputDataObjectType();
            output2.setName("AMBER_Trajectory_file");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            OutputDataObjectType output3 = new OutputDataObjectType();
            output3.setName("AMBER_Restart_file");
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
            List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
            InputDataObjectType input = new InputDataObjectType();
            input.setName("Heat_Restart_File");
            input.setType(DataType.URI);
            input.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AMBER_FILES/02_Heat.rst");
            exInputs.add(input);

            InputDataObjectType input1 = new InputDataObjectType();
            input1.setName("Production_Control_File");
            input1.setType(DataType.URI);
            input1.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AMBER_FILES/03_Prod.in");
            exInputs.add(input1);

            InputDataObjectType input2 = new InputDataObjectType();
            input2.setName("Parameter_Topology_File");
            input2.setType(DataType.URI);
            input2.setValue("/Users/shameera/Downloads/PHP-Gateway-Scripts/appScripts/AMBER_FILES/prmtop");
            exInputs.add(input2);

            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
            OutputDataObjectType output = new OutputDataObjectType();
            output.setName("AMBER_Execution_Summary");
            output.setType(DataType.URI);
            output.setValue("");
            exOut.add(output);

            OutputDataObjectType output1 = new OutputDataObjectType();
            output1.setName("AMBER_Execution_Summary");
            output1.setType(DataType.URI);
            output1.setValue("");
            exOut.add(output1);
            OutputDataObjectType output2 = new OutputDataObjectType();
            output2.setName("AMBER_Trajectory_file");
            output2.setType(DataType.URI);
            output2.setValue("");
            exOut.add(output2);
            OutputDataObjectType output3 = new OutputDataObjectType();
            output3.setName("AMBER_Restart_file");
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
                        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling(id, 4, 1, 1, "development", 20, 0, 1, "TG-STA110014S");
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
//			List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
//			InputDataObjectType input = new InputDataObjectType();
//			input.setName("Heat_Restart_File");
//			input.setType(DataType.URI);
//			input.setValue("/Users/raminder/Documents/Sample/Amber/02_Heat.rst");
//			exInputs.add(input);
//            InputDataObjectType input1 = new InputDataObjectType();
//            input1.setName("Production_Control_File");
//            input1.setType(DataType.URI);
//            input1.setValue("/Users/raminder/Documents/Sample/Amber/03_Prod.in");
//            exInputs.add(input1);
//
//            InputDataObjectType input2 = new InputDataObjectType();
//            input2.setName("Parameter_Topology_File");
//            input2.setType(DataType.URI);
//            input2.setValue("/Users/raminder/Documents/Sample/Amber/prmtop");
//            exInputs.add(input2);

//            List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
//            OutputDataObjectType output = new OutputDataObjectType();
//            output.setName("AMBER_Execution_Summary");
//            output.setType(DataType.URI);
//            output.setValue("03_Prod.info");
//            exOut.add(output);
//
//            OutputDataObjectType output1 = new OutputDataObjectType();
//            output1.setName("AMBER_Execution_log");
//            output1.setType(DataType.URI);
//            output1.setValue("03_Prod.out");
//            exOut.add(output1);
//            OutputDataObjectType output2 = new OutputDataObjectType();
//            output2.setName("AMBER_Trajectory_file");
//            output2.setType(DataType.URI);
//            output2.setValue("03_Prod.mdcrd");
//            exOut.add(output2);
//            OutputDataObjectType output3 = new OutputDataObjectType();
//            output3.setName("AMBER_Restart_file");
//            output3.setType(DataType.URI);
//            output3.setValue("03_Prod.rst");
//            exOut.add(output3);
//            
//            OutputDataObjectType output4 = new OutputDataObjectType();
//            output4.setName("STDERR");
//            output4.setType(DataType.STDERR);
//            output4.setValue("");
//            exOut.add(output4);
//            
//            OutputDataObjectType output5 = new OutputDataObjectType();
//            output5.setName("STDOUT");
//            output5.setType(DataType.STDOUT);
//            output5.setValue("");
//            exOut.add(output5);
            

            Project project = ProjectModelUtil.createProject("default", "admin", "test project");
            String projectId = client.createProject(project);

            Experiment simpleExperiment =
                    ExperimentModelUtil.createSimpleExperiment(projectId, "admin", "sshEchoExperiment", "SimpleEchoBR", amberAppId, exInputs);
            simpleExperiment.setExperimentOutputs(exOut);
            simpleExperiment.setEnableEmailNotification(true);
            simpleExperiment.addToEmailAddresses("raman@ogce.org");
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
        	String tokenId = "5f116091-0ad3-4ab6-9df7-6ac909f21f8b";
//        	String tokenId ="aaaaaa";
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
