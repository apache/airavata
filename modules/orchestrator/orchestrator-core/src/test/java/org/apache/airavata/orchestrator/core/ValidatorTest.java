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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.orchestrator.core;
//
//import org.apache.airavata.common.utils.AiravataUtils;
//import org.apache.airavata.model.error.LaunchValidationException;
//import org.apache.airavata.model.util.ExperimentModelUtil;
//import org.apache.airavata.model.experiment.*;
//import org.apache.airavata.orchestrator.core.utils.OrchestratorConstants;
//import org.apache.airavata.orchestrator.cpi.Orchestrator;
//import org.apache.airavata.orchestrator.cpi.impl.SimpleOrchestratorImpl;
//import org.apache.airavata.registry.core.experiment.registry.jpa.impl.RegistryFactory;
//import org.apache.airavata.registry.cpi.ParentDataType;
//import org.apache.airavata.registry.cpi.Registry;
//import org.junit.Assert;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ValidatorTest extends BaseOrchestratorTest {
//    private static final Logger log = LoggerFactory.getLogger(NewOrchestratorTest.class);
//
//    private Orchestrator orchestrator;
//    private List<TaskDetails> tasks;
//
//    @BeforeTest
//    public void setUp() throws Exception {
//        AiravataUtils.setExecutionAsServer();
//        super.setUp();
//        System.setProperty(OrchestratorConstants.JOB_VALIDATOR,"org.apache.airavata.orchestrator.core.util.TestValidator,org.apache.airavata.orchestrator.core.util.SecondValidator");
//        System.setProperty("enable.validation", "true");
//        orchestrator = new SimpleOrchestratorImpl();
//    }
//
//
//
//    @Test
//    public void testValidator() throws Exception {
//          // creating host description
//        List<DataObjectType> exInputs = new ArrayList<DataObjectType>();
//        DataObjectType input = new DataObjectType();
//        input.setKey("echo_input");
//        input.setType(DataType.STRING);
//        input.setValue("echo_output=Hello World");
//        exInputs.add(input);
//
//        List<DataObjectType> exOut = new ArrayList<DataObjectType>();
//        DataObjectType output = new DataObjectType();
//        output.setKey("echo_output");
//        output.setType(DataType.STRING);
//        output.setValue("");
//        exOut.add(output);
//
//        Experiment simpleExperiment =
//                ExperimentModelUtil.createSimpleExperiment("default", "admin", "echoExperiment", "SimpleEcho0", "SimpleEcho0", exInputs);
//        simpleExperiment.setExperimentOutputs(exOut);
//
//        WorkflowNodeDetails test = ExperimentModelUtil.createWorkflowNode("test", null);
//        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("localhost", 1, 1, 1, "normal", 0, 0, 1, "sds128");
//        scheduling.setResourceHostId("localhost");
//        UserConfigurationData userConfigurationData = new UserConfigurationData();
//        userConfigurationData.setAiravataAutoSchedule(false);
//        userConfigurationData.setOverrideManualScheduledParams(false);
//        userConfigurationData.setComputationalResourceScheduling(scheduling);
//        simpleExperiment.setUserConfigurationData(userConfigurationData);
//
//        Registry defaultRegistry = RegistryFactory.getDefaultExpCatalog();
//        String experimentId = (String)defaultRegistry.add(ParentDataType.EXPERIMENT, simpleExperiment);
//
//        simpleExperiment.setExperimentID(experimentId);
//        tasks = orchestrator.createTasks(experimentId);
//
//        Assert.assertTrue(orchestrator.validateExperiment(simpleExperiment, test, tasks.get(0)).isValidationState());
//
//        simpleExperiment.setExperimentID(null);
//
//        try {
//            orchestrator.validateExperiment(simpleExperiment, test, tasks.get(0)).isValidationState();
//        }catch(LaunchValidationException e){
//            Assert.assertTrue(true);
//        }
//        tasks.get(0).setTaskID(null);
//        try {
//            orchestrator.validateExperiment(simpleExperiment, test, tasks.get(0));
//        }catch (LaunchValidationException e){
//            Assert.assertTrue(true);
//        }
//    }
//
//}
