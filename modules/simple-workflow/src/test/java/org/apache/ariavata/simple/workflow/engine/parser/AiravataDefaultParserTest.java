package org.apache.ariavata.simple.workflow.engine.parser;

import junit.framework.Assert;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowInputNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AiravataDefaultParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testWorkflowParse() throws Exception {
        File jsonWfFile = new File("modules/simple-workflow/src/test/resources/ComplexMathWorkflow.awf");
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

    }
}