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

public class OrchestratorTestWithGRAM extends BaseOrchestratorTest {
//    private static final Logger log = LoggerFactory.getLogger(OrchestratorTestWithGRAM.class);
//
//    private Orchestrator orchestrator;
//
//    private String experimentID;
//
//    @BeforeTest
//    public void setUp() throws Exception {
//        AiravataUtils.setExecutionAsServer();
//        super.setUp();
//        orchestrator = new SimpleOrchestratorImpl();
//        createJobRequestWithDocuments();
//    }
//
//    private void createJobRequestWithDocuments() throws Exception{
//        //Using new airavata-api methods to store experiment metadata
////        BasicMetadata basicMetadata = new BasicMetadata();
////        basicMetadata.setExperimentName("test-trestles");
////        basicMetadata.setUserName("admin");
////        basicMetadata.setUserNameIsSet(true);
////        basicMetadata.setProjectID("default");
////
////        AdvancedInputDataHandling advancedInputDataHandling = new AdvancedInputDataHandling();
////        AdvancedOutputDataHandling advancedOutputDataHandling = new AdvancedOutputDataHandling();
////        ComputationalResourceScheduling computationalResourceScheduling = new ComputationalResourceScheduling();
////        QualityOfServiceParams qualityOfServiceParams = new QualityOfServiceParams();
////        ConfigurationData configurationData = new ConfigurationData();
////
////        HashMap<String, String> exInputs = new HashMap<String, String>();
////        exInputs.put("echo_input", "echo_output=hello");
////
////        configurationData.setExperimentInputs(exInputs);
////        configurationData.setAdvanceInputDataHandling(advancedInputDataHandling);
////        configurationData.setAdvanceOutputDataHandling(advancedOutputDataHandling);
////        configurationData.setComputationalResourceScheduling(computationalResourceScheduling);
////        configurationData.setQosParams(qualityOfServiceParams);
////        configurationData.setApplicationId("SimpleEcho1");
////
////        Registry registry = new RegistryImpl();
////        experimentID = (String) registry.add(ParentDataType.EXPERIMENT, basicMetadata);
////        registry.add(ChildDataType.EXPERIMENT_CONFIGURATION_DATA, configurationData, experimentID);
//    }
//
//    @Test
//    public void noDescriptorTest() throws Exception {
//
////        boolean b = orchestrator.launchProcess(experimentID);
////
////        if (b) {
////            Assert.assertTrue(true);
////        } else {
////            Assert.assertFalse(true);
////        }
//    }



}
