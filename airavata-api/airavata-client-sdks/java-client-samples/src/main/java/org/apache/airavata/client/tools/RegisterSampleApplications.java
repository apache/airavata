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
package org.apache.airavata.client.tools;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.samples.CreateLaunchExperiment;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RegisterSampleApplications {

//    public static final String THRIFT_SERVER_HOST = "gw127.iu.xsede.org";
    public static final String THRIFT_SERVER_HOST = CreateLaunchExperiment.THRIFT_SERVER_HOST;
//    public static final int THRIFT_SERVER_PORT = 9930;
    public static final int THRIFT_SERVER_PORT = CreateLaunchExperiment.THRIFT_SERVER_PORT;
    private final static Logger logger = LoggerFactory.getLogger(RegisterSampleApplications.class);
//    private static final String DEFAULT_GATEWAY = "default";
    private static final String DEFAULT_GATEWAY = CreateLaunchExperiment.DEFAULT_GATEWAY;
    private static Airavata.Client airavataClient;

    //Host Id's
    private static String localhostId = "";
    private static String stampedeResourceId = "stampede.tacc.xsede.org_92ac5ed6-35a5-4910-82ef-48f128f9245a";
    private static String trestlesResourceId = "trestles.sdsc.xsede.org_db29986e-5a27-4949-ae7f-04a6012d0d35";
    private static String bigredResourceId = "bigred2.uits.iu.edu_3eae6e9d-a1a7-44ec-ac85-3796ef726ef1";
    private static String lsfResourceId = "lsf_3eae6e9d-a1a7-44ec-ac85-3796ef726ef1";
    private static String alamoResourceId;


    private static String fsdResourceId;
 // unicore service endpoint url
//    private static final String unicoreEndPointURL = "https://fsd-cloud15.zam.kfa-juelich.de:7000/INTEROP1/services/BESFactory?res=default_bes_factory";
    private static final String unicoreEndPointURL = "https://deisa-unic.fz-juelich.de:9111/FZJ_JUROPA/services/BESFactory?res=default_bes_factory";

    //Appplication Names
    private static final String echoName = "Echo";
    private static final String amberName = "Amber";
    private static final String autoDockName = "AutoDock";
    private static final String espressoName = "ESPRESSO";
    private static final String gromacsName = "GROMACS";
    private static final String lammpsName = "LAMMPS";
    private static final String nwChemName = "NWChem";
    private static final String trinityName = "Trinity";
    private static final String wrfName = "WRF";
    private static final String phastaName = "PHASTA";
    private static final String mpiName = "HelloMPI";
    private static final String monteXName = "TinkerMonte";
    private static final String gaussianName = "Gaussian";
    private static final String gamessName = "Gamess";
    private static final String stressMemName = "StressMem";
    private static final String ultrascanName = "Ultrascan";

    //Appplication Descriptions
    private static final String echoDescription = "A Simple Echo Application";
    private static final String amberDescription = "Assisted Model Building with Energy Refinement MD Package";
    private static final String autoDockDescription = "AutoDock suite of automated docking tools";
    private static final String espressoDescription = "Nanoscale electronic-structure calculations and materials modeling";
    private static final String gromacsDescription = "GROMACS Molecular Dynamics Package";
    private static final String lammpsDescription = "Large-scale Atomic/Molecular Massively Parallel Simulator";
    private static final String nwChemDescription = "Ab initio computational chemistry software package";
    private static final String trinityDescription = "de novo reconstruction of transcriptomes from RNA-seq data";
    private static final String wrfDescription = "Weather Research and Forecasting";
    private static final String phastaDescription = "Computational fluid dynamics solver";
    private static final String mpiDescription = "A Hello MPI Application";
    private static final String monteXDescription = "Grid Chem Tinker Monte Application";
    private static final String gaussianDescription = "Grid Chem Gaussian Application";
    private static final String gamessDescription = "A Gamess Application";
    private static final String ultrascanDescription = "Ultrascan Application";

    //App Module Id's
    private static String echoModuleId;
    private static String amberModuleId;
    private static String autoDockModuleId;
    private static String espressoModuleId = "ESPRESSO_54dc94da-5e2b-4add-b054-41ad88891fdc";
    private static String gromacsModuleId = "GROMACS_417271fd-7ac1-4f40-b2a5-ed0908a743eb";
    private static String lammpsModuleId;
    private static String lammpsModuleId1;
    private static String nwChemModuleId = "NWChem_edbc318d-4c41-46a7-b216-32bad71eabdd";
    private static String trinityModuleId = "Trinity_8af45ca0-b628-4614-9087-c7b73f5f2fb6";
    private static String wrfModuleId;
    private static String phastaModuleId;
    private static String mpiModuleId;
    private static String monteXModuleId;
    private static String gaussianModuleId;
    private static String gamessModuleId;

    //App Interface Id's
    private static String echoInterfaceId = "";
    private static String mpiInterfaceId = "";
    private static String echoLocalInterfaceId = "";
    private static String amberInterfaceId = "";
    private static String autoDockInterfaceId = "";
    private static String espressoInterfaceId = "";
    private static String gromacsInterfaceId = "";
    private static String lammpsInterfaceId = "";
    private static String nwChemInterfaceId = "";
    private static String trinityInterfaceId = "";
    private static String wrfInterfaceId;
    private static String phastaInterfaceId;
    private static String gamessInterfaceId;
    private static String ultrascanModuleId;
    private static String ultrascanInterfaceId;

    public RegisterSampleApplications(Airavata.Client airavataClient) {
           this.airavataClient = airavataClient;
    }

    public static void main(String[] args) {
        try {
            Airavata.Client airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavataClient.getAPIVersion());

            RegisterSampleApplications registerSampleApplications = new RegisterSampleApplications(airavataClient);

            registerLocalHost();
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

            //write output into propertiesFile
//            registerSampleApplications.writeIdPropertyFile();

        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void registerLocalHost() {
        try {
            System.out.println("\n #### Registering Localhost Computational Resource #### \n");

            ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils.
                    createComputeResourceDescription("localhost", "LocalHost", null, null);
            List<String> hostAliases  = new ArrayList<String>();
            hostAliases.add("alias1");
            hostAliases.add("alias2");
            computeResourceDescription.setHostAliases(hostAliases);
            localhostId = airavataClient.registerComputeResource(new AuthzToken(""), computeResourceDescription);
            ResourceJobManager resourceJobManager = RegisterSampleApplicationsUtils.
                    createResourceJobManager(ResourceJobManagerType.FORK, null, null, null);
            LOCALSubmission submission = new LOCALSubmission();
            submission.setResourceJobManager(resourceJobManager);
            airavataClient.addLocalSubmissionDetails(new AuthzToken(""), localhostId, 1, submission);
            System.out.println("LocalHost Resource Id is " + localhostId);

        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public void registerXSEDEHosts() {
        try {
            System.out.println("\n #### Registering XSEDE Computational Resources #### \n");

            //Register Stampede
            List<BatchQueue> stampedeQueues = new ArrayList<BatchQueue>();
            BatchQueue normalQueue = createBatchQueue("normal", "Normal Queue", 2880, 256, 4000, 50, 0);
            BatchQueue developmentQueue = createBatchQueue("development", "Development Queue", 120, 16, 4000, 1, 0);
            stampedeQueues.add(normalQueue);
            stampedeQueues.add(developmentQueue);
            stampedeResourceId = registerComputeHost("stampede.tacc.xsede.org", "TACC Stampede Cluster",
                    ResourceJobManagerType.SLURM, "push", "/usr/bin", SecurityProtocol.SSH_KEYS, 22, "ibrun", stampedeQueues);

            System.out.println("Stampede Resource Id is " + stampedeResourceId);

            //Register Trestles
//            List<BatchQueue> trestlesQueues = new ArrayList<BatchQueue>();
//            BatchQueue normalQueue_tr = createBatchQueue("normal", "Normal Queue", 2880, 32, 32, 50, 0);
//            BatchQueue sharedQueue_tr = createBatchQueue("shared", "Shared Queue", 2880, 4, 32, 50, 0);
//            trestlesQueues.add(normalQueue_tr);
//            trestlesQueues.add(sharedQueue_tr);
//            trestlesResourceId = registerComputeHost("trestles.sdsc.xsede.org", "SDSC Trestles Cluster",
//                    ResourceJobManagerType.PBS, "push", "/opt/torque/bin/", SecurityProtocol.SSH_KEYS, 22, "mpirun -np", trestlesQueues);
//            System.out.println("Trestles Resource Id is " + trestlesResourceId);

            //Register BigRedII
            List<BatchQueue> br2Queues = new ArrayList<BatchQueue>();
            BatchQueue normalQueue_br2 = createBatchQueue("normal", "Normal Queue", 2880, 340, 2048, 50, 0);
            BatchQueue serial_br2 = createBatchQueue("serial", "Normal Queue", 10080, 340, 2048, 50, 0);
            br2Queues.add(normalQueue_br2);
            br2Queues.add(serial_br2);
            bigredResourceId = registerComputeHost("bigred2.uits.iu.edu", "IU BigRed II Cluster",
                    ResourceJobManagerType.PBS, "push", "/opt/torque/torque-4.2.3.1/bin/", SecurityProtocol.SSH_KEYS, 22, "aprun -n", br2Queues);
            System.out.println("BigredII Resource Id is " + bigredResourceId);

            fsdResourceId = registerUnicoreEndpoint("fsd-cloud15.zam.kfa-juelich.de", "interop host", JobSubmissionProtocol.UNICORE, SecurityProtocol.GSI);
            System.out.println("FSd Resource Id: "+fsdResourceId);

            //Register Alamo
            List<BatchQueue> alamoQueues = new ArrayList<BatchQueue>();
            alamoResourceId = registerComputeHost("alamo.uthscsa.edu", "Alamo Cluster",
                    ResourceJobManagerType.PBS, "push", "/usr/bin/", SecurityProtocol.SSH_KEYS, 22, "/usr/bin/mpiexec -np", alamoQueues);
            System.out.println("Alamo Cluster " + alamoResourceId);


        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public void registerNonXSEDEHosts() {
        try {
            System.out.println("\n #### Registering Non-XSEDE Computational Resources #### \n");

            //Register LSF resource
            List<BatchQueue> lsfQueues = new ArrayList<BatchQueue>();
            lsfResourceId = registerComputeHost("ghpcc06.umassrc.org", "LSF Cluster",
                    ResourceJobManagerType.LSF, "push", "source /etc/bashrc;/lsf/9.1/linux2.6-glibc2.3-x86_64/bin", SecurityProtocol.SSH_KEYS, 22, "mpiexec", lsfQueues);
            System.out.println("LSF Resource Id is " + lsfResourceId);

        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public static String registerUnicoreEndpoint(String hostName, String hostDesc, JobSubmissionProtocol protocol, SecurityProtocol securityProtocol) throws TException {

		ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils
				.createComputeResourceDescription(hostName, hostDesc, null, null);

		fsdResourceId = airavataClient.registerComputeResource(new AuthzToken(""), computeResourceDescription);

		if (fsdResourceId.isEmpty())
			throw new AiravataClientException();

		System.out.println("FSD Compute ResourceID: "+fsdResourceId);
		JobSubmissionInterface jobSubmission = RegisterSampleApplicationsUtils.createJobSubmissionInterface(fsdResourceId, protocol, 2);
		UnicoreJobSubmission ucrJobSubmission = new UnicoreJobSubmission();
		ucrJobSubmission.setSecurityProtocol(securityProtocol);
		ucrJobSubmission.setUnicoreEndPointURL(unicoreEndPointURL);
//		ucrJobSubmission.setAuthenticationMode(AuthenticationMode.MYPROXY_ISSUED);
		jobSubmission.setJobSubmissionProtocol(JobSubmissionProtocol.UNICORE);

		airavataClient.addUNICOREJobSubmissionDetails(new AuthzToken(""), fsdResourceId, 0, ucrJobSubmission);

		return jobSubmission.getJobSubmissionInterfaceId();
	}

    public void registerAppModules() {
        try {
            System.out.println("\n #### Registering Application Modules #### \n");

            //Register Echo
            echoModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            echoName, "1.0", echoDescription));
            System.out.println("Echo Module Id " + echoModuleId);

            mpiModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            mpiName, "1.0", mpiDescription));
            System.out.println("MPI Module Id " + mpiModuleId);


            //Register Amber
            amberModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            amberName, "12.0", amberDescription));
            System.out.println("Amber Module Id " + amberModuleId);

            //Register AutoDock
            autoDockModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            autoDockName, "4.2", autoDockDescription));
            System.out.println("AutoDock Module Id " + autoDockModuleId);

            //Register ESPRESSO
            espressoModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            espressoName, "5.0.3", espressoDescription));
            System.out.println("ESPRESSO Module Id " + espressoModuleId);

            //Register GROMACS
            gromacsModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            gromacsName, "4.6.5", gromacsDescription));
            System.out.println("GROMACS Module Id " + gromacsModuleId);

            //Register LAMMPS
            lammpsModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            lammpsName, "20Mar14", lammpsDescription));

            lammpsModuleId1 = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            lammpsName, "28Jun14-base", lammpsDescription));

            System.out.println("LAMMPS Module Id " + lammpsModuleId);

            System.out.println("LAMMPS Module Id for LSF " + lammpsModuleId1);

            //Register NWChem
            nwChemModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            nwChemName, "6.3", nwChemDescription));
            System.out.println("NWChem Module Id " + nwChemModuleId);

            //Register Trinity
            trinityModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            trinityName, "r20130225", trinityDescription));
            System.out.println("Trinity Module Id " + trinityModuleId);

            //Register WRF
            wrfModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            wrfName, "3.5.1", wrfDescription));
            System.out.println("WRF Module Id " + wrfModuleId);

            //Register PHASTA
            phastaModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            phastaName, "1.0", phastaDescription));
            System.out.println("phasta Module Id " + phastaModuleId);

            monteXModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            monteXName, "1.0", monteXDescription));
            System.out.println("Tinker Monte Module Id " + monteXModuleId);

            gaussianModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            gaussianName, "1.0", gaussianDescription));
            //Register GAMESS
            gamessModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            gamessName, "17May13", gamessDescription));
            System.out.println("Gamess Module Id " + gamessModuleId);
            //Register Ultrascan
            ultrascanModuleId = airavataClient.registerApplicationModule(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            ultrascanName, "1.0", ultrascanDescription));
            System.out.println("Ultrascan Module Id " + ultrascanModuleId);

        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public void registerAppDeployments() {
        System.out.println("\n #### Registering Application Deployments #### \n");

        //Registering localhost echo App
        registerLocalApps();
        //Registering Stampede Apps
        registerStampedeApps();

        //Registering Trestles Apps
        registerTrestlesApps();

        //Registering BigRed II Apps
        registerBigRedApps();

        //Registering FSD Apps
        registerFSDApps();

        registerLSFApps();

        //Registering Alamo Apps
        registerAlamoApps();

    }

    public void registerAppInterfaces() {
        System.out.println("\n #### Registering Application Interfaces #### \n");

        //Registering local Echo
        registerLocalEchoInterface();

        //Registering Echo
        registerEchoInterface();

        //Registering MPI
        registerMPIInterface();

        //Registering Amber
        registerAmberInterface();

        //Registering AutoDock
        registerAutoDockInterface();

        //Registering Espresso
        registerEspressoInterface();

        //Registering Gromacs
        registerGromacsInterface();

        //Registering Lammps
        registerLammpsInterface();
        //Registrting Gamess
        registerGamessInterface();

        //Registering NWChem
        registerNWChemInterface();

        //Registering Trinity
        registerTrinityInterface();

        //Registering WRF
        registerWRFInterface();

        registerTinkerMonteInterface();

        registerGaussianInterface();

        // Register Ultrascan
        registerUltrascanInterface();

    }

    public void registerGamessInterface() {
        try {
            System.out.println("#### Registering Gamess Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(gamessModuleId);


            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(RegisterSampleApplicationsUtils.createAppInput("gams_input", "",
                    DataType.URI, null, 1, true, false, false, "Gamess Input file", null));
//            applicationInputs.add(RegisterSampleApplicationsUtils.createAppInput("EXT_FILE", "",
//                    DataType.URI, null, 2, ValidityType.OPTIONAL, CommandLineType.EXCLUSIVE, false, "Gamess EXT file", null));

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("gams_output",
                    "", DataType.URI, true, false, null));
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("dat_file",
                    "", DataType.URI, true, true, null));
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("trj_file",
                    "", DataType.URI, false, true, null));
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("rst_file",
                    "", DataType.URI, false, true, null));
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("f10_file",
                    "", DataType.URI, false, true, null));
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("STDOUT",
                    "", DataType.STDOUT, false, true, null));
            applicationOutputs.add(RegisterSampleApplicationsUtils.createAppOutput("STDERR",
                    "", DataType.STDERR, false, true, null));

            gamessInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("gamess", "gamess",
                            appModules, applicationInputs, applicationOutputs));

            System.out.println("GAMESS Application Interface Id " + gamessModuleId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerLocalEchoInterface() {
        try {
            System.out.println("#### Registering Echo Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("echo_input", "echo_output=Hello World",
                    DataType.STRING, null, 1,false, false, false, "A test string to Echo", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",
                    "", DataType.STDOUT, false, false, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",
                    "", DataType.STDERR, false, false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);

            echoLocalInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(echoName, echoDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Local Echo Application Interface Id " + echoLocalInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerEchoInterface() {
        try {
            System.out.println("#### Registering Echo Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);


            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo", "",
                    DataType.STRING, null, 1, false, true,false, "A test string to Echo", null);

//            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo2", "",
//                    DataType.URI, null, 2, false, false,false, "A sample input remote file", null);
//
//            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo3", "file:///tmp/test.txt",
//                    DataType.URI, null, 3,false, false, false, "A sample input local file", null);


            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
//            applicationInputs.add(input2); applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STDOUT, true, false, null);

            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Error",
                    "", DataType.STDERR, true, false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);


            echoInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(echoName, echoDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Echo Application Interface Id " + echoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public void registerMPIInterface() {
        try {
            System.out.println("#### Registering MPI Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(mpiModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Sample_Input", "",
                    DataType.STRING, null, 1,true, false, false, "An optional MPI source file", null);

            InputDataObjectType input11 = RegisterSampleApplicationsUtils.createAppInput("US3INPUT", "",
                    DataType.URI, null, 1,true, false, false, "Input US3 file", null);

            InputDataObjectType input12 = RegisterSampleApplicationsUtils.createAppInput("US3INPUTARG", "",
                    DataType.STRING, null, 1,true, false, false, "Input US3 Arg", null);


            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("NumberOfProcesses", "",
                    DataType.INTEGER, null, 2,false, true, false, "Number Of Processes", null);


            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("ProcessesPerHost", "",
                    DataType.INTEGER, null, 3,false, true, false, "Processes per host", null);


            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);
            applicationInputs.add(input11);
            applicationInputs.add(input12);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("STDOutput",
                    "", DataType.STDOUT, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDErr",
                    "", DataType.STDERR, true, true, null);

            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("US3OUT",
                    "", DataType.STRING, true, false, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);


            mpiInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(mpiName, mpiDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("MPI Application Interface Id " + mpiInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public void registerAmberInterface() {
        try {
            System.out.println("#### Registering Amber Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(amberModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Heat_Restart_File", null,
                    DataType.URI, "-c", 1, true, true, false, "Heating up the system equilibration stage - 02_Heat.rst", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("Production_Control_File", null,
                    DataType.URI, "-i ", 2, true, true, false, "Constant pressure and temperature for production stage - 03_Prod.in", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("Parameter_Topology_File", null,
                    DataType.URI, "-p", 3, true, true, false, "Parameter and Topology coordinates - prmtop", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("AMBER_Execution_Summary", "03_Prod.info", DataType.URI, true, true, "-inf");
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("AMBER_Execution_log", "03_Prod.out", DataType.URI, true, true, "-o");
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("AMBER_Trajectory_file", "03_Prod.mdcrd", DataType.URI, true, true, "-x");
            OutputDataObjectType output4 = RegisterSampleApplicationsUtils.createAppOutput("AMBER_Restart_file", "03_Prod.rst", DataType.URI, true, true, " -r");
            OutputDataObjectType output5 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT", null, DataType.STDOUT, true, false, null);
            OutputDataObjectType output6 = RegisterSampleApplicationsUtils.createAppOutput("STDERR", null, DataType.STDERR, true, false, null);
            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);
            applicationOutputs.add(output4);
            applicationOutputs.add(output5);
            applicationOutputs.add(output6);

            amberInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(amberName, amberDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Amber Application Interface Id " + amberInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerAutoDockInterface() {
        try {
            System.out.println("#### Registering AutoDock Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(autoDockModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Heat_Restart_File", null,
                    DataType.URI, null, 1,true, true, false, "Heating up the system equilibration stage", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("Production_Control_File", null,
                    DataType.URI, null, 2,true, true, false, "Constant pressure and temperature for production stage", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("Parameter_Topology_File", null,
                    DataType.URI, null, 3,true, true, false, "Parameter and Topology coordinates", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);

            autoDockInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(autoDockName, autoDockDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("AutoDock Application Interface Id " + autoDockInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerEspressoInterface() {
        try {
            System.out.println("#### Registering Espresso Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(espressoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("AI_Primitive_Cell", null,
                    DataType.URI, null, 1, true, true,false, "AI_Metal_Input_File - Al.sample.in", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("AI_Pseudopotential_File", null,
                    DataType.URI, null, 2, true, true,false, "Constant pressure and temperature for production stage - Al.pz-vbc.UPF", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("ESPRESSO_Execution_Log",null,DataType.URI, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("ESPRESSO_WFC_Binary_file",null,DataType.URI, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output4 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);
            applicationOutputs.add(output4);

            espressoInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(espressoName, espressoDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Espresso Application Interface Id " + espressoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerGromacsInterface() {
        try {
            System.out.println("#### Registering Gromacs Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(gromacsModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Portable_Input_Binary_File", null,
                    DataType.URI, null, 1, true, true, false, "Coordinates velocities, molecular topology and simulation parameters - pdb1y6l-EM-vacuum.tpr", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("GROMOS_Coordinate_File", null,
                    DataType.URI, null, 2, true, true,false, "Trajectory Coordinates Molecular Structure in Gromos87 format - pdb1y6l-EM-vacuum.gro", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("GROMACS_Execution_Log",null,DataType.URI, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("Full_Precision_Trajectory_file",null,DataType.URI, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("Portable_Energy_file",null,DataType.URI, true, true, null);
            OutputDataObjectType output4 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output5 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);
            applicationOutputs.add(output4);
            applicationOutputs.add(output5);

            gromacsInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(gromacsName, gromacsDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Gromacs Application Interface Id " + gromacsInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerLammpsInterface() {
        try {
            System.out.println("#### Registering LAMMPS Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(lammpsModuleId);
            appModules.add(lammpsModuleId1);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Friction_Simulation_Input", null,
                    DataType.URI, "<", 1,true, true, false, "Friction Simulation Input - in.friction", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("LAMMPS_Simulation_Log",null,DataType.URI, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);

            lammpsInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(lammpsName, lammpsDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("LAMMPS Application Interface Id " + lammpsInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerNWChemInterface() {
        try {
            System.out.println("#### Registering NWChem Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(nwChemModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Water_Molecule_Input", null,
                    DataType.URI, null, 1,true , true, false, "Water Molecule Input File - water.nw", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("NWChem_Execution_Log",
                    null, DataType.URI, true, false, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);

            nwChemInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(nwChemName, nwChemDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("NWChem Application Interface Id " + nwChemInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public void registerTrinityInterface() {
        try {
            System.out.println("#### Registering Trinity Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(trinityModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("RNA_Seq_Left_Input", null,
                    DataType.URI, null, 1,true, true, false, "RNA-Seq Left Library - reads.left.fq", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("RNA_Seq_Right_Input", null,
                    DataType.URI, null, 2, true, true,false, "RNA-Seq Right Library - reads.right.fq", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Trinity_Execution_Log",null,DataType.URI, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("Trinity_FASTA_File",null,DataType.URI, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output4 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);
            applicationOutputs.add(output4);

            trinityInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(trinityName, trinityDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Trinity Application Interface Id " + trinityInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public void registerWRFInterface() {
        try {
            System.out.println("#### Registering WRF Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(wrfModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Config_Namelist_File", null,
                    DataType.URI, null, 1,true, true, false, "Namelist Configuration File - namelist.input", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("WRF_Initial_Conditions", null,
                    DataType.URI, null, 2, true, true,false, "Initial Conditions File - wrfinput_d01", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("WRF_Boundary_File", null,
                    DataType.URI, null, 3,true, true, false, "Boundary Conditions File - wrfbdy_d01", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("WRF_Output",
                    "", DataType.URI, true, true, null);

            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("WRF_Execution_Log",
                    "", DataType.URI, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output4 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);
            applicationOutputs.add(output4);

            wrfInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(wrfName, wrfDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("WRF Application Interface Id " + wrfInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerPhastaInterface() {
        try {
            System.out.println("#### Registering PHASTA Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(phastaModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Parasolid_Geometric_Model", null,
                    DataType.URI, null, 1, true, true,false, "Parasolid geometric model - geom.xmt_txt", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("Problem_Definition", null,
                    DataType.URI, null, 2,true, true, false, "problem definition - geom.smd", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("Mesh_Description_File", null,
                    DataType.URI, null, 3, true, true,false, "Mesh Description - geom.sms", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("PHASTA_Execution_Log",null,DataType.URI, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("PHASTA_Output_tar",null,DataType.URI, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output4 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);
            applicationOutputs.add(output4);


            phastaInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(phastaName, phastaDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("phasta Application Interface Id " + phastaInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerLocalApps (){
        try {
            System.out.println("#### Registering Application Deployments on Localhost #### \n");
            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, localhostId,
                            "/bin/echo", ApplicationParallelismType.SERIAL, echoDescription, null, null, null));


            System.out.println("Echo on localhost Id " + echoAppDeployId);
        }catch (TException e) {
            e.printStackTrace();
        }
    }

    private void registerGaussianInterface() {
        try {
            System.out.println("#### Registering Gaussian Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(gaussianModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("MainInputFile", null,
                    DataType.URI, null, 1, true, true,  false, "Gaussian main input file", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("gaussian.out",
                    "", DataType.URI, true, true, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
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
                    DataType.URI, null, 1, true, true, false, "Tinker monte input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("keyf", "O16.key",
                    DataType.URI, "-k", 2, true, true, false, "Tinker monte input_2", null);
            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("stps", "20000",
                    DataType.STRING, null, 3, true, true, false, "Tinker monte input_3", null);
            InputDataObjectType input4 = RegisterSampleApplicationsUtils.createAppInput("Ctc", "C",
                    DataType.STRING, null, 4, true, true, false, "Tinker monte input_4", null);
            InputDataObjectType input5 = RegisterSampleApplicationsUtils.createAppInput("stpsZ", "3.0",
                    DataType.STRING, null, 5, true, true, false, "Tinker monte input_5", null);
            InputDataObjectType input6 = RegisterSampleApplicationsUtils.createAppInput("temp", "298",
                    DataType.STRING, null, 6, true, true, false, "Tinker monte input_6", null);
            InputDataObjectType input7 = RegisterSampleApplicationsUtils.createAppInput("Rconv", "0.01",
                    DataType.STRING, null, 7, true, true,  false, "Tinker monte input_7", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);
            applicationInputs.add(input4);
            applicationInputs.add(input5);
            applicationInputs.add(input6);
            applicationInputs.add(input7);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Diskoutputfile_with_dir",
                    "", DataType.URI, false, false, null);
            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",null,DataType.STDOUT, true, true, null);
            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",null,DataType.STDERR, true, true, null);


            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Tinker_Monte", "Monte application",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Monte Application Interface Id " + addApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public void registerStampedeApps() {
        try {
            System.out.println("#### Registering Application Deployments on Stampede #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                            echoDescription, null, null,null));
            System.out.println("Echo on stampede deployment Id " + echoAppDeployId);

            //Register Amber
            List<CommandObject> moduleLoadCMDs = new ArrayList<>();
            CommandObject cmd = new CommandObject("module load amber");
            moduleLoadCMDs.add(cmd);
            ApplicationDeploymentDescription amberStampedeDeployment = RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, stampedeResourceId,
                    "/opt/apps/intel13/mvapich2_1_9/amber/12.0/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                    amberDescription, moduleLoadCMDs, null, null);
            String amberAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,amberStampedeDeployment);
            System.out.println("Amber on stampede deployment Id " + amberAppDeployId);

            //Register ESPRESSO
            String espressoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(espressoModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/espresso_wrapper.sh", ApplicationParallelismType.MPI,
                            espressoDescription, null, null, null));
            System.out.println("ESPRESSO on stampede deployment Id " + espressoAppDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            gromacsDescription, null, null ,null));
            System.out.println("GROMACS on stampede deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            List<CommandObject> preJobCommands = new ArrayList();
            preJobCommands.add(new CommandObject("cp /home1/00421/ccguser/apps/ds_lammps/data/* $workingDir"));
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, stampedeResourceId,
                            "~/apps/ds_lammps/bin/lmp_stampede", ApplicationParallelismType.MPI,
                            lammpsDescription,null, preJobCommands, null));
            System.out.println("LAMMPS on stampede deployment Id " + lammpsAppDeployId);

            //Register NWChem
            List<CommandObject> nwChemModuleCmds = new ArrayList();
            nwChemModuleCmds.add(new CommandObject("module load nwchem"));
            String nwChemAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(nwChemModuleId, stampedeResourceId,
                            "nwchem", ApplicationParallelismType.MPI,
                            nwChemDescription, nwChemModuleCmds, null, null));
            System.out.println("NWChem on stampede deployment Id " + nwChemAppDeployId);

            //Register Trinity
            String trinityAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(trinityModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/trinity_wrapper.sh", ApplicationParallelismType.MPI,
                            trinityDescription, null, null, null));
            System.out.println("Trinity on stampede deployment Id " + trinityAppDeployId);

            //Register WRF
            String wrfAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(wrfModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/wrf_wrapper.sh", ApplicationParallelismType.MPI,
                            wrfDescription, null, null , null));
            System.out.println("WRF on stampede deployment Id " + wrfAppDeployId);


            List<CommandObject> monteXModuleCmds = new ArrayList();
//            monteXModuleCmds.add("module load globus");
//            monteXModuleCmds.add("module load uberftp");
            monteXModuleCmds.add(new CommandObject("module load fftw3"));

            String monteXAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(monteXModuleId, stampedeResourceId,
                            "/home1/00421/ccguser/apps/tinker/tinker/bin/monte.x", ApplicationParallelismType.OPENMP,
                            monteXDescription, monteXModuleCmds, null, null));
            System.out.println("Tinker Monte on trestles deployment Id " + monteXAppDeployId);
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerTrestlesApps() {
        try {
            System.out.println("#### Registering Application Deployments on Trestles #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                            echoDescription, null, null, null));
            System.out.println("Echo on trestles deployment Id " + echoAppDeployId);

            //Register Amber
            List<CommandObject> moduleLoadCMDs = new ArrayList();
            moduleLoadCMDs.add(new CommandObject("module load amber"));
            String amberAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, trestlesResourceId,
                            "/opt/amber/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                            amberDescription, moduleLoadCMDs, null, null));
            System.out.println("Amber on trestles deployment Id " + amberAppDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            gromacsDescription, null, null, null));
            System.out.println("GROMACS on trestles deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            lammpsDescription, null, null , null));
            System.out.println("LAMMPS on trestles deployment Id " + lammpsAppDeployId);

            //Register GAMESS
            List<CommandObject> moduleLoadCmd = new ArrayList();
            moduleLoadCmd.add(new CommandObject("module load gamess"));
            String gamessAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gamessModuleId, trestlesResourceId,
                            "/opt/gamess/rungms", ApplicationParallelismType.MPI,
                            gamessDescription, moduleLoadCmd, null,null));
            System.out.println("Gamess on trestles deployment Id " + gamessAppDeployId);


            List<CommandObject> gaussianMouldes = new ArrayList();
            gaussianMouldes.add(new CommandObject("module load gaussian"));
            String gaussianAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gaussianModuleId, trestlesResourceId,
                            "g09", ApplicationParallelismType.OPENMP, gaussianDescription, gaussianMouldes , null, null));
            System.out.println("Gaussian on trestles deployment Id " + gaussianAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerLSFApps() {
        try {
            System.out.println("#### Registering Application Deployments on Trestles #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, lsfResourceId,
                            "/home/lg11w/executables/echo.sh", ApplicationParallelismType.SERIAL,
                            echoDescription, null, null, null));

            List<CommandObject> moduleLoadCmd = new ArrayList();
            moduleLoadCmd.add(new CommandObject("module load LAMMPS/28Jun14-base"));
            //Register Echo

            String lammpsDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, lsfResourceId,
                            "lmp_ghpcc", ApplicationParallelismType.MPI,
                            echoDescription, moduleLoadCmd, null, null));
            System.out.println("Echo on LSF deployment Id " + echoAppDeployId);
            System.out.println("LAMMPS on LSF deployment Id " + lammpsDeployId);
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerBigRedApps() {
        try {
            System.out.println("#### Registering Application Deployments on BigRed II #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/echo_wrapper.sh",
                            ApplicationParallelismType.SERIAL, echoDescription, null, null, null));
            System.out.println("Echo on bigredII deployment Id " + echoAppDeployId);

            //Register Amber
            List<CommandObject> amberModuleLoadCMDsBr2 = new ArrayList();
            amberModuleLoadCMDsBr2.add(new CommandObject("module load amber/gnu/mpi/12"));
            amberModuleLoadCMDsBr2.add(new CommandObject("module swap PrgEnv-cray PrgEnv-gnu"));
            String amberAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, bigredResourceId,
                            "/N/soft/cle4/amber/gnu/mpi/12/amber12/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                            amberDescription, amberModuleLoadCMDsBr2, null, null));

            System.out.println("Amber on bigredII deployment Id " + amberAppDeployId);

            //Register AutoDock
            String autoDockDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(autoDockModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/auto_dock_wrapper.sh", ApplicationParallelismType.MPI,
                            autoDockDescription, null, null, null));
            System.out.println("AutoDock on bigredII deployment Id " + autoDockDeployId);

//            //Register GROMACS
//            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
//                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, bigredResourceId,
//                            "/N/u/cgateway/BigRed2/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
//                            gromacsDescription));
//            System.out.println("GROMACS on bigredII deployment Id " + gromacsAppDeployId);
//
//            //Register LAMMPS
//            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
//                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, bigredResourceId,
//                            "/N/u/cgateway/BigRed2/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
//                            lammpsDescription));
//            System.out.println("LAMMPS on bigredII deployment Id " + lammpsAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerFSDApps() {
        try {
            System.out.println("#### Registering Application Deployments on FSD #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, fsdResourceId,
                            "/bin/echo", ApplicationParallelismType.SERIAL, echoDescription, null, null, null));
            System.out.println("Echo on FSD deployment Id: " + echoAppDeployId);

            //Register MPI
//            String mpiAppDeployId = airavataClient.registerApplicationDeployment(DEFAULT_GATEWAY,
//                    RegisterSampleApplicationsUtils.createApplicationDeployment(mpiModuleId, fsdResourceId,
//                            "/home/bes/hellompi", ApplicationParallelismType.OPENMP_MPI, mpiDescription, null, null, null));


            String mpiAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(mpiModuleId, fsdResourceId,
                            "us_mpi_analysis", ApplicationParallelismType.MPI, mpiDescription, null, null, null));


            System.out.println("MPI on FSD deployment Id: " + mpiAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public BatchQueue createBatchQueue (String queuename,
                                        String queueDesc,
                                        int maxRunTime,
                                        int maxNodes,
                                        int maxProcessors,
                                        int maxJobsInQueue,
                                        int maxMemory){
        BatchQueue batchQueue = new BatchQueue();
        batchQueue.setQueueName(queuename);
        batchQueue.setQueueDescription(queueDesc);
        batchQueue.setMaxMemory(maxMemory);
        batchQueue.setMaxJobsInQueue(maxJobsInQueue);
        batchQueue.setMaxNodes(maxNodes);
        batchQueue.setMaxRunTime(maxRunTime);
        batchQueue.setMaxProcessors(maxProcessors);
        return batchQueue;
    }

    public String registerComputeHost(String hostName, String hostDesc,
                                             ResourceJobManagerType resourceJobManagerType,
                                             String monitoringEndPoint, String jobMangerBinPath,
                                             SecurityProtocol securityProtocol,
                                             int portNumber,
                                             String jobManagerCommand,
                                             List<BatchQueue> batchQueues) throws TException {

        ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils.
                createComputeResourceDescription(hostName, hostDesc, null, null);
        computeResourceDescription.setBatchQueues(batchQueues);
        String computeResourceId = airavataClient.registerComputeResource(new AuthzToken(""), computeResourceDescription);

        if (computeResourceId.isEmpty()) throw new AiravataClientException();

        ResourceJobManager resourceJobManager = RegisterSampleApplicationsUtils.
                createResourceJobManager(resourceJobManagerType, monitoringEndPoint, jobMangerBinPath, null);

	    Map<JobManagerCommand, String> jobManagerCommandStringMap = new HashMap<JobManagerCommand, String>();
	    if (resourceJobManagerType == ResourceJobManagerType.SLURM) {
		    jobManagerCommandStringMap.put(JobManagerCommand.SUBMISSION, "sbatch");
		    jobManagerCommandStringMap.put(JobManagerCommand.JOB_MONITORING, "squeue");
		    jobManagerCommandStringMap.put(JobManagerCommand.DELETION, "scancel");
		    resourceJobManager.setJobManagerCommands(jobManagerCommandStringMap);
	    } else if (resourceJobManagerType == ResourceJobManagerType.PBS) {
		    jobManagerCommandStringMap.put(JobManagerCommand.SUBMISSION, "qsub");
		    jobManagerCommandStringMap.put(JobManagerCommand.JOB_MONITORING, "qstat");
		    jobManagerCommandStringMap.put(JobManagerCommand.DELETION, "qdel");
		    resourceJobManager.setJobManagerCommands(jobManagerCommandStringMap);
	    }
	    // TODO - set job manage commands for UGE and LSF type compute resources.

	    // TODO - set parallelism command

        SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
        sshJobSubmission.setResourceJobManager(resourceJobManager);
        sshJobSubmission.setSecurityProtocol(securityProtocol);
        sshJobSubmission.setSshPort(portNumber);
        sshJobSubmission.setMonitorMode(MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR);
        airavataClient.addSSHJobSubmissionDetails(new AuthzToken(""), computeResourceId, 1, sshJobSubmission);

        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setSecurityProtocol(securityProtocol);
        scpDataMovement.setSshPort(portNumber);
        airavataClient.addSCPDataMovementDetails(new AuthzToken(""), computeResourceId, DMType.COMPUTE_RESOURCE, 1, scpDataMovement);

        return computeResourceId;
    }





    public void registerGatewayResourceProfile() {

        try {
            System.out.println("#### Registering Application Deployments on BigRed II #### \n");


            ComputeResourcePreference stampedeResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(stampedeResourceId, "TG-STA110014S", false, null,
                            JobSubmissionProtocol.SSH, DataMovementProtocol.SCP, "/scratch/01437/ogce/gta-work-dirs");

            ComputeResourcePreference trestlesResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(trestlesResourceId, "sds128", false, null, JobSubmissionProtocol.SSH,
                            DataMovementProtocol.SCP, "/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs");

            ComputeResourcePreference bigRedResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(bigredResourceId, "TG-STA110014S", false, null, null, null,
                            "/N/dc2/scratch/cgateway/gta-work-dirs");

            ComputeResourcePreference lsfResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(lsfResourceId, "airavata", false, null, null, null,
                            "/home/lg11w/mywork");

            ComputeResourcePreference fsdResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(fsdResourceId, null, false, null, JobSubmissionProtocol.UNICORE, DataMovementProtocol.UNICORE_STORAGE_SERVICE,null);

            ComputeResourcePreference alamoResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(alamoResourceId, null, false, null, JobSubmissionProtocol.SSH, DataMovementProtocol.SCP,"/home/us3/work/uslims3_cauma3-03896");

            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setGatewayID(DEFAULT_GATEWAY);
            gatewayResourceProfile.addToComputeResourcePreferences(stampedeResourcePreferences);
            gatewayResourceProfile.addToComputeResourcePreferences(trestlesResourcePreferences);
            gatewayResourceProfile.addToComputeResourcePreferences(bigRedResourcePreferences);
            gatewayResourceProfile.addToComputeResourcePreferences(fsdResourcePreferences);
            gatewayResourceProfile.addToComputeResourcePreferences(lsfResourcePreferences);

            String gatewayProfile = airavataClient.registerGatewayResourceProfile(new AuthzToken(""), gatewayResourceProfile);
            System.out.println("Gateway Profile is registered with Id " + gatewayProfile);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void writeIdPropertyFile() {
        try {
            Properties properties = new Properties();
            properties.setProperty("stampedeResourceId", stampedeResourceId);
            properties.setProperty("trestlesResourceId", trestlesResourceId);
            properties.setProperty("bigredResourceId", bigredResourceId);
            properties.setProperty("lsfResourceId", lsfResourceId);

            properties.setProperty("echoInterfaceId", echoInterfaceId);
            properties.setProperty("amberInterfaceId", amberInterfaceId);
            properties.setProperty("autoDockInterfaceId", autoDockInterfaceId);
            properties.setProperty("espressoInterfaceId", espressoInterfaceId);
            properties.setProperty("gromacsInterfaceId", gromacsInterfaceId);
            properties.setProperty("lammpsInterfaceId", lammpsInterfaceId);
            properties.setProperty("nwChemInterfaceId", nwChemInterfaceId);
            properties.setProperty("trinityInterfaceId", trinityInterfaceId);
            properties.setProperty("wrfInterfaceId", wrfInterfaceId);

            File file = new File("airavata-api/airavata-client-sdks/airavata-php-sdk/src/main/resources/conf/app-catalog-identifiers.ini");
            FileOutputStream fileOut = new FileOutputStream(file);
            properties.store(fileOut, "Apache Airavata Gateway to Airavata Deployment Identifiers");
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void registerAlamoApps() {
        try {
            System.out.println("#### Registering Application Deployments on Alamo #### \n");

            //Register Ultrascan on Alamo
            List<CommandObject> ultrascanMouldes = new ArrayList();
            ultrascanMouldes.add(new CommandObject("module load intel/2015/64"));
            ultrascanMouldes.add(new CommandObject("module load openmpi/intel/1.8.4"));
            ultrascanMouldes.add(new CommandObject("module load qt4/4.8.6"));
            ultrascanMouldes.add(new CommandObject("module load ultrascan3/3.3"));

            String ultrascanAppDeployId = airavataClient.registerApplicationDeployment(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationDeployment(ultrascanModuleId, alamoResourceId,
                            "/home/us3/bin/us_mpi_analysis", ApplicationParallelismType.OPENMP, ultrascanDescription, ultrascanMouldes , null, null));
            System.out.println("Ultrascan on alamo deployment Id " + ultrascanAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

     private void registerUltrascanInterface() {
        try {
            System.out.println("#### Registering Ultrascan Application Interface ####");

            List<String> appModules = new ArrayList<String>();
            appModules.add(ultrascanModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("input", null,
                    DataType.URI, null, 1, true, true,  false, "input file", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("output/analysis-results.tar",
                    "", DataType.URI, true, true, null );

            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("STDOUT",
                    "", DataType.STDOUT, true, true, null );

            OutputDataObjectType output3 = RegisterSampleApplicationsUtils.createAppOutput("STDERR",
                    "", DataType.STDERR, true, true, null );

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);
            applicationOutputs.add(output3);

            String ultrascanInterfaceId = airavataClient.registerApplicationInterface(new AuthzToken(""), DEFAULT_GATEWAY,
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Ultrascan", "Ultrascan application",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Ultrascan Application Interface Id " + ultrascanInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }
}

