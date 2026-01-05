/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.service.integration;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.UserConfigurationDataModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.orchestrator.OrchestratorService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.util.ExperimentModelUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class OrchestratorServiceIntegrationTest extends ServiceIntegrationTestBase {

    @MockitoBean
    private OrchestratorService orchestratorService;

    private final RegistryService registryService;
    private final AiravataServerProperties properties;

    public OrchestratorServiceIntegrationTest(RegistryService registryService, AiravataServerProperties properties) {
        this.registryService = registryService;
        this.properties = properties;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUpOrchestratorMock() throws OrchestratorException {
        // Configure mock to return true when launchExperiment is called
        Mockito.when(orchestratorService.launchExperiment(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(true);
    }

    private static int NUM_CONCURRENT_REQUESTS = 1;
    private static final Logger logger = LoggerFactory.getLogger(OrchestratorServiceIntegrationTest.class);

    @Test
    public void testStoreExperimentDetail() {
        for (int i = 0; i < NUM_CONCURRENT_REQUESTS; i++) {
            Thread thread = new Thread() {
                public void run() {
                    List<InputDataObjectType> exInputs = new ArrayList<InputDataObjectType>();
                    InputDataObjectType input = new InputDataObjectType();
                    input.setName("echo_input");
                    input.setType(DataType.STRING);
                    input.setValue("echo_output=Hello World");
                    exInputs.add(input);

                    List<OutputDataObjectType> exOut = new ArrayList<OutputDataObjectType>();
                    OutputDataObjectType output = new OutputDataObjectType();
                    output.setName("echo_output");
                    output.setType(DataType.STRING);
                    output.setValue("");
                    exOut.add(output);

                    String defaultGateway = properties.services.defaults.gateway;
                    ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(
                            defaultGateway,
                            "default",
                            "admin",
                            "echoExperiment",
                            "SimpleEcho2",
                            "SimpleEcho2",
                            exInputs);
                    simpleExperiment.setExperimentOutputs(exOut);

                    ComputationalResourceSchedulingModel scheduling =
                            ExperimentModelUtil.createComputationResourceScheduling(
                                    "trestles.sdsc.edu", 1, 1, 1, "normal", 0, 0);
                    scheduling.setResourceHostId("gsissh-trestles");
                    UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
                    userConfigurationDataModel.setComputationalResourceScheduling(scheduling);
                    simpleExperiment.setUserConfigurationData(userConfigurationDataModel);

                    String expId = null;
                    try {
                        expId = registryService.createExperiment(defaultGateway, simpleExperiment);
                    } catch (RegistryServiceException e) {
                        logger.error("Error while creating experiment", e);
                        Assertions.fail("Error while creating experiment");
                    }
                    Assertions.assertNotNull(expId, "Experiment ID should not be null");

                    try {
                        orchestratorService.launchExperiment(expId, defaultGateway, null);
                    } catch (OrchestratorException e) {
                        logger.error("Error while launching experiment", e);
                        Assertions.fail("Error while launching experiment");
                    }
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("Error while joining thread", e);
                Assertions.fail("Error while joining thread");
            }
        }
    }
}
