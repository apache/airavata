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

package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationRegister {
    private Airavata.Client airavata;
    private List<Gateway> allGateways;
    private Map<String, String> applicationInterfaceListPerGateway;
    private Map<String, String> applicationDeployementListPerGateway;
    private final static Logger logger = LoggerFactory.getLogger(ApplicationRegister.class);
    private String stampedeResourceId;
    private String trestlesResourceId;
    private String br2ResourceId;


    public ApplicationRegister(Airavata.Client airavata) throws Exception {
        this.airavata = airavata;
        allGateways = getAllGateways(airavata);
        applicationInterfaceListPerGateway = new HashMap<String, String>();
        applicationDeployementListPerGateway = new HashMap<String, String>();
    }

    public List<Gateway> getAllGateways(Airavata.Client client) throws Exception{
        try {
             return client.getAllGateways();
        }catch (Exception e){
            logger.error("Error while getting all the gateways", e);
            throw new Exception("Error while getting all the gateways", e);
        }
    }

    public void addApplications () throws Exception{
        Map<String, String> allComputeResourceNames = airavata.getAllComputeResourceNames();
        for (String resourceId : allComputeResourceNames.keySet()){
            String resourceName = allComputeResourceNames.get(resourceId);
            if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)){
                stampedeResourceId = resourceId;
            }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.TRESTLES_RESOURCE_NAME)){
                trestlesResourceId = resourceId;
            }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)){
                br2ResourceId = resourceId;
            }
        }
        addAmberApplication();
        addEchoApplication();
        addLAMMPSApplication();
    }

    protected void addAmberApplication () throws Exception{
        for (Gateway gateway : allGateways){
            // add amber module
            String amberModuleId = airavata.registerApplicationModule(gateway.getGatewayId(),
                    createApplicationModule(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME, "12.0", TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION));
            System.out.println("Amber Module Id " + amberModuleId);

            // add amber interface
            String amberInterfaceId = registerAmberInterface(gateway, amberModuleId);
            applicationInterfaceListPerGateway.put(amberInterfaceId, gateway.getGatewayId());

            // add amber deployment
            List<String> moduleLoadCMDs = new ArrayList<String>();
            moduleLoadCMDs.add("module load amber");
            ApplicationDeploymentDescription amberStampedeDeployment = createApplicationDeployment(amberModuleId, stampedeResourceId,
                    "/opt/apps/intel13/mvapich2_1_9/amber/12.0/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                    TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION, moduleLoadCMDs, null, null);
            String amberStampedeAppDeployId = airavata.registerApplicationDeployment(gateway.getGatewayId(),amberStampedeDeployment);

            String amberTrestlesAppDeployId = airavata.registerApplicationDeployment(gateway.getGatewayId(),
                    createApplicationDeployment(amberModuleId, trestlesResourceId,
                            "/opt/amber/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                            TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION, moduleLoadCMDs, null, null));

            List<String> amberModuleLoadCMDsBr2 = new ArrayList<String>();
            amberModuleLoadCMDsBr2.add("module load amber/gnu/mpi/12");
            amberModuleLoadCMDsBr2.add("module swap PrgEnv-cray PrgEnv-gnu");
            String amberBr2AppDeployId = airavata.registerApplicationDeployment(gateway.getGatewayId(),
                    createApplicationDeployment(amberModuleId, br2ResourceId,
                            "/N/soft/cle4/amber/gnu/mpi/12/amber12/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                            TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION, amberModuleLoadCMDsBr2, null, null));

            applicationDeployementListPerGateway.put(amberStampedeAppDeployId, gateway.getGatewayId());
            applicationDeployementListPerGateway.put(amberTrestlesAppDeployId, gateway.getGatewayId());
            applicationDeployementListPerGateway.put(amberBr2AppDeployId, gateway.getGatewayId());
        }

    }

    private String registerAmberInterface(Gateway gateway, String amberModuleId) throws org.apache.thrift.TException {
        List<String> appModules = new ArrayList<String>();
        appModules.add(amberModuleId);

        InputDataObjectType input1 = createAppInput("Heat_Restart_File", null,
                DataType.URI, null, 1, true, true,false, "Heating up the system equilibration stage - 02_Heat.rst", null);

        InputDataObjectType input2 = createAppInput("Production_Control_File", null,
                DataType.URI, null, 2, true, true, false, "Constant pressure and temperature for production stage - 03_Prod.in", null);

        InputDataObjectType input3 = createAppInput("Parameter_Topology_File", null,
                DataType.URI, null, 3, true, true, false, "Parameter and Topology coordinates - prmtop", null);

        List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
        applicationInputs.add(input1);
        applicationInputs.add(input2);
        applicationInputs.add(input3);

        OutputDataObjectType output1 = createAppOutput("AMBER_Execution_Summary", "03_Prod.info", DataType.URI, true, true);
        OutputDataObjectType output2 = createAppOutput("AMBER_Execution_log", "03_Prod.out", DataType.URI, true, true);
        OutputDataObjectType output3 = createAppOutput("AMBER_Trajectory_file", "03_Prod.mdcrd", DataType.URI, true, true);
        OutputDataObjectType output4 = createAppOutput("AMBER_Restart_file", "03_Prod.rst", DataType.URI, true, true);
        OutputDataObjectType output5 = createAppOutput("STDOUT", null, DataType.STDOUT, true, false);
        OutputDataObjectType output6 = createAppOutput("STDERR", null, DataType.STDERR, true, false);
        List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
        applicationOutputs.add(output1);
        applicationOutputs.add(output2);
        applicationOutputs.add(output3);
        applicationOutputs.add(output4);
        applicationOutputs.add(output5);
        applicationOutputs.add(output6);

        String amberInterfaceId = airavata.registerApplicationInterface(gateway.getGatewayId(),
                createApplicationInterfaceDescription(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME, TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION,
                        appModules, applicationInputs, applicationOutputs));
        System.out.println("Amber Application Interface Id " + amberInterfaceId);
        return amberInterfaceId;
    }

    private String registerEchoInterface(Gateway gateway, String moduleId) throws org.apache.thrift.TException {
        List<String> appModules = new ArrayList<String>();
        appModules.add(moduleId);

        InputDataObjectType input1 = createAppInput("input_to_Echo", null,
                DataType.STRING, null, 1, true, true,false, "Sample input to Echo", null);

        List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
        applicationInputs.add(input1);

        OutputDataObjectType output1 = createAppOutput("STDOUT", null, DataType.STDOUT, true, false);
        OutputDataObjectType output2 = createAppOutput("STDERR", null, DataType.STDERR, true, false);
        List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
        applicationOutputs.add(output1);
        applicationOutputs.add(output2);

        String echoInterfaceId = airavata.registerApplicationInterface(gateway.getGatewayId(),
                createApplicationInterfaceDescription(TestFrameworkConstants.AppcatalogConstants.ECHO_NAME, TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION,
                        appModules, applicationInputs, applicationOutputs));
        System.out.println("Echo Application Interface Id " + echoInterfaceId);
        return echoInterfaceId;
    }


    protected void addEchoApplication() throws Exception{
        for (Gateway gateway : allGateways){
            // add echo module
            String echoModuleId = airavata.registerApplicationModule(gateway.getGatewayId(),
                    createApplicationModule(TestFrameworkConstants.AppcatalogConstants.ECHO_NAME, "1.0", TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION));
            System.out.println("Echo Module Id " + echoModuleId);

            // add amber interface
            String echoInterfaceId = registerEchoInterface(gateway, echoModuleId);
            applicationInterfaceListPerGateway.put(echoInterfaceId, gateway.getGatewayId());

            // add amber deployment
            String echoStampedeAppDeployId = airavata.registerApplicationDeployment(gateway.getGatewayId(),
                    createApplicationDeployment(echoModuleId, stampedeResourceId,
                            "/home1/01437/ogce/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                            TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION, null, null, null));

            String echoTrestlesAppDeployId = airavata.registerApplicationDeployment(gateway.getGatewayId(),
                    createApplicationDeployment(echoModuleId, trestlesResourceId,
                            "/home/ogce/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                            TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION, null, null, null));

            String echoBr2AppDeployId = airavata.registerApplicationDeployment(gateway.getGatewayId(),
                    createApplicationDeployment(echoModuleId, br2ResourceId,
                            "/N/u/cgateway/BigRed2/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                            TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION, null, null, null));

            applicationDeployementListPerGateway.put(echoStampedeAppDeployId, gateway.getGatewayId());
            applicationDeployementListPerGateway.put(echoTrestlesAppDeployId, gateway.getGatewayId());
            applicationDeployementListPerGateway.put(echoBr2AppDeployId, gateway.getGatewayId());
        }
    }

    protected void addLAMMPSApplication() throws Exception{
        // add LAMPPS module
        // add LAMPSS interface
        // add LAMPSS deployment
    }


    protected ApplicationDeploymentDescription createApplicationDeployment(String appModuleId,
                                                                               String computeResourceId,
                                                                               String executablePath,
                                                                               ApplicationParallelismType parallelism,
                                                                               String appDeploymentDescription,
                                                                               List<String> moduleLoadCmds,
                                                                               List<String> preJobCmds,
                                                                               List<String> postJobCmds) {
        ApplicationDeploymentDescription deployment = new ApplicationDeploymentDescription();
        deployment.setAppDeploymentDescription(appDeploymentDescription);
        deployment.setAppModuleId(appModuleId);
        deployment.setComputeHostId(computeResourceId);
        deployment.setExecutablePath(executablePath);
        deployment.setParallelism(parallelism);
        deployment.setModuleLoadCmds(moduleLoadCmds);
        deployment.setPreJobCommands(preJobCmds);
        deployment.setPostJobCommands(postJobCmds);
        return deployment;
    }

    protected ApplicationModule createApplicationModule(String appModuleName,
                                                            String appModuleVersion, String appModuleDescription) {
        ApplicationModule module = new ApplicationModule();
        module.setAppModuleDescription(appModuleDescription);
        module.setAppModuleName(appModuleName);
        module.setAppModuleVersion(appModuleVersion);
        return module;
    }

    protected InputDataObjectType createAppInput (String inputName,
                                                      String value,
                                                      DataType type,
                                                      String applicationArgument,
                                                      int order,
                                                      boolean isRequired,
                                                      boolean requiredToCMD,
                                                      boolean stdIn,
                                                      String description,
                                                      String metadata) {
        InputDataObjectType input = new InputDataObjectType();
        if (inputName != null) input.setName(inputName);
        if (value != null) input.setValue(value);
        if (type != null) input.setType(type);
        if (applicationArgument != null) input.setApplicationArgument(applicationArgument);
        input.setInputOrder(order);
        input.setIsRequired(isRequired);
        input.setRequiredToAddedToCommandLine(requiredToCMD);
        if (description != null) input.setUserFriendlyDescription(description);
        input.setStandardInput(stdIn);
        if (metadata != null) input.setMetaData(metadata);
        return input;
    }

    protected OutputDataObjectType createAppOutput(String inputName,
                                                       String value,
                                                       DataType type,
                                                       boolean isRequired,
                                                       boolean requiredToCMD) {
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        if (inputName != null) outputDataObjectType.setName(inputName);
        if (value != null) outputDataObjectType.setValue(value);
        if (type != null) outputDataObjectType.setType(type);
        outputDataObjectType.setIsRequired(isRequired);
        outputDataObjectType.setRequiredToAddedToCommandLine(requiredToCMD);
        return outputDataObjectType;
    }

    protected ApplicationInterfaceDescription createApplicationInterfaceDescription
            (String applicationName, String applicationDescription, List<String> applicationModules,
             List<InputDataObjectType> applicationInputs, List<OutputDataObjectType>applicationOutputs) {
        ApplicationInterfaceDescription applicationInterfaceDescription = new ApplicationInterfaceDescription();

        applicationInterfaceDescription.setApplicationName(applicationName);
        if (applicationDescription != null) applicationInterfaceDescription.setApplicationDescription(applicationDescription);
        if (applicationModules != null) applicationInterfaceDescription.setApplicationModules(applicationModules);
        if (applicationInputs != null) applicationInterfaceDescription.setApplicationInputs(applicationInputs);
        if (applicationOutputs != null) applicationInterfaceDescription.setApplicationOutputs(applicationOutputs);

        return applicationInterfaceDescription;
    }

    public Map<String, String> getApplicationInterfaceListPerGateway() {
        return applicationInterfaceListPerGateway;
    }

    public void setApplicationInterfaceListPerGateway(Map<String, String> applicationInterfaceListPerGateway) {
        this.applicationInterfaceListPerGateway = applicationInterfaceListPerGateway;
    }

    public Map<String, String> getApplicationDeployementListPerGateway() {
        return applicationDeployementListPerGateway;
    }

    public void setApplicationDeployementListPerGateway(Map<String, String> applicationDeployementListPerGateway) {
        this.applicationDeployementListPerGateway = applicationDeployementListPerGateway;
    }
}
