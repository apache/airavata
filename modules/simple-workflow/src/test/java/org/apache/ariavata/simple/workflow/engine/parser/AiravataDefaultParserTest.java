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

package org.apache.ariavata.simple.workflow.engine.parser;

import junit.framework.Assert;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.ApplicationNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowOutputNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AiravataDefaultParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testWorkflowParse() throws Exception {
//        File jsonWfFile = new File("modules/simple-workflow/src/test/resources/ComplexMathWorkflow.awf");
        File jsonWfFile = new File("/Users/shameera/work/source/git_airavata/modules/simple-workflow/src/test/resources/ComplexMathWorkflow.awf");
        BufferedReader br = new BufferedReader(new FileReader(jsonWfFile));
        StringBuffer sb = new StringBuffer();
        String nextLine = br.readLine();
        while (nextLine != null) {
            sb.append(nextLine);
            nextLine = br.readLine();
        }

        Workflow workflow = new Workflow(sb.toString());
        AiravataDefaultParser parser = new AiravataDefaultParser("testExperimentId", "testCredentialId");
        Experiment experiment = new Experiment();
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
        parser.setExperiment(experiment);
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
            }else if (wfNode instanceof WorkflowOutputNode) {
                WorkflowOutputNode workflowOutputNode = (WorkflowOutputNode) wfNode;
                Assert.assertNotNull(workflowOutputNode.getInPort());
            }
        }

    }
}