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
package org.apache.airavata.api.thrift.server;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.OrchestratorService;
import org.apache.airavata.service.RegistryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class TestOrchestratorServiceServer {

    @Autowired
    private OrchestratorService orchestratorService;

    @Autowired
    private RegistryService registryService;

    private static int NUM_CONCURRENT_REQUESTS = 1;
    private static final String DEFAULT_GATEWAY = "default.registry.gateway";
    private static final Logger logger = LoggerFactory.getLogger(TestOrchestratorServiceServer.class);

    public static void main(String[] args) {
        try {
            new OrchestratorServiceServer().start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

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

                    ExperimentModel simpleExperiment = ExperimentModelUtil.createSimpleExperiment(
                            DEFAULT_GATEWAY,
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
                        expId = registryService.createExperiment(DEFAULT_GATEWAY, simpleExperiment);
                    } catch (RegistryServiceException e) {
                        logger.error("Error while creating experiment", e);
                        Assertions.fail("Error while creating experiment");
                    }
                    Assertions.assertNotNull(expId, "Experiment ID should not be null");

                    try {
                        orchestratorService.launchExperiment(expId, DEFAULT_GATEWAY, null);
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
