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

    public static final String THRIFT_SERVER_HOST = "localhost";
    public static final int THRIFT_SERVER_PORT = 8930;
    private final static Logger logger = LoggerFactory.getLogger(RegisterSampleApplications.class);
    private static final String DEFAULT_USER = "default.registry.user";
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static Airavata.Client airavataClient;

    //Host Id's
    private static String stampedeResourceId;
    private static String trestlesResourceId;
    private static String bigredResourceId;

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

    public static void main(String[] args) {
        try {
            airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println("API version is " + airavataClient.getAPIVersion());

            //Register all compute hosts
            registerXSEDEHosts();

            //Register all application modules
            registerAppModules();

            //Register all application deployments
            registerAppDeployments();

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
                            "Echo", "1.0", "A Simple Echo Application"));
            System.out.println("Echo Module Id " + echoModuleId);

            //Register Amber
            amberModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "Amber", "12.0", "Amber Molecular Dynamics Package"));
            System.out.println("Amber Module Id " + amberModuleId);

            //Register AutoDock
            autoDockModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "AutoDock", "4.2", "AutoDock suite of automated docking tools"));
            System.out.println("AutoDock Module Id " + autoDockModuleId);

            //Register ESPRESSO
            espressoModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "ESPRESSO", "5.0.3", "Nanoscale electronic-structure calculations and materials modeling"));
            System.out.println("ESPRESSO Module Id " + espressoModuleId);

            //Register GROMACS
            gromacsModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "GROMACS", "4.6.5", "GROMACS Molecular Dynamics Package"));
            System.out.println("GROMACS Module Id " + gromacsModuleId);

            //Register LAMMPS
            lammpsModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "LAMMPS", "20Mar14", "Large-scale Atomic/Molecular Massively Parallel Simulator"));
            System.out.println("LAMMPS Module Id " + lammpsModuleId);

            //Register NWChem
            nwChemModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "NWChem", "6.3", "Ab initio computational chemistry software package"));
            System.out.println("NWChem Module Id " + nwChemModuleId);

            //Register Trinity
            trinityModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "Trinity", "r20130225", "de novo reconstruction of transcriptomes from RNA-seq data"));
            System.out.println("Trinity Module Id " + trinityModuleId);

            //Register WRF
            wrfModuleId = airavataClient.registerApplicationModule(
                    RegisterSampleApplicationsUtils.createApplicationModule(
                            "WRF", "3.5.1", "Weather Research and Forecasting"));
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

    public static void registerStampedeApps() {
        try {
            System.out.println("#### Registering Application Deployments on Stampede #### \n");

            //Register Echo
            String echoAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, stampedeResourceId,
                            "/bin/echo", ApplicationParallelismType.SERIAL, "Echo Testing Application"));
            System.out.println("Echo on stampede deployment Id " + echoAppDeployId);

            //Register Amber
            String amberAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/amber_wrapper_simple.sh", ApplicationParallelismType.MPI,
                            "Amber Molecular Dynamics Package"));
            System.out.println("Amber on stampede deployment Id " + amberAppDeployId);

            //Register ESPRESSO
            String espressoAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(espressoModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/espresso_wrapper.sh", ApplicationParallelismType.MPI,
                            "Nanoscale electronic-structure calculations and materials modeling"));
            System.out.println("ESPRESSO on stampede deployment Id " + espressoAppDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            "GROMACS Molecular Dynamics Package"));
            System.out.println("GROMACS on stampede deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            "Large-scale Atomic/Molecular Massively Parallel Simulator"));
            System.out.println("LAMMPS on stampede deployment Id " + lammpsAppDeployId);

            //Register NWChem
            String nwChemAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(nwChemModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/nwchem_wrapper.sh", ApplicationParallelismType.MPI,
                            "Ab initio computational chemistry software package"));
            System.out.println("NWChem on stampede deployment Id " + nwChemAppDeployId);

            //Register Trinity
            String trinityAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(trinityModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/trinity_wrapper.sh", ApplicationParallelismType.MPI,
                            "de novo reconstruction of transcriptomes from RNA-seq data"));
            System.out.println("Trinity on stampede deployment Id " + trinityAppDeployId);

            //Register WRF
            String wrfAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(wrfModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/wrf_wrapper_3.5.1.sh", ApplicationParallelismType.MPI,
                            "Weather Research and Forecasting"));
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
                            "/bin/echo", ApplicationParallelismType.SERIAL, "Echo Testing Application"));
            System.out.println("Echo on trestles deployment Id " + echoAppDeployId);

            //Register Amber
            String amberAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/amber_wrapper_simple.sh", ApplicationParallelismType.MPI,
                            "Amber Molecular Dynamics Package"));
            System.out.println("Amber on trestles deployment Id " + amberAppDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            "GROMACS Molecular Dynamics Package"));
            System.out.println("GROMACS on trestles deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            "Large-scale Atomic/Molecular Massively Parallel Simulator"));
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
                            "/bin/echo", ApplicationParallelismType.SERIAL, "Echo Testing Application"));
            System.out.println("Echo on bigredII deployment Id " + echoAppDeployId);

            //Register Amber
            String amberAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(amberModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/amber_wrapper_simple.sh", ApplicationParallelismType.MPI,
                            "Amber Molecular Dynamics Package"));
            System.out.println("Amber on bigredII deployment Id " + amberAppDeployId);

            //Register AutoDock
            String autoDockDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(nwChemModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/auto_dock_wrapper.sh", ApplicationParallelismType.MPI,
                            "AutoDock suite of automated docking tools"));
            System.out.println("AutoDock on bigredII deployment Id " + autoDockDeployId);

            //Register GROMACS
            String gromacsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(gromacsModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/gromacs_wrapper.sh", ApplicationParallelismType.MPI,
                            "GROMACS Molecular Dynamics Package"));
            System.out.println("GROMACS on bigredII deployment Id " + gromacsAppDeployId);

            //Register LAMMPS
            String lammpsAppDeployId = airavataClient.registerApplicationDeployment(
                    RegisterSampleApplicationsUtils.createApplicationDeployment(lammpsModuleId, bigredResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/lammps_wrapper.sh", ApplicationParallelismType.MPI,
                            "Large-scale Atomic/Molecular Massively Parallel Simulator"));
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
}

