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
package org.apache.airavata.orchestrator.core;

import junit.framework.Assert;
import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.orchestrator.cpi.Orchestrator;
import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryImpl;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewOrchestratorTest extends AbstractOrchestratorTest {
    private static final Logger log = LoggerFactory.getLogger(NewOrchestratorTest.class);

    private Orchestrator orchestrator;
    private String experimentID;

    @BeforeTest
    public void setUp() throws Exception {
        AiravataUtils.setExecutionAsServer();
        super.setUp();
        orchestrator = new SimpleOrchestratorImpl();
        createJobRequestWithDocuments(getAiravataAPI());
    }

    private void createJobRequestWithDocuments(AiravataAPI airavataAPI) {
        // creating host description
        HostDescription descriptor = new HostDescription();
        descriptor.getType().setHostName("localhost");
        descriptor.getType().setHostAddress("127.0.0.1");
        try {
            airavataAPI.getApplicationManager().saveHostDescription(descriptor);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("Echo");
        serviceDescription.getType().setDescription("Echo service");
        // Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        // Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        // Setting input and output parameters to serviceDescriptor
        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription
                .getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue("EchoApplication");
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription("Echo", "localhost", applicationDeploymentDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //Using new airavata-api methods to store experiment metadata
        BasicMetadata basicMetadata = new BasicMetadata();
        basicMetadata.setExperimentName("test123");
        basicMetadata.setUserName("admin");
        basicMetadata.setUserNameIsSet(true);
        basicMetadata.setProjectID("default");

        AdvancedInputDataHandling advancedInputDataHandling = new AdvancedInputDataHandling();
        AdvancedOutputDataHandling advancedOutputDataHandling = new AdvancedOutputDataHandling();
        ComputationalResourceScheduling computationalResourceScheduling = new ComputationalResourceScheduling();
        QualityOfServiceParams qualityOfServiceParams = new QualityOfServiceParams();
        ConfigurationData configurationData = new ConfigurationData();


        HashMap<String, String> exInputs = new HashMap<String, String>();
        exInputs.put("echo_input", "echo_output=hello");

        configurationData.setExperimentInputs(exInputs);
        configurationData.setAdvanceInputDataHandling(advancedInputDataHandling);
        configurationData.setAdvanceOutputDataHandling(advancedOutputDataHandling);
        configurationData.setComputationalResourceScheduling(computationalResourceScheduling);
        configurationData.setQosParams(qualityOfServiceParams);
        configurationData.setApplicationId("Echo");

        Registry registry = new RegistryImpl();
        experimentID = (String) registry.add(ParentDataType.EXPERIMENT_BASIC_DATA, basicMetadata);
        registry.add(ChildDataType.EXPERIMENT_CONFIGURATION_DATA, configurationData, experimentID);
    }

    @Test
    public void noDescriptorTest() throws Exception {

        boolean b = orchestrator.launchExperiment(experimentID);

        if (b) {
            // This means orchestrator successfully accepted the job
            Assert.assertTrue(true);
        } else {
            Assert.assertFalse(true);
        }
    }

    private AiravataAPI getAiravataAPI() {
        AiravataAPI airavataAPI = null;
        if (airavataAPI == null) {
            try {
                String systemUserName = ServerSettings.getSystemUser();
                String gateway = ServerSettings.getSystemUserGateway();
                airavataAPI = AiravataAPIFactory.getAPI(gateway, systemUserName);
            } catch (ApplicationSettingsException e) {
                e.printStackTrace();
            } catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            }
        }
        return airavataAPI;
    }

}
