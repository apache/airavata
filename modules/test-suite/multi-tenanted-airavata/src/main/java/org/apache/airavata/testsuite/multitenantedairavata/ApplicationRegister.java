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
import org.apache.airavata.model.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.testsuite.multitenantedairavata.utils.FrameworkUtils;
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
    private String gordenResourceId;
    private String alamoResourceId;
    private List<String> gatewaysToAvoid;
    private AuthzToken authzToken;


    public ApplicationRegister(Airavata.Client airavata, TestFrameworkProps props) throws Exception {
        this.airavata = airavata;
        authzToken = new AuthzToken("emptyToken");
        allGateways = getAllGateways(airavata);
        applicationInterfaceListPerGateway = new HashMap<String, String>();
        applicationDeployementListPerGateway = new HashMap<String, String>();
        FrameworkUtils frameworkUtils = FrameworkUtils.getInstance();
        gatewaysToAvoid = frameworkUtils.getGatewayListToAvoid(props.getSkippedGateways());
    }

    public List<Gateway> getAllGateways(Airavata.Client client) throws Exception{
        try {
             return client.getAllGateways(authzToken);
        }catch (Exception e){
            logger.error("Error while getting all the gateways", e);
            throw new Exception("Error while getting all the gateways", e);
        }
    }

    public void addApplications () throws Exception{
        Map<String, String> allComputeResourceNames = airavata.getAllComputeResourceNames(authzToken);
        System.out.println("All compute resources :" + allComputeResourceNames.size());
        for (String resourceId : allComputeResourceNames.keySet()){
            String resourceName = allComputeResourceNames.get(resourceId);
            if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.STAMPEDE_RESOURCE_NAME)){
                stampedeResourceId = resourceId;
            }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.BR2_RESOURCE_NAME)){
                br2ResourceId = resourceId;
            }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.GORDEN_RESOURCE_NAME)){
                gordenResourceId = resourceId;
            }else if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.ALAMO_RESOURCE_NAME)){
                alamoResourceId = resourceId;
            }
        }
//        addUltrascanApplication();
        addAmberApplication();
        addEchoApplication();
        addLAMMPSApplication();
    }

    protected void addAmberApplication () throws Exception{
        for (Gateway gateway : allGateways) {
                boolean isgatewayValid = true;
                for (String ovoidGateway : gatewaysToAvoid){
                    if (gateway.getGatewayId().equals(ovoidGateway)){
                        isgatewayValid = false;
                        break;
                    }
                }
                if (isgatewayValid) {
                    // add amber module
                    String amberModuleId = airavata.registerApplicationModule(authzToken, gateway.getGatewayId(),
                            createApplicationModule(TestFrameworkConstants.AppcatalogConstants.AMBER_APP_NAME, "12.0", TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION));
                    System.out.println("Amber Module Id " + amberModuleId);

                    // add amber interface
                    String amberInterfaceId = registerAmberInterface(gateway, amberModuleId);
                    applicationInterfaceListPerGateway.put(amberInterfaceId, gateway.getGatewayId());

                    // add amber deployment
                    List<CommandObject> moduleLoadCMDs = new ArrayList();
                    CommandObject cmd  = new CommandObject();
                    cmd.setCommand("module load amber");
                    cmd.setCommandOrder(0);
                    moduleLoadCMDs.add(cmd);
                    ApplicationDeploymentDescription amberStampedeDeployment = createApplicationDeployment(amberModuleId, stampedeResourceId,
                            "/opt/apps/intel13/mvapich2_1_9/amber/12.0/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                            TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION, moduleLoadCMDs, null, null);
                    String amberStampedeAppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(), amberStampedeDeployment);

                    String amberTrestlesAppDeployId = airavata.registerApplicationDeployment(authzToken,gateway.getGatewayId(),
                            createApplicationDeployment(amberModuleId, trestlesResourceId,
                                    "/opt/amber/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                                    TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION, moduleLoadCMDs, null, null));

                    List<CommandObject> amberModuleLoadCMDsBr2 = new ArrayList<>();
                    cmd  = new CommandObject();
                    cmd.setCommand("module load amber/gnu/mpi/12");
                    cmd.setCommandOrder(0);
                    amberModuleLoadCMDsBr2.add(cmd);

                    cmd  = new CommandObject();
                    cmd.setCommand("module swap PrgEnv-cray PrgEnv-gnu");
                    cmd.setCommandOrder(1);
                    amberModuleLoadCMDsBr2.add(cmd);
                    amberModuleLoadCMDsBr2.add(cmd);

                    String amberBr2AppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                            createApplicationDeployment(amberModuleId, br2ResourceId,
                                    "/N/soft/cle4/amber/gnu/mpi/12/amber12/bin/sander.MPI -O", ApplicationParallelismType.MPI,
                                    TestFrameworkConstants.AppcatalogConstants.AMBER_DESCRIPTION, amberModuleLoadCMDsBr2, null, null));

                    applicationDeployementListPerGateway.put(amberStampedeAppDeployId, gateway.getGatewayId());
                    applicationDeployementListPerGateway.put(amberTrestlesAppDeployId, gateway.getGatewayId());
                    applicationDeployementListPerGateway.put(amberBr2AppDeployId, gateway.getGatewayId());
                }
        }


    }

    protected void addUltrascanApplication () throws Exception{
        for (Gateway gateway : allGateways) {
            boolean isgatewayValid = true;
            for (String ovoidGateway : gatewaysToAvoid){
                if (gateway.getGatewayId().equals(ovoidGateway)){
                    isgatewayValid = false;
                    break;
                }
            }
            if (isgatewayValid) {
                // add amber module
                String ultrascanModuleId = airavata.registerApplicationModule(authzToken, gateway.getGatewayId(),
                        createApplicationModule(TestFrameworkConstants.AppcatalogConstants.ULTRASCAN, "1.0", TestFrameworkConstants.AppcatalogConstants.ULTRASCAN_DESCRIPTION));
                System.out.println("Ultrascan module Id " + ultrascanModuleId);

                // add amber interface
                String ultrascanInterfaceId = registerUltrascanInterface(gateway, ultrascanModuleId);
                applicationInterfaceListPerGateway.put(ultrascanInterfaceId, gateway.getGatewayId());

                // add amber deployment
                ApplicationDeploymentDescription ultrascanStampedeDeployment = createApplicationDeployment(ultrascanModuleId, stampedeResourceId,
                        "/home1/01623/us3/bin/us_mpi_analysis", ApplicationParallelismType.MPI,
                        TestFrameworkConstants.AppcatalogConstants.ULTRASCAN_DESCRIPTION, null, null, null);
                String ultrascanStampedeAppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(), ultrascanStampedeDeployment);

                String ultrascanTrestlesAppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                        createApplicationDeployment(ultrascanModuleId, trestlesResourceId,
                                "/home/us3/trestles/bin/us_mpi_analysis", ApplicationParallelismType.MPI,
                                TestFrameworkConstants.AppcatalogConstants.ULTRASCAN_DESCRIPTION, null, null, null));

                String ultrascanGordenAppDepId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                        createApplicationDeployment(ultrascanModuleId,gordenResourceId,
                                "/home/us3/gordon/bin/us_mpi_analysis", ApplicationParallelismType.MPI,
                                TestFrameworkConstants.AppcatalogConstants.ULTRASCAN_DESCRIPTION, null, null, null));

                List<CommandObject> alamoModules = new ArrayList<>();
                CommandObject cmd = new CommandObject("module load intel/2015/64");
                alamoModules.add(cmd);
                cmd = new CommandObject("module load openmpi/intel/1.8.4");
                alamoModules.add(cmd);
                cmd = new CommandObject("module load qt4/4.8.6");
                alamoModules.add(cmd);
                cmd = new CommandObject("module load ultrascan3/3.3");
                alamoModules.add(cmd);

                String ultrascanAlamoAppId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                        createApplicationDeployment(ultrascanModuleId,alamoResourceId,
                                "/home/us3/bin/us_mpi_analysis", ApplicationParallelismType.OPENMP,
                                TestFrameworkConstants.AppcatalogConstants.ULTRASCAN_DESCRIPTION, alamoModules, null, null));

                applicationDeployementListPerGateway.put(ultrascanStampedeAppDeployId, gateway.getGatewayId());
                applicationDeployementListPerGateway.put(ultrascanTrestlesAppDeployId, gateway.getGatewayId());
                applicationDeployementListPerGateway.put(ultrascanGordenAppDepId, gateway.getGatewayId());
                applicationDeployementListPerGateway.put(ultrascanAlamoAppId, gateway.getGatewayId());
            }
        }
    }

    private String registerUltrascanInterface(Gateway gateway, String ultrascanModuleId) throws org.apache.thrift.TException {
        List<String> appModules = new ArrayList<String>();
        appModules.add(ultrascanModuleId);

        InputDataObjectType input1 = createAppInput("input", null,
                DataType.URI, null, 1, true, true,false, "Input tar file", null);

        InputDataObjectType input2 = createAppInput("mgroupcount", "-mgroupcount=1",
                DataType.STRING, null, 3, true, true,false, "mgroupcount", null);

        InputDataObjectType input3 = createAppInput("walltime", "-walltime=60",
                DataType.STRING, null, 2, true, true,false, "walltime", null);
        List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
        applicationInputs.add(input1);
        applicationInputs.add(input2);
        applicationInputs.add(input3);

        OutputDataObjectType output1 = createAppOutput("ultrascanOutput", "analysis-results.tar", DataType.URI, true, false, null);
        output1.setLocation("output");
        OutputDataObjectType output2 = createAppOutput("STDOUT", null, DataType.STDOUT, true, false, null);
        OutputDataObjectType output3 = createAppOutput("STDERR", null, DataType.STDERR, true, false, null);
        List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
        applicationOutputs.add(output1);
        applicationOutputs.add(output2);
        applicationOutputs.add(output3);

        String ultrascanAppId = airavata.registerApplicationInterface(authzToken, gateway.getGatewayId(),
                createApplicationInterfaceDescription(TestFrameworkConstants.AppcatalogConstants.ULTRASCAN, TestFrameworkConstants.AppcatalogConstants.ULTRASCAN_DESCRIPTION,
                        appModules, applicationInputs, applicationOutputs));
        System.out.println("Ultrascan Application Interface Id " + ultrascanAppId);
        return ultrascanAppId;
    }

    private String registerAmberInterface(Gateway gateway, String amberModuleId) throws org.apache.thrift.TException {
        List<String> appModules = new ArrayList<String>();
        appModules.add(amberModuleId);

        InputDataObjectType input1 = createAppInput("heatRst", null,
                DataType.URI, "-c", 1, true, true,false, "Heating up the system equilibration stage - 02_Heat.rst", null);

        InputDataObjectType input2 = createAppInput("prodIn", null,
                DataType.URI, "-i ", 2, true, true, false, "Constant pressure and temperature for production stage - 03_Prod.in", null);

        InputDataObjectType input3 = createAppInput("prmtop", null,
                DataType.URI, "-p", 3, true, true, false, "Parameter and Topology coordinates - prmtop", null);

        List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
        applicationInputs.add(input1);
        applicationInputs.add(input2);
        applicationInputs.add(input3);

        OutputDataObjectType output1 = createAppOutput("AMBER_Execution_Summary", "03_Prod.info", DataType.URI, true, true, "-inf");
        OutputDataObjectType output2 = createAppOutput("AMBER_Execution_log", "03_Prod.out", DataType.URI, true, true, "-o");
        OutputDataObjectType output3 = createAppOutput("AMBER_Trajectory_file", "03_Prod.mdcrd", DataType.URI, true, true, "-x");
        OutputDataObjectType output4 = createAppOutput("AMBER_Restart_file", "03_Prod.rst", DataType.URI, true, true, " -r");
        OutputDataObjectType output5 = createAppOutput("STDOUT", null, DataType.STDOUT, true, false, null);
        OutputDataObjectType output6 = createAppOutput("STDERR", null, DataType.STDERR, true, false, null);
        List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
        applicationOutputs.add(output1);
        applicationOutputs.add(output2);
        applicationOutputs.add(output3);
        applicationOutputs.add(output4);
        applicationOutputs.add(output5);
        applicationOutputs.add(output6);

        String amberInterfaceId = airavata.registerApplicationInterface(authzToken, gateway.getGatewayId(),
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

        OutputDataObjectType output1 = createAppOutput("STDOUT", null, DataType.STDOUT, true, false, null);
        OutputDataObjectType output2 = createAppOutput("STDERR", null, DataType.STDERR, true, false, null);
        List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
        applicationOutputs.add(output1);
        applicationOutputs.add(output2);

        String echoInterfaceId = airavata.registerApplicationInterface(authzToken, gateway.getGatewayId(),
                createApplicationInterfaceDescription(TestFrameworkConstants.AppcatalogConstants.ECHO_NAME, TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION,
                        appModules, applicationInputs, applicationOutputs));
        System.out.println("Echo Application Interface Id " + echoInterfaceId);
        return echoInterfaceId;
    }


    protected void addEchoApplication() throws Exception{
        for (Gateway gateway : allGateways){
            boolean isgatewayValid = true;
            for (String ovoidGateway : gatewaysToAvoid){
                if (gateway.getGatewayId().equals(ovoidGateway)){
                    isgatewayValid = false;
                    break;
                }
            }
            if (isgatewayValid) {
                // add echo module
                String echoModuleId = airavata.registerApplicationModule(authzToken, gateway.getGatewayId(),
                        createApplicationModule(TestFrameworkConstants.AppcatalogConstants.ECHO_NAME, "1.0", TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION));
                System.out.println("Echo Module Id " + echoModuleId);

                // add amber interface
                String echoInterfaceId = registerEchoInterface(gateway, echoModuleId);
                applicationInterfaceListPerGateway.put(echoInterfaceId, gateway.getGatewayId());

                // add amber deployment
                String echoStampedeAppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                        createApplicationDeployment(echoModuleId, stampedeResourceId,
                                "/home1/01437/ogce/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                                TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION, null, null, null));

                String echoTrestlesAppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                        createApplicationDeployment(echoModuleId, trestlesResourceId,
                                "/home/ogce/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                                TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION, null, null, null));

                String echoBr2AppDeployId = airavata.registerApplicationDeployment(authzToken, gateway.getGatewayId(),
                        createApplicationDeployment(echoModuleId, br2ResourceId,
                                "/N/u/cgateway/BigRed2/production/app_wrappers/echo_wrapper.sh", ApplicationParallelismType.SERIAL,
                                TestFrameworkConstants.AppcatalogConstants.ECHO_DESCRIPTION, null, null, null));

                applicationDeployementListPerGateway.put(echoStampedeAppDeployId, gateway.getGatewayId());
                applicationDeployementListPerGateway.put(echoTrestlesAppDeployId, gateway.getGatewayId());
                applicationDeployementListPerGateway.put(echoBr2AppDeployId, gateway.getGatewayId());
            }
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
                                                                               List<CommandObject> moduleLoadCmds,
                                                                               List<CommandObject> preJobCmds,
                                                                               List<CommandObject> postJobCmds) {
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
                                                       boolean requiredToCMD,
                                                       String argument) {
        OutputDataObjectType outputDataObjectType = new OutputDataObjectType();
        if (inputName != null) outputDataObjectType.setName(inputName);
        if (value != null) outputDataObjectType.setValue(value);
        if (type != null) outputDataObjectType.setType(type);
        outputDataObjectType.setIsRequired(isRequired);
        outputDataObjectType.setRequiredToAddedToCommandLine(requiredToCMD);
        outputDataObjectType.setApplicationArgument(argument);
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
