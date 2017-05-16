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
import org.apache.airavata.testsuite.multitenantedairavata.utils.ApplicationProperties;
import org.apache.airavata.testsuite.multitenantedairavata.utils.FrameworkUtils;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.LocalApplication.*;

public class ApplicationRegister {
    private Airavata.Client airavata;
    private List<Gateway> gateways;
    private Map<String, String> applicationInterfaceListPerGateway;
    private Map<String, String> applicationDeployementListPerGateway;
    private final static Logger logger = LoggerFactory.getLogger(ApplicationRegister.class);
    private String localResourceId;
    private AuthzToken authzToken;
    private TestFrameworkProps props;


    public ApplicationRegister(Airavata.Client airavata, TestFrameworkProps props) throws Exception {
        this.airavata = airavata;
        authzToken = new AuthzToken("emptyToken");
        gateways = getAllGateways(airavata);
        applicationInterfaceListPerGateway = new HashMap<String, String>();
        applicationDeployementListPerGateway = new HashMap<String, String>();
        FrameworkUtils frameworkUtils = FrameworkUtils.getInstance();
        this.props = props;
    }

    public List<Gateway> getAllGateways(Airavata.Client client) throws Exception{
        try {
             return client.getAllGateways(authzToken);
        }catch (Exception e){
            logger.error("Error while getting all the gateways", e);
            throw new Exception("Error while getting all the gateways", e);
        }
    }

    public ApplicationProperties addApplications () throws Exception{
        Map<String, String> allComputeResourceNames = airavata.getAllComputeResourceNames(authzToken);
        System.out.println("All compute resources :" + allComputeResourceNames.size());
        for (String resourceId : allComputeResourceNames.keySet()){
            String resourceName = allComputeResourceNames.get(resourceId);
            if (resourceName.equals(TestFrameworkConstants.AppcatalogConstants.LOCAL_RESOURCE_NAME)){
                localResourceId = resourceId;
            }
        }
        return addLocalEchoApplication();
    }

    private ApplicationProperties addLocalEchoApplication() throws Exception{
        Gateway testGateway = airavata.getGateway(authzToken, props.getGname());

        String localEchoModuleId = airavata.registerApplicationModule(authzToken, props.getGname(),
                createApplicationModule(TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME, TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_VERSION, TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION));
        System.out.println("Echo Module Id " + localEchoModuleId);

        String echoInterfaceId = registerLocalEchoInterface(testGateway, localEchoModuleId);
        applicationInterfaceListPerGateway.put(echoInterfaceId, testGateway.getGatewayId());

        String echoLocalAppDeployId = airavata.registerApplicationDeployment(authzToken, testGateway.getGatewayId(),
                createApplicationDeployment(localEchoModuleId, localResourceId,
                        TestFrameworkConstants.LOCAL_ECHO_JOB_FILE_PATH, ApplicationParallelismType.SERIAL,
                        TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION, null, null, null));

        applicationDeployementListPerGateway.put(echoLocalAppDeployId, testGateway.getGatewayId());

        return new ApplicationProperties(localEchoModuleId, echoInterfaceId, echoLocalAppDeployId);
    }

    public ApplicationModule getApplicationModule(String applicationModuleId){
        ApplicationModule applicationModule = null;
        try {
            applicationModule = airavata.getApplicationModule(authzToken, applicationModuleId);
        } catch (TException e) {
            logger.error("Error fetching application module", e);
        }
        return applicationModule;
    }

    public ApplicationInterfaceDescription getApplicationInterfaceDescription(String applicationInterfaceId){
        ApplicationInterfaceDescription applicationInterfaceDescription = null;
        try {
            applicationInterfaceDescription = airavata.getApplicationInterface(authzToken, applicationInterfaceId);
        } catch (TException e) {
            logger.error("Error fetching application interface description", e);
        }
        return applicationInterfaceDescription;
    }


    public ApplicationDeploymentDescription getApplicationDeploymentDescription(String applicationDeployId){
        ApplicationDeploymentDescription applicationDeploymentDescription = null;
        try {
            applicationDeploymentDescription = airavata.getApplicationDeployment(authzToken, applicationDeployId);
        } catch (TException e) {
            logger.error("Error fetching application deployment description", e);
        }
        return applicationDeploymentDescription;
    }

    private String registerLocalEchoInterface(Gateway gateway, String moduleId) throws org.apache.thrift.TException {
        List<String> appModules = new ArrayList<String>();
        appModules.add(moduleId);

        InputDataObjectType input1 = createAppInput(INPUT_NAME, INPUT_VALUE,
                DataType.STRING, null, 0, true, true,false, INPUT_DESC, null);

        List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>(1);
        applicationInputs.add(input1);

        OutputDataObjectType output1 = createAppOutput(STDOUT_NAME, STDOUT_VALUE, DataType.URI, true, true, null);
        OutputDataObjectType output2 = createAppOutput(STDERR_NAME, STDERR_VALUE, DataType.URI, true, true, null);

        List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>(2);
        applicationOutputs.add(output1);
        applicationOutputs.add(output2);

        String localEchoInterfaceId = airavata.registerApplicationInterface(authzToken, gateway.getGatewayId(),
                createApplicationInterfaceDescription(TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_NAME, TestFrameworkConstants.AppcatalogConstants.LOCAL_ECHO_DESCRIPTION,
                        appModules, applicationInputs, applicationOutputs));
        System.out.println("Echo Local Application Interface Id " + localEchoInterfaceId);
        return localEchoInterfaceId;
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
