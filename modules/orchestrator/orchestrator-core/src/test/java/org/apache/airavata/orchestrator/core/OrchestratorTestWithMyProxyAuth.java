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
package org.apache.airavata.orchestrator.core;

public class OrchestratorTestWithMyProxyAuth extends BaseOrchestratorTest {
//    private static final Logger log = LoggerFactory.getLogger(NewOrchestratorTest.class);
//
//    private Orchestrator orchestrator;
//
//    private String experimentID;
//
//    private List<TaskDetails> tasks;
//
//    @BeforeTest
//    public void setUp() throws Exception {
//        AiravataUtils.setExecutionAsServer();
//        super.setUp();
//        orchestrator = new SimpleOrchestratorImpl();
////         System.setProperty("myproxy.username", "ogce");
////         System.setProperty("myproxy.password", "");
//         System.setProperty("trusted.cert.location", "/Users/lahirugunathilake/Downloads/certificates");
////        System.setProperty("trusted.cert.location",System.getProperty("gsi.working.directory"));
//    }
//
//    @Test
//    public void noDescriptorTest() throws Exception {
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
//                ExperimentModelUtil.createSimpleExperiment("default", "admin", "echoExperiment", "SimpleEcho2", "SimpleEcho2", exInputs);
//        simpleExperiment.setExperimentOutputs(exOut);
//
//        ComputationalResourceScheduling scheduling = ExperimentModelUtil.createComputationResourceScheduling("trestles.sdsc.edu", 1, 1, 1, "normal", 0, 0, 1, "sds128");
//        scheduling.setResourceHostId("gsissh-trestles");
//        UserConfigurationData userConfigurationData = new UserConfigurationData();
//        userConfigurationData.setAiravataAutoSchedule(false);
//        userConfigurationData.setOverrideManualScheduledParams(false);
//        userConfigurationData.setComputationalResourceScheduling(scheduling);
//        simpleExperiment.setUserConfigurationData(userConfigurationData);
//
//        WorkflowNodeDetails test = ExperimentModelUtil.createWorkflowNode("test", null);
//        Registry registry = RegistryFactory.getDefaultExpCatalog();
//        experimentID = (String) registry.add(ParentDataType.EXPERIMENT, simpleExperiment);
//        tasks = orchestrator.createTasks(experimentID);
//
//        for (TaskDetails taskDetail: tasks)
//        {
//            orchestrator.launchProcess(simpleExperiment,test, taskDetail,null);
//        }
//    }
}
