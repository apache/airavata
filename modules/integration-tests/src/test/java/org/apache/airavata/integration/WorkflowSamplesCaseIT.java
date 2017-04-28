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
package org.apache.airavata.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test class.
 */
public class WorkflowSamplesCaseIT extends WorkflowIntegrationTestBase {

    private final Logger log = LoggerFactory.getLogger(WorkflowSamplesCaseIT.class);

    public WorkflowSamplesCaseIT() throws Exception {
//        setUpEnvironment();
    }

//    @BeforeTest
//    public void setUp() throws Exception {
//        this.airavataAPI = getAiravataAPI();
//    }

//    @Test(groups = {"workflowSamplesGroup"}/*, dependsOnGroups = { "forEachGroup" }*/)
//    public void testWorkflowSamples() throws Exception {
//        log("Running tests .............................");
//        executeExperiment("target/samples/workflows/SimpleEcho.xwf", Arrays.asList("Test_Value"), "Test_Value");
//        executeExperiment("target/samples/workflows/LevenshteinDistance.xwf", Arrays.asList("abc", "def"), Arrays.asList("3"));
//        executeExperiment("target/samples/workflows/SimpleForEach.xwf", Arrays.asList("1,2","3,4"), Arrays.asList("4","6"));
//        executeExperiment("target/samples/workflows/ComplexMath.xwf", Arrays.asList("15","16","18","21","25","30","36","43"), "5554");
//		executeExperiment("target/samples/workflows/SimpleMath.xwf", Arrays.asList("15","16","18","21","25","30","36","43"), "204");
//		executeExperiment("target/samples/workflows/ComplexForEach.xwf", Arrays.asList("1,2","3,4","5,6","7,8","9,10","11,12","13,14","15,16"), Arrays.asList("2027025","10321920"));
//    }

//    private void executeExperiment(String workflowFilePath,
//                                   List<String> inputs, Object outputs) throws GraphException,
//            ComponentException, IOException, WorkflowAlreadyExistsException,
//            AiravataAPIInvocationException, Exception {
//        log("Saving workflow ...");
//
//        Workflow workflow = new Workflow(getWorkflowComposeContent(workflowFilePath));
//        if (!airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName())) {
//            airavataAPI.getWorkflowManager().addWorkflow(workflow);
//        }
//        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));
//
//        log("Workflow setting up completed ...");
//
//        runWorkFlow(workflow, inputs, outputs);
//    }
//
//    protected void runWorkFlow(Workflow workflow, List<String> inputValues, Object outputValue) throws Exception {
//        List<WorkflowInput> workflowInputs = setupInputs(workflow, inputValues);
//        String workflowName = workflow.getName();
//        ExperimentAdvanceOptions options = airavataAPI.getExecutionManager().createExperimentAdvanceOptions(
//                workflowName, getUserName(), null);
//
//        String experimentId = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, options);
//
//        Assert.assertNotNull(experimentId);
//
//        log.info("Run workflow completed ....");
//
//        airavataAPI.getExecutionManager().waitForExperimentTermination(experimentId);
//        verifyOutput(experimentId, outputValue);
//    }
//
//    protected void verifyOutput(String experimentId, Object outputVerifyingString) throws Exception {
//        log.info("Experiment ID Returned : " + experimentId);
//
//        ExperimentData experimentData = airavataAPI.getProvenanceManager().getExperimentData(experimentId);
//
//        log.info("Verifying output ...");
//
//        List<WorkflowExecutionDataImpl> workflowInstanceData = experimentData.getWorkflowExecutionDataList();
//
//        Assert.assertFalse("Workflow instance data cannot be empty !", workflowInstanceData.isEmpty());
//
//        for (WorkflowExecutionDataImpl data : workflowInstanceData) {
//            List<NodeExecutionData> nodeDataList = data.getNodeDataList(WorkflowNode.OUTPUTNODE);
//            Assert.assertFalse("Node execution data list cannot be empty !", nodeDataList.isEmpty());
//            for (NodeExecutionData nodeData : nodeDataList) {
//                for (InputData inputData : nodeData.getInputData()) {
//                    if (outputVerifyingString instanceof List) {
//                        @SuppressWarnings("unchecked")
//                        List<String> outputs = (List<String>) outputVerifyingString;
//                        String[] outputValues = StringUtil.getElementsFromString(inputData.getValue());
//                        Assert.assertEquals(outputs.size(), outputValues.length);
//                        for (int i = 0; i < outputValues.length; i++) {
//                            Assert.assertEquals(outputs.get(i), outputValues[i]);
//                        }
//                    } else {
//                        Assert.assertEquals(outputVerifyingString.toString(), inputData.getValue());
//                    }
//
//                }
//            }
//        }
//    }

}
