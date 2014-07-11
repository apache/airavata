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
package org.apache.airavata.client.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;

public class RegisterSampleApplications {

    public static final String THRIFT_SERVER_HOST = "gw111.iu.xsede.org";
//    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 9930;
//    public static final int THRIFT_SERVER_PORT = 8930;
    private final static Logger logger = LoggerFactory.getLogger(RegisterSampleApplications.class);
    private static final String DEFAULT_GATEWAY = "default";
    private static Airavata.Client airavataClient;

    //Host Id's
    private static String stampedeResourceId = "stampede.tacc.xsede.org_cd0aed54-7d66-4d90-94cc-dd69ea86351f";
    private static String trestlesResourceId = "trestles.sdsc.xsede.org_a94e6620-e3b3-47bf-ba4b-e1ac06f9abe6";
    private static String bigredResourceId = "bigred2.uits.iu.edu_9e54dc94-fad6-4bc3-ba14-b3b874457933";

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

    //App Module Id's
    private static String echoModuleId;
    private static String amberModuleId;
    private static String autoDockModuleId;
    private static String espressoModuleId;
    private static String gromacsModuleId;
    private static String lammpsModuleId;
    private static String nwChemModuleId;
    private static String trinityModuleId;
    private static String wrfModuleId;

    //App Interface Id's
    private static String echoInterfaceId;
    private static String amberInterfaceId;
    private static String autoDockInterfaceId;
    private static String espressoInterfaceId;
    private static String gromacsInterfaceId;
    private static String lammpsInterfaceId;
    private static String nwChemInterfaceId;
    private static String trinityInterfaceId;
    private static String wrfInterfaceId;

    public static void main(String[] args) {
        try {
            airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavataClient.getAPIVersion());

            //Register all compute hosts
//            registerXSEDEHosts();

            //Register Gateway Resource Preferences
            registerGatewayResourceProfile();
//
//            //Register all application modules
//            registerAppModules();
//
//            //Register all application deployments
//            registerAppDeployments();
//
//            //Register all application interfaces
//            registerAppInterfaces();

        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void registerXSEDEHosts() {
        try {
            System.out.println("\n #### Registering XSEDE Computational Resources #### \n");

            //Register Stampede
            stampedeResourceId = registerComputeHost("stampede.tacc.xsede.org", "TACC Stampede Cluster",
                    ResourceJobManagerType.SLURM, "push", "/usr/bin", SecurityProtocol.GSI, 2222);
            System.out.println("Stampede Resource Id is " + stampedeResourceId);

            //Register Trestles
            trestlesResourceId = registerComputeHost("trestles.sdsc.xsede.org", "SDSC Trestles Cluster",
                    ResourceJobManagerType.PBS, "push", "/usr/bin", SecurityProtocol.GSI, 22);
            System.out.println("Trestles Resource Id is " + trestlesResourceId);

            //Register BigRedII
            bigredResourceId = registerComputeHost("bigred2.uits.iu.edu", "IU BigRed II Cluster",
                    ResourceJobManagerType.PBS, "push", "/opt/torque/torque-4.2.3.1/bin/", SecurityProtocol.SSH_KEYS, 22);
            System.out.println("BigredII Resource Id is " + bigredResourceId);

        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public static void registerAppModules() {
        try {
            System.out.println("\n #### Registering Application Modules #### \n");

            //Register Echo
            echoModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            echoName, "1.0", echoDescription));
            System.out.println("Echo Module Id " + echoModuleId);

            //Register Amber
            amberModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            amberName, "12.0", amberDescription));
            System.out.println("Amber Module Id " + amberModuleId);

            //Register AutoDock
            autoDockModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            autoDockName, "4.2", autoDockDescription));
            System.out.println("AutoDock Module Id " + autoDockModuleId);

            //Register ESPRESSO
            espressoModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            espressoName, "5.0.3", espressoDescription));
            System.out.println("ESPRESSO Module Id " + espressoModuleId);

            //Register GROMACS
            gromacsModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            gromacsName, "4.6.5", gromacsDescription));
            System.out.println("GROMACS Module Id " + gromacsModuleId);

            //Register LAMMPS
            lammpsModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            lammpsName, "20Mar14", lammpsDescription));
            System.out.println("LAMMPS Module Id " + lammpsModuleId);

            //Register NWChem
            nwChemModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            nwChemName, "6.3", nwChemDescription));
            System.out.println("NWChem Module Id " + nwChemModuleId);

            //Register Trinity
            trinityModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            trinityName, "r20130225", trinityDescription));
            System.out.println("Trinity Module Id " + trinityModuleId);

            //Register WRF
            wrfModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            wrfName, "3.5.1", wrfDescription));
            System.out.println("WRF Module Id " + wrfModuleId);

        } catch (TException e) {
            e.printStackTrace();
        }

    }

    public static void registerAppDeployments() {
        System.out.println("\n #### Registering Application Deployments #### \n");

        //Registering Stampede Apps
        registerStampedeApps();

        //Registering Trestles Apps
        registerTrestlesApps();

        //Registering BigRed II Apps
        registerBigRedApps();
    }

    public static void registerAppInterfaces() {
        System.out.println("\n #### Registering Application Interfaces #### \n");

        //Registering Echo
        registerEchoInterface();

        //Registering Amber
        registerAmberInterface();

        //Registering AutoDock
//        registerAutoDockInterface();

        //Registering Espresso
        registerEspressoInterface();

        //Registering Gromacs
        registerGromacsInterface();

        //Registering Lammps
        registerLammpsInterface();

        //Registering NWChem
        registerNWChemInterface();

        //Registering Trinity
        registerTrinityInterface();

        //Registering WRF
        registerWRFInterface();

    }

    public static void registerEchoInterface() {
        try {
            System.out.println("#### Registering Echo Interface #### \n");

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

            echoInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(echoName, echoDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Echo Application Interface Id " + echoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerAmberInterface() {
        try {
            System.out.println("#### Registering Amber Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Heat_Restart_File", null,
                    DataType.URI, null, false, "Heating up the system equilibration stage", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("Production_Control_File", null,
                    DataType.URI, null, false, "Constant pressure and temperature for production stage", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("Parameter_Topology_File", null,
                    DataType.URI, null, false, "Parameter and Topology coordinates", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            amberInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(amberName, amberDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Amber Application Interface Id " + amberInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerAutoDockInterface() {
        try {
            System.out.println("#### Registering AutoDock Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Heat_Restart_File", null,
                    DataType.URI, null, false, "Heating up the system equilibration stage", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("Production_Control_File", null,
                    DataType.URI, null, false, "Constant pressure and temperature for production stage", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("Parameter_Topology_File", null,
                    DataType.URI, null, false, "Parameter and Topology coordinates", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            autoDockInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(autoDockName, autoDockDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("AutoDock Application Interface Id " + autoDockInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerEspressoInterface() {
        try {
            System.out.println("#### Registering Espresso Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("AI_Primitive_Cell", null,
                    DataType.URI, null, false, "AI_Metal_Input_File", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("AI_Pseudopotential_File", null,
                    DataType.URI, null, false, "Constant pressure and temperature for production stage", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            espressoInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(espressoName, espressoDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Espresso Application Interface Id " + espressoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerGromacsInterface() {
        try {
            System.out.println("#### Registering Gromacs Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Portable_Input_Binary_File", null,
                    DataType.URI, null, false, "Coordinates velocities, molecular topology and simulation parameters", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            gromacsInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(gromacsName, gromacsDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Gromacs Application Interface Id " + gromacsInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerLammpsInterface() {
        try {
            System.out.println("#### Registering LAMMPS Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Friction_Simulation_Input", null,
                    DataType.URI, null, false, "Friction Simulation Input", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            lammpsInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(lammpsName, lammpsDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("LAMMPS Application Interface Id " + lammpsInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerNWChemInterface() {
        try {
            System.out.println("#### Registering NWChem Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Water_Molecule_Input", null,
                    DataType.URI, null, false, "Water Molecule Input File", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            nwChemInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(nwChemName, nwChemDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("NWChem Application Interface Id " + nwChemInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public static void registerTrinityInterface() {
        try {
            System.out.println("#### Registering Trinity Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("RNA_Seq_Left_Input", null,
                    DataType.URI, null, false, "RNA-Seq Left Library", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("RNA_Seq_Left_Input", null,
                    DataType.URI, null, false, "RNA-Seq Right Library", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            trinityInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(trinityName, trinityDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Trinity Application Interface Id " + trinityInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }


    public static void registerWRFInterface() {
        try {
            System.out.println("#### Registering WRF Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Config_Namelist_File", null,
                    DataType.URI, null, false, "Namelist Configuration File", null);

            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("WRF_Initial_Conditions", null,
                    DataType.URI, null, false, "Inputial Conditions File", null);

            InputDataObjectType input3 = RegisterSampleApplicationsUtils.createAppInput("WRF_Boundary_File", null,
                    DataType.URI, null, false, "Boundary Conditions File", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);
            applicationInputs.add(input3);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("WRF_Output",
                    "", DataType.URI);

            OutputDataObjectType output2 = RegisterSampleApplicationsUtils.createAppOutput("WRF_Execution_Log",
                    "", DataType.URI);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);
            applicationOutputs.add(output2);

            wrfInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription(wrfName, wrfDescription,
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("WRF Application Interface Id " + wrfInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerStampedeApps() {
        try {
            System.out.println("#### Registering Application Deployments on Stampede #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, stampedeResourceId,
                            "/bin/echo", ApplicationParallelismType.SERIAL, echoDescription));
            System.out.println("Echo on stampede deployment Id " + echoAppDeployId);

            //Register Amber
            String amberAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/amber_wrapper_simple.sh", ApplicationParallelismType.MPI,
                            amberDescription));
            System.out.println("Amber on stampede deployment Id " + amberAppDeployId);

            //Register ESPRESSO
            String espressoAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(espressoModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/espresso_wrapper.sh", ApplicationParallelismType.MPI,
                            espressoDescription));
            System.out.println("ESPRESSO on stampede deployment Id " + espressoAppDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            gromacsDescription));
            System.out.println("GROMACS on stampede deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            lammpsDescription));
            System.out.println("LAMMPS on stampede deployment Id " + lammpsAppDeployId);

            //Register NWChem
            String nwChemAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(nwChemModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/nwchem_wrapper.sh", ApplicationParallelismType.MPI,
                            nwChemDescription));
            System.out.println("NWChem on stampede deployment Id " + nwChemAppDeployId);

            //Register Trinity
            String trinityAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(trinityModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/trinity_wrapper.sh", ApplicationParallelismType.MPI,
                            trinityDescription));
            System.out.println("Trinity on stampede deployment Id " + trinityAppDeployId);

            //Register WRF
            String wrfAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(wrfModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/wrf_wrapper_3.5.1.sh", ApplicationParallelismType.MPI,
                            wrfDescription));
            System.out.println("WRF on stampede deployment Id " + wrfAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerTrestlesApps() {
        try {
            System.out.println("#### Registering Application Deployments on Trestles #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, trestlesResourceId,
                            "/bin/echo", ApplicationParallelismType.SERIAL, echoDescription));
            System.out.println("Echo on trestles deployment Id " + echoAppDeployId);

            //Register Amber
            String amberAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/amber_wrapper_simple.sh", ApplicationParallelismType.MPI,
                            amberDescription));
            System.out.println("Amber on trestles deployment Id " + amberAppDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            gromacsDescription));
            System.out.println("GROMACS on trestles deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            lammpsDescription));
            System.out.println("LAMMPS on trestles deployment Id " + lammpsAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void registerBigRedApps() {
        try {
            System.out.println("#### Registering Application Deployments on BigRed II #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, bigredResourceId,
                            "/bin/echo", ApplicationParallelismType.SERIAL, echoDescription));
            System.out.println("Echo on bigredII deployment Id " + echoAppDeployId);

            //Register Amber
            String amberAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/amber_wrapper_simple.sh", ApplicationParallelismType.MPI,
                            amberDescription));
            System.out.println("Amber on bigredII deployment Id " + amberAppDeployId);

            //Register AutoDock
            String autoDockDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(nwChemModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/auto_dock_wrapper.sh", ApplicationParallelismType.MPI,
                            autoDockDescription));
            System.out.println("AutoDock on bigredII deployment Id " + autoDockDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            gromacsDescription));
            System.out.println("GROMACS on bigredII deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            lammpsDescription));
            System.out.println("LAMMPS on bigredII deployment Id " + lammpsAppDeployId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static String registerComputeHost(String hostName, String hostDesc,
                                             ResourceJobManagerType resourceJobManagerType,
                                             String monitoringEndPoint, String jobMangerBinPath,
                                             SecurityProtocol securityProtocol, int portNumber) throws TException {

        ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils.
                createComputeResourceDescription(hostName, hostDesc, null, null);

        String computeResourceId = airavataClient.registerComputeResource(computeResourceDescription);

        if (computeResourceId.isEmpty()) throw new AiravataClientException();

        ResourceJobManager resourceJobManager = RegisterSampleApplicationsUtils.
                createResourceJobManager(resourceJobManagerType, monitoringEndPoint, jobMangerBinPath, null);

        SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
        sshJobSubmission.setResourceJobManager(resourceJobManager);
        sshJobSubmission.setSecurityProtocol(securityProtocol);
        sshJobSubmission.setSshPort(portNumber);
        boolean sshAddStatus = airavataClient.addSSHJobSubmissionDetails(computeResourceId, 1, sshJobSubmission);

        if (!sshAddStatus) throw new AiravataClientException();

        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setSecurityProtocol(securityProtocol);
        scpDataMovement.setSshPort(portNumber);
        boolean scpAddStatus = airavataClient.addSCPDataMovementDetails(computeResourceId, 1, scpDataMovement);

        if (!scpAddStatus) throw new AiravataClientException();

        return computeResourceId;
    }

    public static void registerGatewayResourceProfile() {

        try {
            System.out.println("#### Registering Application Deployments on BigRed II #### \n");


            ComputeResourcePreference stampedeResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(stampedeResourceId, "TG-STA110014S", false, null, null, null,
                            "/scratch/01437/ogce/gta-work-dirs");

            ComputeResourcePreference trestlesResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(trestlesResourceId, "TG-STA110014S", false, null, null, null,
                            "/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs");

            ComputeResourcePreference bigRedResourcePreferences = RegisterSampleApplicationsUtils.
                    createComputeResourcePreference(bigredResourceId, "TG-STA110014S", false, null, null, null,
                            "/N/dc2/scratch/cgateway/gta-work-dirs");

            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setGatewayID(DEFAULT_GATEWAY);
            gatewayResourceProfile.setGatewayName(DEFAULT_GATEWAY);
            gatewayResourceProfile.addToComputeResourcePreferences(stampedeResourcePreferences);
            gatewayResourceProfile.addToComputeResourcePreferences(trestlesResourcePreferences);
            gatewayResourceProfile.addToComputeResourcePreferences(bigRedResourcePreferences);

            String gatewayProfile = airavataClient.registerGatewayResourceProfile(gatewayResourceProfile);
            System.out.println("Gateway Profile is registered with Id " + gatewayProfile);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

}

