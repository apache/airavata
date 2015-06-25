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

package org.apache.airavata.workflow.core.parser;

import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowInputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowOutputNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiravataWorkflowParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testWorkflowParse() throws Exception {
        Assert.assertNotNull("Test file (ComplexMathWorkflow.awf) is missing", getClass().getResource("/ComplexMathWorkflow.awf"));
        InputStreamReader isr = new InputStreamReader(this.getClass().getResourceAsStream("/ComplexMathWorkflow.awf"));
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        String nextLine = br.readLine();
        while (nextLine != null) {
            sb.append(nextLine);
            nextLine = br.readLine();
        }
        Workflow workflow = new Workflow(sb.toString());
        ExperimentModel experiment = new ExperimentModel();
        InputDataObjectType x = new InputDataObjectType();
        x.setValue("6");
        x.setType(DataType.STRING);
        x.setName("x");

        InputDataObjectType y = new InputDataObjectType();
        y.setValue("8");
        y.setType(DataType.STRING);
        y.setName("y");

        InputDataObjectType z = new InputDataObjectType();
        z.setValue("10");
        z.setType(DataType.STRING);
        z.setName("y_2");

        List<InputDataObjectType> inputs = new ArrayList<InputDataObjectType>();
        inputs.add(x);
        inputs.add(y);
        inputs.add(z);
        experiment.setExperimentInputs(inputs);
        // create parser
        AiravataWorkflowParser parser = new AiravataWorkflowParser(experiment, "testCredentialId");
        List<WorkflowInputNode> workflowInputNodes = parser.parseWorkflow(workflow);
        Assert.assertNotNull(workflowInputNodes);
        Assert.assertEquals(3, workflowInputNodes.size());
        for (WorkflowInputNode workflowInputNode : workflowInputNodes) {
            Assert.assertNotNull(workflowInputNode.getOutPort());
            Assert.assertNotNull(workflowInputNode.getInputObject());
        }

        Map<String, WorkflowNode> wfNodes = parser.getWfNodes();
        for (String wfId : wfNodes.keySet()) {
            WorkflowNode wfNode = wfNodes.get(wfId);
            if (wfNode instanceof ApplicationNode) {
                ApplicationNode node = (ApplicationNode) wfNode;
                Assert.assertEquals(2, node.getInputPorts().size());
                Assert.assertNotNull(node.getInputPorts().get(0).getInputObject());
                Assert.assertNotNull(node.getInputPorts().get(1).getInputObject());
                Assert.assertNotNull(node.getInputPorts().get(0).getEdge());
                Assert.assertNotNull(node.getInputPorts().get(1).getEdge());

                Assert.assertEquals(1, node.getOutputPorts().size());
                Assert.assertEquals(1, node.getOutputPorts().get(0).getOutEdges().size());
                Assert.assertNotNull(node.getOutputPorts().get(0).getOutEdges().get(0));
            } else if (wfNode instanceof WorkflowOutputNode) {
                WorkflowOutputNode workflowOutputNode = (WorkflowOutputNode) wfNode;
                Assert.assertNotNull(workflowOutputNode.getInPort());
            }
        }

    }
}