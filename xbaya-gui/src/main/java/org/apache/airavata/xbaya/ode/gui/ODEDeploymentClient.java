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

package org.apache.airavata.xbaya.ode.gui;

import java.awt.Point;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.XBaya;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.component.registry.URLComponentRegistry;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.gui.StreamSourceNode;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.streaming.StreamReceiveComponent;
import org.apache.airavata.xbaya.streaming.StreamReceiveNode;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;
import org.ietf.jgss.GSSCredential;

public class ODEDeploymentClient {

    private XBayaEngine engine;

    private WaitDialog invokingDialog;

    public ODEDeploymentClient(XBayaEngine engine) {
        this.engine = engine;
    }

    public ODEDeploymentClient(XBayaEngine engine, WaitDialog invokingDialog) {
        this(engine);
        this.invokingDialog = invokingDialog;
    }

    /**
     * Deploy to ODE and XRegistry
     * 
     * @param wfClient
     * @param workflow
     * @param gssCredential
     * @param makePublic
     */
    public void deploy(WorkflowProxyClient wfClient, Workflow workflow, GSSCredential gssCredential,
            boolean makePublic, long start) {
        try {

            org.xmlpull.infoset.XmlElement workflowXml = workflow.toXML();
            XMLUtil.xmlElementToString(workflowXml);
            URI xRegistryURL = this.engine.getConfiguration().getXRegistryURL();
            if (xRegistryURL == null) {
                xRegistryURL = XBayaConstants.DEFAULT_XREGISTRY_URL;
            }
            wfClient.deploy(workflow, false);
            hideUI();
            String oldWorkflowName = workflow.getName();

            boolean needDeployment = false;
            List<NodeImpl> nodes = this.engine.getWorkflow().getGraph().getNodes();
            for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
                Node node = (Node) iterator.next();
                if (node instanceof StreamSourceNode) {
                    needDeployment = true;
                    break;
                }
            }
            if (needDeployment) {
                deployStreamControlWorkflowIfNecessary(this.engine.getWorkflow());

                WorkflowProxyClient wpClient = new WorkflowProxyClient();
                wpClient.setXRegistryUrl(this.engine.getConfiguration().getXRegistryURL());
                wpClient.setEngineURL(this.engine.getConfiguration().getProxyURI());
                wpClient.setXBayaEngine(this.engine);
                GSSCredential proxy = null;
                if (wpClient.isSecure()) {
                    MyProxyClient myProxyClient = this.engine.getMyProxyClient();
                    proxy = myProxyClient.getProxy();
                    UserX509Credential credential = new UserX509Credential(proxy,
                            XBayaSecurity.getTrustedCertificates());
                    wpClient.setUserX509Credential(credential);
                }
                this.engine.getGUI().getGraphCanvas()
                        .setNameAndDescription("Control_" + oldWorkflowName, "Control_" + oldWorkflowName);
                engine.getWorkflow().setName("Control_" + oldWorkflowName);
                engine.getWorkflow().getGraph().setID("Control_" + oldWorkflowName);

                deploy(wfClient, engine.getWorkflow(), gssCredential, makePublic, System.currentTimeMillis());

                long end = System.currentTimeMillis();
                XBaya.time.add("" + (end - start));
                System.out.println("Time:" + (end - start));

            }

        } catch (Throwable e) {
            hideUI();

            // The swing components get confused when there is html in the error
            // message
            if (e.getMessage() != null && e.getMessage().indexOf("<html>") != -1) {
                this.engine.getErrorWindow().error(e.getMessage().substring(0, e.getMessage().indexOf("<html>")));
            } else {
                this.engine.getErrorWindow().error(e);
            }
        }
    }

    /**
     * @param workflowWsdlUri
     * @throws URISyntaxException
     * @throws ComponentException
     * @throws ComponentRegistryException
     * @throws GraphException
     * 
     */
    // private void deployStreamControlWorkflowIfNecessary(Workflow oldWorkflow)
    // throws URISyntaxException, ComponentRegistryException,
    // ComponentException, GraphException {
    //
    // // create a new tab
    // GraphCanvas canvas = this.engine.getGUI().newGraphCanvas(true);
    //
    // // we can call with null because WSDL is already accessed at this point
    // WSComponent workflowComponent = new StreamReceiveComponent(oldWorkflow.getOdeInvokableWSDL(null, null));
    // Node workflowNode = canvas.addNode(workflowComponent, new Point(600,
    // 100));
    // // this.engine.getWorkflow().getWSDLs().put(((StreamReceiveNode)workflowNode).getWSDLID(),
    // oldWorkflow.getOdeWorkflowWSDL(null, null));
    //
    // URLComponentRegistry serviceRegistry = new URLComponentRegistry(
    // new URI(
    // "https://pagodatree.cs.indiana.edu:17443/axis2/services/Sleep?wsdl"));
    //
    // new ComponentRegistryLoader(engine).load(serviceRegistry);
    // ComponentTreeNode cepComponent = (ComponentTreeNode) serviceRegistry
    // .getComponentTree().getChildAt(0);
    //
    // InputComponent inputComponent = new InputComponent();
    // InputNode newNode = (InputNode) canvas.addNode(inputComponent,
    // new Point(50,50));
    // Node cepNode = canvas.addNode(cepComponent.getComponentReference()
    // .getComponent(), new Point(100, 100));
    // Port outputPort = cepNode.getOutputPort(0);
    // Graph graph = this.engine.getGUI().getGraphCanvas().getGraph();
    // graph.addEdge(newNode.getOutputPort(0), cepNode.getInputPort(0));
    //
    // for (int i = 0; i < XBaya.preservice; i++) {
    //
    // cepNode = canvas.addNode(cepComponent.getComponentReference()
    // .getComponent(), new Point(100+50*i, 100+50*i));
    // graph.addEdge(outputPort, cepNode.getInputPort(0));
    // outputPort = cepNode.getOutputPort(0);
    // }
    //
    // graph.addEdge(outputPort, workflowNode.getInputPort(0));
    //
    // OutputComponent outputComponent = new OutputComponent();
    // List<DataPort> outputPorts = workflowNode.getOutputPorts();
    //
    // for (DataPort dataPort : outputPorts) {
    // Node outNode = canvas.addNode(outputComponent, new Point(
    // 500, 100));
    // graph.addEdge(dataPort, outNode.getInputPort(0));
    // }
    //
    // }

    /**
     * @param workflowWsdlUri
     * @throws URISyntaxException
     * @throws ComponentException
     * @throws ComponentRegistryException
     * @throws GraphException
     * 
     */
    private void deployStreamControlWorkflowIfNecessary(Workflow oldWorkflow) throws URISyntaxException,
            ComponentRegistryException, ComponentException, GraphException {

        // create a new tab
        GraphCanvas canvas = this.engine.getGUI().newGraphCanvas(true);

        // we can call with null because WSDL is already accessed at this point
        WSComponent workflowComponent = new StreamReceiveComponent(oldWorkflow.getOdeInvokableWSDL(null, null));
        Node workflowNode = canvas.addNode(workflowComponent, new Point(600, 100));
        //
        this.engine.getWorkflow().getWSDLs()
                .put(((StreamReceiveNode) workflowNode).getWSDLID(), oldWorkflow.getOdeWorkflowWSDL(null, null));

        URLComponentRegistry serviceRegistry = new URLComponentRegistry(new URI(
                "https://pagodatree.cs.indiana.edu:17443/axis2/services/CEPService?wsdl"));

        new ComponentRegistryLoader(engine).load(serviceRegistry);
        ComponentTreeNode cepComponent = (ComponentTreeNode) serviceRegistry.getComponentTree().getChildAt(0);

        Node cepNode = canvas.addNode(cepComponent.getComponentReference().getComponent(), new Point(300, 50));

        Graph graph = this.engine.getGUI().getGraphCanvas().getGraph();
        List<DataPort> cepOutPorts = cepNode.getOutputPorts();
        int count = 0;
        InputComponent inputComponent = new InputComponent();
        LinkedList<InputNode> inputNodes = getStaticInputNodes(oldWorkflow);
        List<DataPort> workflowInPorts = workflowNode.getInputPorts();

        for (InputNode inputNode : inputNodes) {

            List<Port> streamedInputOutPorts = inputNode.getOutputPort(0).getToPorts();
            for (Port streamedInputOutPort : streamedInputOutPorts) {
                for (DataPort dataPort : workflowInPorts) {
                    if (dataPort.getName().equals(((DataPort) streamedInputOutPort).getName())) {
                        InputNode newNode = (InputNode) canvas.addNode(inputComponent, new Point(250 + 5 * count,
                                200 + count * 50));
                        graph.addEdge(newNode.getOutputPort(0), dataPort);
                        newNode.setDefaultValue(inputNode.getDefaultValue());
                        break;
                    }
                }

            }
            ++count;
        }
        for (DataPort dataPort : workflowInPorts) {
            if (dataPort.getFromNode() == null) {
                graph.addEdge(cepOutPorts.get(0), dataPort);
            }
        }

        List<DataPort> inputs = cepNode.getInputPorts();
        count = 0;
        for (DataPort dataPort : inputs) {
            Node inputNode = canvas.addNode(inputComponent, new Point(5 + 5 * count, 5 + count * 50));
            graph.addEdge(inputNode.getOutputPort(0), dataPort);
            ++count;
        }
        OutputComponent outputComponent = new OutputComponent();
        List<DataPort> outputPorts = workflowNode.getOutputPorts();

        count = 0;
        for (DataPort dataPort : outputPorts) {
            Node outNode = canvas.addNode(outputComponent, new Point(900 + count * 5, 100 + count * 50));
            graph.addEdge(dataPort, outNode.getInputPort(0));
            ++count;
        }

    }

    /**
     * @param oldWorkflow
     * @return static inputs
     */
    private LinkedList<InputNode> getStaticInputNodes(Workflow workflow) {

        List<NodeImpl> nodes = workflow.getGraph().getNodes();
        LinkedList<InputNode> streamNodes = new LinkedList<InputNode>();
        LinkedList<InputNode> ret = new LinkedList<InputNode>();
        for (NodeImpl nodeImpl : nodes) {
            if (nodeImpl instanceof StreamSourceNode) {
                streamNodes.addAll(((StreamSourceNode) nodeImpl).getInputNodes());
            }
        }

        for (NodeImpl nodeImpl : nodes) {
            if (nodeImpl instanceof InputNode && !streamNodes.contains(nodeImpl)) {
                ret.add((InputNode) nodeImpl);
            }
        }
        return ret;
    }

    private void hideUI() {
        if (this.invokingDialog != null) {
            this.invokingDialog.hide();
        }
    }

}