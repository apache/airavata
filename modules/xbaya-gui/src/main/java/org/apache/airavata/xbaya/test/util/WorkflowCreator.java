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
// */
//
//package org.apache.airavata.xbaya.test.util;
//
//import java.awt.Point;
//
//import org.apache.airavata.workflow.model.component.Component;
//import org.apache.airavata.workflow.model.component.ComponentException;
//import org.apache.airavata.workflow.model.component.ComponentRegistryException;
//import org.apache.airavata.workflow.model.component.local.LocalComponentRegistry;
//import org.apache.airavata.workflow.model.component.system.ConstantComponent;
//import org.apache.airavata.workflow.model.component.system.DoWhileComponent;
//import org.apache.airavata.workflow.model.component.system.EndDoWhileComponent;
//import org.apache.airavata.workflow.model.component.system.EndForEachComponent;
//import org.apache.airavata.workflow.model.component.system.EndifComponent;
//import org.apache.airavata.workflow.model.component.system.ForEachComponent;
//import org.apache.airavata.workflow.model.component.system.IfComponent;
//import org.apache.airavata.workflow.model.component.system.InputComponent;
//import org.apache.airavata.workflow.model.component.system.OutputComponent;
//import org.apache.airavata.workflow.model.component.system.ReceiveComponent;
//import org.apache.airavata.workflow.model.graph.Graph;
//import org.apache.airavata.workflow.model.graph.GraphException;
//import org.apache.airavata.workflow.model.graph.Node;
//import org.apache.airavata.workflow.model.graph.system.ConstantNode;
//import org.apache.airavata.workflow.model.graph.system.DoWhileNode;
//import org.apache.airavata.workflow.model.graph.system.IfNode;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.graph.system.OutputNode;
//import org.apache.airavata.workflow.model.graph.system.ReceiveNode;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.xbaya.file.XBayaPathConstants;
//import org.apache.airavata.xbaya.test.service.adder.Adder;
//import org.apache.airavata.xbaya.test.service.approver.Approver;
//import org.apache.airavata.xbaya.test.service.arrayadder.ArrayAdder;
//import org.apache.airavata.xbaya.test.service.arraygen.ArrayGenerator;
//import org.apache.airavata.xbaya.test.service.echo.Echo;
//import org.apache.airavata.xbaya.test.service.multiplier.Multiplier;
//
//public class WorkflowCreator {
//
//    /**
//     * GFAC_TEST_AWSDL
//     */
//    public static final String GFAC_TEST_AWSDL = "TestCMD_Example1_AWSDL.xml";
//
//    private Component inputComponent;
//
//    private Component outputComponent;
//
//    private LocalComponentRegistry componentRegistry;
//
//    private ConstantComponent constantComponent;
//
//    private ForEachComponent splitComponent;
//
//    private EndForEachComponent mergeComponent;
//
//    private IfComponent ifComponent;
//
//    private EndifComponent endifComponent;
//
//    private ReceiveComponent receiveComponent;
//
//    private DoWhileComponent doWhileComponent;
//
//    private EndDoWhileComponent endDoWhileComponent;
//
//    /**
//     * Constructs a WorkflowCreator.
//     */
//    public WorkflowCreator() {
//        this.componentRegistry = new LocalComponentRegistry(XBayaPathConstants.WSDL_DIRECTORY);
//        this.inputComponent = new InputComponent();
//        this.outputComponent = new OutputComponent();
//        this.constantComponent = new ConstantComponent();
//        this.splitComponent = new ForEachComponent();
//        this.mergeComponent = new EndForEachComponent();
//        this.ifComponent = new IfComponent();
//        this.endifComponent = new EndifComponent();
//        this.receiveComponent = new ReceiveComponent();
//        this.doWhileComponent = new DoWhileComponent();
//        this.endDoWhileComponent = new EndDoWhileComponent();
//    }
//
//    /**
//     * @return The graph
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createSimpleMathWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Simple math workflow");
//        workflow.setDescription("Simple math workflow");
//
//        Graph graph = workflow.getGraph();
//
//        // Adder node
//        Component adderComp = this.componentRegistry.getComponent(Adder.WSDL_PATH);
//        Node adderNode = workflow.addNode(adderComp);
//        adderNode.setPosition(new Point(250, 100));
//
//        // Input parameter node 1
//        InputNode paramNode1 = (InputNode) workflow.addNode(this.inputComponent);
//        paramNode1.setPosition(new Point(50, 50));
//
//        // Input parameter node 2
//        InputNode paramNode2 = (InputNode) workflow.addNode(this.inputComponent);
//        paramNode2.setPosition(new Point(50, 120));
//
//        // Output parameter
//        OutputNode outParamNode = (OutputNode) workflow.addNode(this.outputComponent);
//        outParamNode.setPosition(new Point(300, 220));
//
//        // Connect ports
//        graph.addEdge(paramNode1.getOutputPort(0), adderNode.getInputPort(0));
//        graph.addEdge(paramNode2.getOutputPort(0), adderNode.getInputPort(1));
//        graph.addEdge(adderNode.getOutputPort(0), outParamNode.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        String paramValue1 = "2";
//        paramNode1.setDefaultValue(paramValue1);
//        String paramValue2 = "3";
//        paramNode2.setDefaultValue(paramValue2);
//        return workflow;
//    }
//
//    /**
//     * @return The graph
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createMathWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Math workflow");
//        workflow.setDescription("A workflow that calculates (a + b) * c.");
//
//        Graph graph = workflow.getGraph();
//
//        // Adder node
//        Component adderComp = this.componentRegistry.getComponent(Adder.WSDL_PATH);
//
//        Node adderNode1 = workflow.addNode(adderComp);
//        adderNode1.setPosition(new Point(170, 50));
//
//        // Multiplier node
//        Component multiComp = this.componentRegistry.getComponent(Multiplier.WSDL_PATH);
//
//        Node multiNode = workflow.addNode(multiComp);
//        multiNode.setPosition(new Point(320, 130));
//
//        // Input node 1
//        InputNode inputNode1 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode1.setPosition(new Point(20, 30));
//
//        // Input node 2
//        InputNode inputNode2 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode2.setPosition(new Point(20, 100));
//
//        // Input node 3
//        InputNode inputNode3 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode3.setPosition(new Point(20, 170));
//
//        // Output
//        OutputNode outputNode = (OutputNode) workflow.addNode(this.outputComponent);
//        outputNode.setPosition(new Point(500, 130));
//
//        // Connect ports
//        graph.addEdge(inputNode1.getOutputPort(0), adderNode1.getInputPort(0));
//        graph.addEdge(inputNode2.getOutputPort(0), adderNode1.getInputPort(1));
//        graph.addEdge(adderNode1.getOutputPort(0), multiNode.getInputPort(0));
//        graph.addEdge(inputNode3.getOutputPort(0), multiNode.getInputPort(1));
//        graph.addEdge(multiNode.getOutputPort(0), outputNode.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        inputNode1.setConfiguredName("a");
//        inputNode2.setConfiguredName("b");
//        inputNode3.setConfiguredName("c");
//        inputNode1.setConfigured(true);
//        inputNode2.setConfigured(true);
//        inputNode3.setConfigured(true);
//        inputNode1.setDefaultValue("2");
//        inputNode2.setDefaultValue("3");
//        inputNode3.setDefaultValue("4");
//        outputNode.setConfiguredName("z");
//        outputNode.setConfigured(true);
//
//        return workflow;
//    }
//
//    /**
//     * @return The graph
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createComplexMathWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Complex math workflow");
//        workflow.setDescription("Complex math workflow");
//
//        Graph graph = workflow.getGraph();
//
//        // Adder nodes
//        Component adderComp = this.componentRegistry.getComponent(Adder.WSDL_PATH);
//
//        Node adderNode1 = workflow.addNode(adderComp);
//        adderNode1.setPosition(new Point(170, 50));
//
//        Node adderNode2 = workflow.addNode(adderComp);
//        adderNode2.setPosition(new Point(170, 210));
//
//        // Multiplier node
//        Component multiComp = this.componentRegistry.getComponent(Multiplier.WSDL_PATH);
//
//        Node multiNode = workflow.addNode(multiComp);
//        multiNode.setPosition(new Point(320, 130));
//
//        // Input node 1
//        InputNode inputNode1 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode1.setPosition(new Point(20, 30));
//
//        // Input node 2
//        InputNode inputNode2 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode2.setPosition(new Point(20, 100));
//
//        // Input node 3
//        InputNode inputNode3 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode3.setPosition(new Point(20, 170));
//
//        // Input node 4
//        InputNode inputNode4 = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode4.setPosition(new Point(20, 240));
//
//        // Output
//        OutputNode outputNode = (OutputNode) workflow.addNode(this.outputComponent);
//        outputNode.setPosition(new Point(500, 130));
//
//        // Connect ports
//        graph.addEdge(inputNode1.getOutputPort(0), adderNode1.getInputPort(0));
//        graph.addEdge(inputNode2.getOutputPort(0), adderNode1.getInputPort(1));
//        graph.addEdge(inputNode3.getOutputPort(0), adderNode2.getInputPort(0));
//        graph.addEdge(inputNode4.getOutputPort(0), adderNode2.getInputPort(1));
//        graph.addEdge(adderNode1.getOutputPort(0), multiNode.getInputPort(0));
//        graph.addEdge(adderNode2.getOutputPort(0), multiNode.getInputPort(1));
//        graph.addEdge(multiNode.getOutputPort(0), outputNode.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        inputNode1.setConfiguredName("a");
//        inputNode2.setConfiguredName("b");
//        inputNode3.setConfiguredName("c");
//        inputNode4.setConfiguredName("d");
//        inputNode1.setDescription("This is the first input.");
//        inputNode2.setDescription("This is the second input.");
//        inputNode3.setDescription("This is the third input.");
//        inputNode4.setDescription("This is the fourth input.");
//        inputNode1.setConfigured(true);
//        inputNode2.setConfigured(true);
//        inputNode3.setConfigured(true);
//        inputNode4.setConfigured(true);
//        inputNode1.setDefaultValue("2");
//        inputNode2.setDefaultValue("3");
//        inputNode3.setDefaultValue("4");
//        inputNode4.setDefaultValue("5");
//        outputNode.setConfiguredName("z");
//        outputNode.setConfigured(true);
//
//        return workflow;
//    }
//
//    /**
//     * @return The graph
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createMathWithConstWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Math with const");
//        workflow.setDescription("Math with const");
//
//        Graph graph = workflow.getGraph();
//
//        // Adder node
//        Component adderComp = this.componentRegistry.getComponent(Adder.WSDL_PATH);
//        Node adderNode = workflow.addNode(adderComp);
//        adderNode.setPosition(new Point(250, 100));
//
//        // Input parameter node
//        InputNode inputNode = (InputNode) workflow.addNode(this.inputComponent);
//        inputNode.setPosition(new Point(50, 50));
//
//        // Constant node
//        ConstantNode constantNode = (ConstantNode) workflow.addNode(this.constantComponent);
//        constantNode.setPosition(new Point(50, 120));
//
//        // Output parameter
//        OutputNode outParamNode = (OutputNode) workflow.addNode(this.outputComponent);
//        outParamNode.setPosition(new Point(300, 220));
//
//        // Connect ports
//        graph.addEdge(inputNode.getOutputPort(0), adderNode.getInputPort(0));
//        graph.addEdge(constantNode.getOutputPort(0), adderNode.getInputPort(1));
//        graph.addEdge(adderNode.getOutputPort(0), outParamNode.getInputPort(0));
//
//        // Set the default value of an input and the constant.
//        // This needs to be after connection.
//        String paramValue1 = "2";
//        inputNode.setDefaultValue(paramValue1);
//        String paramValue2 = "3";
//        constantNode.setValue(paramValue2);
//        return workflow;
//    }
//
//    /**
//     * @return The workflow
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createArrayWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Array test");
//        workflow.setDescription("A workflow that tests arrays");
//
//        Graph graph = workflow.getGraph();
//
//        // n
//        InputNode inputN = (InputNode) workflow.addNode(this.inputComponent);
//        inputN.setPosition(new Point(0, 80));
//
//        // Array generator
//        Component arrayGeneratorComponent = this.componentRegistry.getComponent(ArrayGenerator.WSDL_PATH);
//        Node arrayGenerator = workflow.addNode(arrayGeneratorComponent);
//        arrayGenerator.setPosition(new Point(150, 80));
//
//        // Array adder
//        Component arrayAdderComponent = this.componentRegistry.getComponent(ArrayAdder.WSDL_PATH);
//        Node arrayAdder = workflow.addNode(arrayAdderComponent);
//        arrayAdder.setPosition(new Point(400, 80));
//
//        // Output
//        OutputNode output = (OutputNode) workflow.addNode(this.outputComponent);
//        output.setConfiguredName("output");
//        output.setPosition(new Point(550, 80));
//
//        // Connect ports
//        graph.addEdge(inputN.getOutputPort(0), arrayGenerator.getInputPort(0));
//        graph.addEdge(arrayGenerator.getOutputPort(0), arrayAdder.getInputPort(0));
//        graph.addEdge(arrayAdder.getOutputPort(0), output.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        String n = "5";
//        inputN.setDefaultValue(n);
//
//        return workflow;
//    }
//
//    /**
//     * @return The workflow
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createForEachWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("ForEach test");
//        workflow.setDescription("Workflow that tests if");
//
//        Graph graph = workflow.getGraph();
//
//        // x
//        InputNode inputX = (InputNode) workflow.addNode(this.inputComponent);
//        inputX.setPosition(new Point(0, 0));
//
//        // n
//        InputNode inputN = (InputNode) workflow.addNode(this.inputComponent);
//        inputN.setPosition(new Point(0, 80));
//
//        // Array generator
//        Component arrayGeneratorComponent = this.componentRegistry.getComponent(ArrayGenerator.WSDL_PATH);
//        Node arrayGenerator = workflow.addNode(arrayGeneratorComponent);
//        arrayGenerator.setPosition(new Point(120, 80));
//
//        // Split
//        Node split = workflow.addNode(this.splitComponent);
//        split.setPosition(new Point(310, 80));
//
//        // Adder
//        Component adderComponent = this.componentRegistry.getComponent(Adder.WSDL_PATH);
//        Node adder = workflow.addNode(adderComponent);
//        adder.setPosition(new Point(440, 40));
//
//        // Merge
//        Node merge = workflow.addNode(this.mergeComponent);
//        merge.setPosition(new Point(580, 40));
//
//        // Output
//        OutputNode output = (OutputNode) workflow.addNode(this.outputComponent);
//        output.setConfiguredName("output");
//        output.setPosition(new Point(720, 40));
//
//        // Connect ports
//        graph.addEdge(inputX.getOutputPort(0), adder.getInputPort(0));
//        graph.addEdge(inputN.getOutputPort(0), arrayGenerator.getInputPort(0));
//        graph.addEdge(arrayGenerator.getOutputPort(0), split.getInputPort(0));
//        graph.addEdge(split.getOutputPort(0), adder.getInputPort(1));
//        graph.addEdge(adder.getOutputPort(0), merge.getInputPort(0));
//        graph.addEdge(merge.getOutputPort(0), output.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        String x = "2";
//        inputX.setDefaultValue(x);
//        String n = "3";
//        inputN.setDefaultValue(n);
//
//        return workflow;
//    }
//
//    /**
//     * @return The workflow
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     * @throws ComponentException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createIfWorkflow() throws GraphException, ComponentException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("If test");
//        workflow.setDescription("Workflow that tests if");
//
//        Graph graph = workflow.getGraph();
//
//        // x
//        InputNode x = (InputNode) workflow.addNode(this.inputComponent);
//        x.setPosition(new Point(10, 10));
//
//        // y
//        InputNode y = (InputNode) workflow.addNode(this.inputComponent);
//        y.setPosition(new Point(10, 90));
//
//        // const0
//        ConstantNode const0 = (ConstantNode) workflow.addNode(this.constantComponent);
//        const0.setPosition(new Point(20, 180));
//
//        // if
//        IfNode ifNode = (IfNode) workflow.addNode(this.ifComponent);
//        ifNode.setPosition(new Point(170, 180));
//
//        // Adder nodes
//        Component adderComp = this.componentRegistry.getComponent(Adder.WSDL_PATH);
//
//        Node adder = workflow.addNode(adderComp);
//        adder.setPosition(new Point(400, 10));
//
//        // Multiplier node
//        Component multiComp = this.componentRegistry.getComponent(Multiplier.WSDL_PATH);
//
//        Node multiplier = workflow.addNode(multiComp);
//        multiplier.setPosition(new Point(400, 90));
//
//        // endif
//        Node endif = workflow.addNode(this.endifComponent);
//        endif.setPosition(new Point(550, 40));
//
//        // Output
//        OutputNode output = (OutputNode) workflow.addNode(this.outputComponent);
//        output.setConfiguredName("output");
//        output.setPosition(new Point(700, 40));
//
//        // Connect ports
//        graph.addEdge(x.getOutputPort(0), adder.getInputPort(0));
//        graph.addEdge(x.getOutputPort(0), multiplier.getInputPort(0));
//        graph.addEdge(y.getOutputPort(0), adder.getInputPort(1));
//        graph.addEdge(y.getOutputPort(0), multiplier.getInputPort(1));
//        graph.addEdge(const0.getOutputPort(0), ifNode.getInputPort(0));
//        graph.addEdge(ifNode.getControlOutPorts().get(0), adder.getControlInPort());
//        graph.addEdge(ifNode.getControlOutPorts().get(1), multiplier.getControlInPort());
//        graph.addEdge(adder.getOutputPort(0), endif.getInputPort(0));
//        graph.addEdge(multiplier.getOutputPort(0), endif.getInputPort(1));
//        graph.addEdge(endif.getOutputPort(0), output.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        x.setDefaultValue("2");
//        y.setDefaultValue("3");
//        const0.setValue("adder");
//        ifNode.setXPath("$0 = 'adder'");
//
//        return workflow;
//    }
//
//    /**
//     * @return The workflow
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     * @throws ComponentException
//     */
//    public Workflow createReceiveWorkflow() throws GraphException, ComponentException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Receive test");
//        workflow.setDescription("Workflow that tests receive");
//
//        Graph graph = workflow.getGraph();
//
//        // Adder nodes
//        Component echoComponent = this.componentRegistry.getComponent(Echo.WSDL_PATH);
//
//        Node echo = workflow.addNode(echoComponent);
//        echo.setPosition(new Point(40, 40));
//
//        // receive
//        ReceiveNode receive = (ReceiveNode) workflow.addNode(this.receiveComponent);
//        receive.setPosition(new Point(200, 200));
//
//        // Output
//        OutputNode output1 = (OutputNode) workflow.addNode(this.outputComponent);
//        output1.setPosition(new Point(350, 40));
//
//        OutputNode output2 = (OutputNode) workflow.addNode(this.outputComponent);
//        output2.setPosition(new Point(350, 200));
//
//        // Connect ports
//        graph.addEdge(receive.getEPRPort(), echo.getInputPort(0));
//        graph.addEdge(echo.getOutputPort(0), output1.getInputPort(0));
//        graph.addEdge(receive.getOutputPort(0), output2.getInputPort(0));
//        graph.addEdge(echo.getControlOutPorts().get(0), receive.getControlInPort());
//
//        // Confugure
//        output1.setConfiguredName("output1");
//        output1.setConfigured(true);
//        output2.setConfiguredName("output2");
//        output2.setConfigured(true);
//
//        return workflow;
//
//    }
//
//    /**
//     * @return The graph
//     * @throws ComponentException
//     * @throws GraphException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createGFacWorkflow() throws ComponentException, GraphException, ComponentRegistryException {
//
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("GFac test workflow");
//        workflow.setDescription("GFac test workflow");
//
//        Graph graph = workflow.getGraph();
//
//        // Adder node
//        Component gfacComp = this.componentRegistry.getComponent(GFAC_TEST_AWSDL);
//        Node gfacNode = workflow.addNode(gfacComp);
//        gfacNode.setPosition(new Point(250, 100));
//
//        // Input parameter node 1
//        InputNode paramNode1 = (InputNode) workflow.addNode(this.inputComponent);
//        paramNode1.setPosition(new Point(50, 50));
//        String paramValue1 = "300";
//        paramNode1.setDefaultValue(paramValue1);
//
//        // Output parameter
//        OutputNode outParamNode = (OutputNode) workflow.addNode(this.outputComponent);
//        outParamNode.setPosition(new Point(300, 220));
//
//        // Connect ports
//        graph.addEdge(paramNode1.getOutputPort(0), gfacNode.getInputPort(0));
//        graph.addEdge(gfacNode.getOutputPort(0), outParamNode.getInputPort(0));
//
//        return workflow;
//    }
//
//    /**
//     * @return The workflow created.
//     * @throws GraphException
//     * @throws ComponentException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createLoanWorkflow() throws GraphException, ComponentException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Loan Approval");
//        workflow.setDescription("Loan Approval");
//
//        Graph graph = workflow.getGraph();
//
//        // amount
//        InputNode amount = (InputNode) workflow.addNode(this.inputComponent);
//        amount.setPosition(new Point(10, 10));
//
//        // if
//        IfNode ifNode = (IfNode) workflow.addNode(this.ifComponent);
//        ifNode.setPosition(new Point(200, 100));
//
//        // Approver nodes
//        Component approverComponent = this.componentRegistry.getComponent(Approver.WSDL_PATH);
//
//        Node approver = workflow.addNode(approverComponent);
//        approver.setPosition(new Point(350, 10));
//
//        // const
//        ConstantNode constYes = (ConstantNode) workflow.addNode(this.constantComponent);
//        constYes.setPosition(new Point(350, 200));
//
//        // endif
//        Node endif = workflow.addNode(this.endifComponent);
//        endif.setPosition(new Point(550, 100));
//
//        // Output
//        OutputNode output = (OutputNode) workflow.addNode(this.outputComponent);
//        output.setPosition(new Point(700, 100));
//
//        // Connect ports
//        graph.addEdge(amount.getOutputPort(0), approver.getInputPort(0));
//        graph.addEdge(amount.getOutputPort(0), ifNode.getInputPort(0));
//        graph.addEdge(ifNode.getControlOutPorts().get(0), approver.getControlInPort());
//        graph.addEdge(ifNode.getControlOutPorts().get(1), constYes.getControlInPort());
//        graph.addEdge(approver.getOutputPort(0), endif.getInputPort(0));
//        graph.addEdge(constYes.getOutputPort(0), endif.getInputPort(1));
//        graph.addEdge(endif.getOutputPort(0), output.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        amount.setDefaultValue("500");
//        constYes.setValue("Yes");
//        ifNode.setXPath("$0 > 1000");
//        output.setConfiguredName("accept");
//        output.setConfigured(true);
//
//        return workflow;
//    }
//    /**
//     * Create a dowhile workflow
//     * @return Workflow created.
//     * @throws GraphException
//     * @throws ComponentException
//     * @throws ComponentRegistryException
//     */
//    public Workflow createDoWhileWorkflow() throws GraphException, ComponentException, ComponentRegistryException {
//        Workflow workflow = new Workflow();
//
//        // Name, description
//        workflow.setName("Do While");
//        workflow.setDescription("Do While");
//
//        Graph graph = workflow.getGraph();
//
//        // amount
//        InputNode amount = (InputNode) workflow.addNode(this.inputComponent);
//        amount.setPosition(new Point(10, 10));
//
//        // if
//        DoWhileNode doWhileNode = (DoWhileNode) workflow.addNode(this.doWhileComponent);
//        doWhileNode.setPosition(new Point(200, 100));
//
//        // Approver nodes
//        Component approverComponent = this.componentRegistry.getComponent(Approver.WSDL_PATH);
//
//        Node approver = workflow.addNode(approverComponent);
//        approver.setPosition(new Point(350, 10));
//
//        // const
//        ConstantNode constYes = (ConstantNode) workflow.addNode(this.constantComponent);
//        constYes.setPosition(new Point(350, 200));
//
//        // endif
//        Node endDoWhile = workflow.addNode(this.endDoWhileComponent);
//        endDoWhile.setPosition(new Point(550, 100));
//
//        // Output
//        OutputNode output = (OutputNode) workflow.addNode(this.outputComponent);
//        output.setPosition(new Point(700, 100));
//
//        // Connect ports
//        graph.addEdge(amount.getOutputPort(0), approver.getInputPort(0));
//        graph.addEdge(amount.getOutputPort(0), doWhileNode.getInputPort(0));
//        graph.addEdge(doWhileNode.getControlOutPorts().get(0), approver.getControlInPort());
//        graph.addEdge(doWhileNode.getControlOutPorts().get(1), constYes.getControlInPort());
//        graph.addEdge(approver.getOutputPort(0), endDoWhile.getInputPort(0));
//        graph.addEdge(constYes.getOutputPort(0), endDoWhile.getInputPort(1));
//        graph.addEdge(endDoWhile.getOutputPort(0), output.getInputPort(0));
//
//        // Set the default values
//        // This needs to be after connection.
//        amount.setDefaultValue("0");
//        constYes.setValue("Yes");
//        doWhileNode.setXpath("$1 = 1");
//        output.setConfiguredName("accept");
//        output.setConfigured(true);
//
//        return workflow;
//    }
//}