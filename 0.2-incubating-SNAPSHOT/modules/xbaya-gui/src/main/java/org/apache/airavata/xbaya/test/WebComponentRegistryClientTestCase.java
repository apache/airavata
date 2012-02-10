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

package org.apache.airavata.xbaya.test;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.WebComponentRegistry;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlElement;

public class WebComponentRegistryClientTestCase extends XBayaTestCase {

    /**
     * MATH_ADDER_WSDL
     */
    private static final String MATH_ADDER_WSDL = "adder-wsdl.xml";

    /**
     * MATH_MULTIPLIER_WSDL
     */
    private static final String MATH_MULTIPLIER_WSDL = "multiplier-wsdl.xml";

    private Component inputComponent;

    private Component outputComponent;

    private WebComponentRegistry componentRegistry;

    private File temporaryDirectory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.temporaryDirectory = new File("tmp");
        this.temporaryDirectory.mkdir();

        this.componentRegistry = new WebComponentRegistry(XBayaConstants.DEFAULT_WEB_REGISTRY.toURL());
        this.componentRegistry.getComponentTree(); // To read components
        this.inputComponent = new InputComponent();
        this.outputComponent = new OutputComponent();
    }

    /**
     * @throws GraphException
     * @throws ComponentException
     * @throws IOException
     */
    public void testComplexMath() throws GraphException, ComponentException, IOException {

        Workflow workflow = createComplexMathWorkflow();
        File workflowFile = new File(this.temporaryDirectory, "web-complex-math.xwf");
        XMLUtil.saveXML(workflow.toXML(), workflowFile);

        // Load the same graph again from the file, saves it, and compare them.
        XmlElement workflowElement = XMLUtil.loadXML(workflowFile);
        Workflow workflow2 = new Workflow(workflowElement);
        File workflowFile2 = new File(this.temporaryDirectory, "web-complex-math-2.xwf");
        XMLUtil.saveXML(workflow2.toXML(), workflowFile2);

        String workflowFileString = IOUtil.readFileToString(workflowFile);
        String workflowFile2String = IOUtil.readFileToString(workflowFile2);
        assertEquals(workflowFileString, workflowFile2String);

        // Create a Jython script
        File jythonFile = new File(this.temporaryDirectory, "web-complex-math.py");
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        IOUtil.writeToFile(script.getJythonString(), jythonFile);
    }

    /**
     * @return The graph
     * @throws GraphException
     */
    private Workflow createComplexMathWorkflow() throws GraphException {

        Workflow workflow = new Workflow();

        // Name, description
        workflow.setName("Complex math workflow");
        workflow.setDescription("Complex math workflow");

        Graph graph = workflow.getGraph();

        // Adder nodes
        Component adderComp = this.componentRegistry.getComponents(MATH_ADDER_WSDL).get(0);

        Node adderNode1 = workflow.addNode(adderComp);
        adderNode1.setPosition(new Point(170, 50));

        Node adderNode2 = workflow.addNode(adderComp);
        adderNode2.setPosition(new Point(170, 210));

        // Multiplier node
        Component multiComp = this.componentRegistry.getComponents(MATH_MULTIPLIER_WSDL).get(0);

        Node multiNode = workflow.addNode(multiComp);
        multiNode.setPosition(new Point(320, 130));

        // Parameter node 1
        InputNode paramNode1 = (InputNode) workflow.addNode(this.inputComponent);
        paramNode1.setPosition(new Point(20, 30));
        String paramValue1 = "2";
        paramNode1.setDefaultValue(paramValue1);

        // Parameter node 2
        InputNode paramNode2 = (InputNode) workflow.addNode(this.inputComponent);
        paramNode2.setPosition(new Point(20, 100));
        String paramValue2 = "3";
        paramNode2.setDefaultValue(paramValue2);

        // Parameter node 3
        InputNode paramNode3 = (InputNode) workflow.addNode(this.inputComponent);
        paramNode3.setPosition(new Point(20, 170));
        String paramValue3 = "4";
        paramNode3.setDefaultValue(paramValue3);

        // Parameter node 4
        InputNode paramNode4 = (InputNode) workflow.addNode(this.inputComponent);
        paramNode4.setPosition(new Point(20, 240));
        String paramValue4 = "5";
        paramNode4.setDefaultValue(paramValue4);

        OutputNode outParamNode = (OutputNode) workflow.addNode(this.outputComponent);
        outParamNode.setPosition(new Point(370, 240));

        // Connect ports
        graph.addEdge(paramNode1.getOutputPort(0), adderNode1.getInputPort(0));
        graph.addEdge(paramNode2.getOutputPort(0), adderNode1.getInputPort(1));
        graph.addEdge(paramNode3.getOutputPort(0), adderNode2.getInputPort(0));
        graph.addEdge(paramNode4.getOutputPort(0), adderNode2.getInputPort(1));
        graph.addEdge(adderNode1.getOutputPort(0), multiNode.getInputPort(0));
        graph.addEdge(adderNode2.getOutputPort(0), multiNode.getInputPort(1));
        graph.addEdge(multiNode.getOutputPort(0), outParamNode.getInputPort(0));

        return workflow;
    }
}