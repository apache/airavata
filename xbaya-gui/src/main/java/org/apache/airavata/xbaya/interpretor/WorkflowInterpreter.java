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

package org.apache.airavata.xbaya.interpretor;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaExecutionState;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.amazonEC2.gui.AmazonCredential;
import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.SubWorkflowComponent;
import org.apache.airavata.xbaya.component.amazon.InstanceComponent;
import org.apache.airavata.xbaya.component.amazon.TerminateInstanceComponent;
import org.apache.airavata.xbaya.component.dynamic.DynamicComponent;
import org.apache.airavata.xbaya.component.dynamic.DynamicInvoker;
import org.apache.airavata.xbaya.component.system.ConstantComponent;
import org.apache.airavata.xbaya.component.system.EndForEachComponent;
import org.apache.airavata.xbaya.component.system.EndifComponent;
import org.apache.airavata.xbaya.component.system.ForEachComponent;
import org.apache.airavata.xbaya.component.system.IfComponent;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.MemoComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.component.system.S3InputComponent;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.graph.ControlPort;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.amazon.InstanceNode;
import org.apache.airavata.xbaya.graph.dynamic.BasicTypeMapping;
import org.apache.airavata.xbaya.graph.dynamic.DynamicNode;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.impl.EdgeImpl;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.subworkflow.SubWorkflowNode;
import org.apache.airavata.xbaya.graph.subworkflow.SubWorkflowNodeGUI;
import org.apache.airavata.xbaya.graph.system.ConstantNode;
import org.apache.airavata.xbaya.graph.system.EndForEachNode;
import org.apache.airavata.xbaya.graph.system.EndifNode;
import org.apache.airavata.xbaya.graph.system.ForEachNode;
import org.apache.airavata.xbaya.graph.system.IfNode;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.graph.ws.WSPort;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.jython.lib.GenericInvoker;
import org.apache.airavata.xbaya.jython.lib.NotificationSender;
import org.apache.airavata.xbaya.jython.lib.invoker.SecureGFacInvoker;
import org.apache.airavata.xbaya.jython.lib.invoker.WorkflowInvokerWrapperForGFacInvoker;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.AmazonUtil;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowInvoker;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.impl.XmlElementWithViewsImpl;

import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.XmlConstants;
import xsul5.wsdl.WsdlPort;
import xsul5.wsdl.WsdlService;

public class WorkflowInterpreter {

    private static final int GUI_MODE = 1;

    private static final int SERVER_MODE = 2;

    private static final int MAXIMUM_RETRY_TIME = 2;

    private XBayaEngine engine;

    private Map<Node, Integer> retryCounter = new HashMap<Node, Integer>();

    private Map<Node, WorkflowInvoker> invokerMap = new HashMap<Node, WorkflowInvoker>();

    private NotificationSender notifier;

    private boolean retryFailed = true;

    private MyProxyChecker myProxyChecker;

    private Workflow workflow;

    private boolean isSubWorkflow;

    private XBayaConfiguration configuration;

    private int mode;

    private String password;

    private String username;

    private String topic;

    private LeadResourceMapping resourceMapping;

    /**
     * 
     * Constructs a WorkflowInterpreter.
     * 
     * @param configuration
     * @param topic
     * @param workflow
     * @param username
     * @param password
     */
    public WorkflowInterpreter(XBayaConfiguration configuration, String topic, Workflow workflow, String username,
            String password) {
        this.configuration = configuration;

        this.username = username;
        this.password = password;
        this.topic = topic;
        this.workflow = workflow;
        this.notifier = new NotificationSender(this.configuration.getBrokerURL(), topic);
        this.mode = SERVER_MODE;
        this.retryFailed = false;

    }

    /**
     * 
     * Constructs a WorkflowInterpreter.
     * 
     * @param engine
     * @param topic
     */
    public WorkflowInterpreter(XBayaEngine engine, String topic) {
        this(engine, topic, engine.getWorkflow());
    }

    /**
     * 
     * Constructs a WorkflowInterpreter.
     * 
     * @param engine
     * @param topic
     * @param workflow
     */
    public WorkflowInterpreter(XBayaEngine engine, String topic, Workflow workflow) {
        this(engine, topic, workflow, false);
    }

    /**
     * 
     * Constructs a WorkflowInterpreter.
     * 
     * @param engine
     * @param topic
     * @param workflow
     * @param subWorkflow
     */
    public WorkflowInterpreter(XBayaEngine engine, String topic, Workflow workflow, boolean subWorkflow) {
        this.engine = engine;
        this.configuration = engine.getConfiguration();
        this.myProxyChecker = new MyProxyChecker(this.engine);
        this.workflow = workflow;
        this.isSubWorkflow = subWorkflow;
        this.mode = GUI_MODE;
        this.notifier = new NotificationSender(this.configuration.getBrokerURL(), topic);
        this.topic = topic;

    }

    public void setResourceMapping(LeadResourceMapping resourceMapping) {
        this.resourceMapping = resourceMapping;
    }

    /**
     * @throws XBayaException
     */
    public void scheduleDynamically() throws XBayaException {
        try {
            if (!this.isSubWorkflow && this.workflow.getExecutionState() != XBayaExecutionState.NONE) {
                throw new WorkFlowInterpreterException("XBaya is already running a workflow");
            }

            this.workflow.setExecutionState(XBayaExecutionState.RUNNING);

            ArrayList<Node> inputNodes = this.getInputNodesDynamically();
            Object[] values = new Object[inputNodes.size()];
            String[] keywords = new String[inputNodes.size()];
            for (int i = 0; i < inputNodes.size(); ++i) {
                Node node = inputNodes.get(i);
                node.getGUI().setBodyColor(NodeState.FINISHED.color);
                if (this.mode == GUI_MODE) {
                    this.engine.getGUI().getGraphCanvas().repaint();
                }
                keywords[i] = ((InputNode) node).getConfiguredName();
                values[i] = ((InputNode) node).getDefaultValue();
            }
            this.notifier.workflowStarted(values, keywords);
            while (this.workflow.getExecutionState() != XBayaExecutionState.STOPPED) {
                if (getRemainNodesDynamically() == 0) {
                    if (this.mode == GUI_MODE) {
                        this.notifyPause();
                    } else {
                        this.workflow.setExecutionState(XBayaExecutionState.STOPPED);
                    }
                }
                // ok we have paused sleep
                while (this.workflow.getExecutionState() == XBayaExecutionState.PAUSED) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // get task list and execute them
                ArrayList<Node> readyNodes = this.getReadyNodesDynamically();
                for (Node node : readyNodes) {
                    if (node.isBreak()) {
                        this.notifyPause();
                        break;
                    }
                    if (this.workflow.getExecutionState() == XBayaExecutionState.PAUSED
                            || this.workflow.getExecutionState() == XBayaExecutionState.STOPPED) {
                        break;
                        // stop executing and sleep in the outer loop cause we
                        // want
                        // recalculate the execution stack
                    }
                    executeDynamically(node);
                    if (this.workflow.getExecutionState() == XBayaExecutionState.STEP) {
                        this.workflow.setExecutionState(XBayaExecutionState.PAUSED);
                        break;
                    }
                }
                sendOutputsDynamically();
                // Dry run sleep a lil bit to release load
                if (readyNodes.size() == 0) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            this.notifier.workflowTerminated();
            if (this.mode == GUI_MODE) {
                final WaitDialog waitDialog = new WaitDialog(new Cancelable() {
                    @Override
                    public void cancel() {
                        // Do nothing
                    }
                }, "Stop Workflow", "Cleaning up resources for Workflow", this.engine);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        waitDialog.show();
                    }
                }).start();
                // Send Notification for output values
                finish();
                // Sleep to provide for notification delay
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cleanup();
                waitDialog.hide();
            } else {
                finish();
            }
        } catch (RuntimeException e) {
            // we reset all the state
            cleanup();
            raiseException(e);
        }
    }

    /**
     * @param e
     */
    public void raiseException(Throwable e) {
        if (this.mode == GUI_MODE) {
            this.engine.getErrorWindow().error(e);
        } else {
            throw new RuntimeException(e);
        }

    }

    /**
	 * 
	 */
    private void notifyPause() {
        if (this.mode == GUI_MODE) {

            if (this.workflow.getExecutionState() == XBayaExecutionState.RUNNING
                    || this.workflow.getExecutionState() == XBayaExecutionState.STEP) {
                this.engine.getGUI().getToolbar().getPlayAction().actionPerformed(null);
            } else {
                throw new XBayaRuntimeException("Cannot pause when not running");
            }
        }
    }

    /**
     * @throws MonitorException
     */
    public void cleanup() throws MonitorException {
        if (this.mode == GUI_MODE) {
            this.engine.getMonitor().stop();
            this.engine.getGUI().removeDynamicExecutionToolsFromToolbar();
        }
        this.workflow.setExecutionState(XBayaExecutionState.NONE);
    }

    private void sendOutputsDynamically() throws XBayaException {
        ArrayList<Node> outputNodes = getReadyOutputNodesDynamically();
        if (outputNodes.size() != 0) {
            LinkedList<Object> outputValues = new LinkedList<Object>();
            LinkedList<String> outputKeywords = new LinkedList<String>();
            for (Node node : outputNodes) {
                // Change it to processing state so we will not pic it up in the
                // next run
                // even if the next run runs before the notification arrives
                node.getGUI().setBodyColor(NodeState.EXECUTING.color);
                // OutputNode node = (OutputNode) outputNode;
                List<DataPort> inputPorts = node.getInputPorts();

                for (DataPort dataPort : inputPorts) {
                    Object val = findInputFromPort(dataPort);
                    if (null == val) {
                        throw new WorkFlowInterpreterException("Unable to find output for the node:" + node.getID());
                    }
                    // This is ok because the outputnodes always got only one
                    // input
                    ((OutputNode) node).setDescription(val.toString());
                    node.getGUI().setBodyColor(NodeState.FINISHED.color);
                }
            }
            this.notifier.sendingPartialResults(outputValues.toArray(),
                    outputKeywords.toArray(new String[outputKeywords.size()]));
        }
    }

    private void finish() throws XBayaException {
        ArrayList<Node> outoutNodes = new ArrayList<Node>();
        List<NodeImpl> nodes = this.workflow.getGraph().getNodes();
        for (Node node : nodes) {
            if (node instanceof OutputNode) {
                if (node.getInputPort(0).getFromNode().getGUI().getBodyColor() == NodeState.FINISHED.color) {
                    outoutNodes.add(node);
                } else {
                    // The workflow is incomplete so return without sending
                    // workflowFinished
                    return;
                }
            }
        }
        LinkedList<Object> outputValues = new LinkedList<Object>();
        LinkedList<String> outputKeywords = new LinkedList<String>();
        for (Node outputNode : outoutNodes) {
            OutputNode node = (OutputNode) outputNode;
            List<DataPort> inputPorts = node.getInputPorts();
            for (DataPort dataPort : inputPorts) {
                Object val = findInputFromPort(dataPort);

                if (null == val) {
                    throw new WorkFlowInterpreterException("Unable to find output for the node:" + node.getID());
                }
                // Some node not yet updated
                if (node.getGUI().getBodyColor() != NodeState.FINISHED.color) {
                    node.setDescription(val.toString());
                    node.getGUI().setBodyColor(NodeState.FINISHED.color);
                }
            }

        }
        this.notifier.sendingPartialResults(outputValues.toArray(),
                outputKeywords.toArray(new String[outputKeywords.size()]));
    }

    private void executeDynamically(Node node) throws XBayaException {
        node.getGUI().setBodyColor(NodeState.EXECUTING.color);
        Component component = node.getComponent();
        if (component instanceof SubWorkflowComponent) {
            handleSubWorkComponent(node);
        } else if (component instanceof WSComponent) {
            handleWSComponent(node);
        } else if (component instanceof DynamicComponent) {
            handleDynamicComponent(node);
        } else if (component instanceof ForEachComponent) {
            handleForEach(node);
        } else if (component instanceof IfComponent) {
            handleIf(node);
        } else if (component instanceof EndifComponent) {
            handleEndIf(node);
        } else if (component instanceof InstanceComponent) {
            if (AmazonCredential.getInstance().getAwsAccessKeyId().isEmpty()
                    || AmazonCredential.getInstance().getAwsSecretAccessKey().isEmpty()) {
                throw new WorkFlowInterpreterException(
                        "Please set Amazon Credential before using EC2 Instance Component");
            }
            for (ControlPort ports : node.getControlOutPorts()) {
                ports.setConditionMet(true);
            }
        } else if (component instanceof TerminateInstanceComponent) {
            Object inputVal = findInputFromPort(node.getInputPort(0));
            String instanceId = inputVal.toString();

            this.notifier.resourceMapping(instanceId, "Terminating EC2 Instance:" + instanceId);

            /*
             * Terminate instance
             */
            AmazonUtil.terminateInstances(instanceId);

            // set color to done
            node.getGUI().setBodyColor(NodeState.FINISHED.color);
        } else {
            throw new WorkFlowInterpreterException("Encountered Node that cannot be executed:" + node);
        }
    }

    private Object findInputFromPort(Port inputPort) throws XBayaException {
        Object outputVal = null;
        Node fromNode = inputPort.getFromNode();
        if (fromNode instanceof InputNode) {
            outputVal = ((InputNode) fromNode).getDefaultValue();
        } else if (fromNode instanceof ConstantNode) {
            outputVal = ((ConstantNode) fromNode).getValue();
        } else if (fromNode instanceof EndifNode) {
            WorkflowInvoker fromInvoker = this.invokerMap.get(fromNode);
            outputVal = fromInvoker.getOutput(inputPort.getFromPort().getID());
        } else if (fromNode instanceof InstanceNode) {
            return ((InstanceNode) fromNode).getOutputInstanceId();
        } else {
            WorkflowInvoker fromInvoker = this.invokerMap.get(fromNode);
            if (fromInvoker != null)
                outputVal = fromInvoker.getOutput(inputPort.getFromPort().getName());
        }
        return outputVal;
    }

    private void handleSubWorkComponent(Node node) throws XBayaException {
        if (this.mode == GUI_MODE) {
            ((SubWorkflowNodeGUI) node.getGUI()).openWorkflowTab(this.engine);
        }
        // setting the inputs
        Workflow subWorkflow = ((SubWorkflowNode) node).getWorkflow();
        // List<WSComponentPort> subWorkflowdInputs = new ODEClient()
        // .getInputs(subWorkflow);
        ArrayList<Node> subWorkflowInputNodes = getInputNodes(subWorkflow);

        List<DataPort> inputPorts = node.getInputPorts();
        for (DataPort port : inputPorts) {
            Object inputVal = findInputFromPort(port);
            if (null == inputVal) {
                throw new WorkFlowInterpreterException("Unable to find inputs for the subworkflow node node:"
                        + node.getID());
            }

            for (Iterator<Node> iterator = subWorkflowInputNodes.iterator(); iterator.hasNext();) {
                InputNode inputNode = (InputNode) iterator.next();
                if (inputNode.getName().equals(port.getName())) {
                    inputNode.setDefaultValue(inputVal);
                }
            }
        }
        for (Iterator<Node> iterator = subWorkflowInputNodes.iterator(); iterator.hasNext();) {
            InputNode inputNode = (InputNode) iterator.next();
            if (inputNode.getDefaultValue() == null) {
                throw new WorkFlowInterpreterException("Input not set for  :" + inputNode.getID());
            }

        }

        if (this.mode == GUI_MODE) {
            new WorkflowInterpreter(this.engine, this.topic, subWorkflow, true).scheduleDynamically();
        } else {
            new WorkflowInterpreter(this.configuration, this.topic, subWorkflow, this.username, this.password)
                    .scheduleDynamically();
        }
    }

    private void handleWSComponent(Node node) throws XBayaException {
        WSComponent wsComponent = ((WSComponent) node.getComponent());
        QName portTypeQName = wsComponent.getPortTypeQName();
        WorkflowInvoker invoker = this.invokerMap.get(node);
        if (invoker == null) {
            final WSNode wsNode = (WSNode) node;
            String wsdlLocation = this.getEPR(wsNode);
            final String gfacURLString = this.configuration.getGFacURL().toString();
            if (null == wsdlLocation) {
                if (gfacURLString.startsWith("https")) {
                    GSSCredential proxy = null;
                    if (this.mode == GUI_MODE) {
                        this.myProxyChecker.loadIfNecessary();
                        MyProxyClient myProxyClient = this.engine.getMyProxyClient();
                        proxy = myProxyClient.getProxy();
                    } else {
                        proxy = SecurityUtil.getGSSCredential(this.username, this.password,
                                this.configuration.getMyProxyServer());
                    }

                    LeadContextHeader leadCtxHeader = null;
                    try {
                        if (this.mode == GUI_MODE) {
                            leadCtxHeader = WSDLUtil.buildLeadContextHeader(this.workflow, this.configuration,
                                    new MonitorConfiguration(this.configuration.getBrokerURL(), this.topic, true,
                                            this.configuration.getMessageBoxURL()), wsNode.getID(), null);
                        } else {
                            leadCtxHeader = WSDLUtil.buildLeadContextHeader(this.workflow, this.configuration,
                                    new MonitorConfiguration(this.configuration.getBrokerURL(), this.topic, true,
                                            this.configuration.getMessageBoxURL()), wsNode.getID(), null);
                        }
                    } catch (URISyntaxException e) {
                        throw new XBayaException(e);
                    }

                    leadCtxHeader.setServiceId(node.getID());
                    try {
                        leadCtxHeader.setWorkflowId(new URI(this.workflow.getName()));

                        // We do this so that the wsdl resolver can is setup
                        // wsdlresolver.getInstance is static so once this is
                        // done
                        // rest of the loading should work.

                        XBayaSecurity.init();

                    } catch (URISyntaxException e) {
                        throw new XBayaRuntimeException(e);
                    }

                    /*
                     * Resource Mapping Header
                     */
                    if (this.resourceMapping != null) {
                        leadCtxHeader.setResourceMapping(this.resourceMapping);
                    }

                    /*
                     * If there is a instance control component connects to this component send information in soap
                     * header
                     */
                    for (Node n : wsNode.getControlInPort().getFromNodes()) {
                        if (n instanceof InstanceNode) {
                            // TODO make it as constant
                            LeadResourceMapping x = new LeadResourceMapping("AMAZON");

                            x.addAttribute("ACCESS_KEY", AmazonCredential.getInstance().getAwsAccessKeyId());
                            x.addAttribute("SECRET_KEY", AmazonCredential.getInstance().getAwsSecretAccessKey());

                            if (((InstanceNode) n).isStartNewInstance()) {
                                x.addAttribute("AMI_ID", ((InstanceNode) n).getIdAsValue());
                                x.addAttribute("INS_TYPE", ((InstanceNode) n).getInstanceType());
                            } else {
                                x.addAttribute("INS_ID", ((InstanceNode) n).getIdAsValue());
                            }

                            x.addAttribute("USERNAME", ((InstanceNode) n).getUsername());

                            // set to leadHeader
                            leadCtxHeader.setResourceMapping(x);
                        }
                    }

                    invoker = new WorkflowInvokerWrapperForGFacInvoker(portTypeQName, gfacURLString, this.configuration
                            .getMessageBoxURL().toString(), leadCtxHeader,
                            this.notifier.createServiceNotificationSender(node.getID()));

                } else {
                    invoker = new GenericInvoker(portTypeQName, WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(wsNode
                            .getComponent().getWSDL()), node.getID(), this.configuration.getMessageBoxURL().toString(),
                            gfacURLString, this.notifier, this.configuration, null);
                }

            } else {
                if (!wsdlLocation.endsWith("?wsdl")) {
                    wsdlLocation += "?wsdl";
                }
                invoker = new GenericInvoker(portTypeQName, wsdlLocation, node.getID(), this.configuration
                        .getMessageBoxURL().toString(), gfacURLString, this.notifier);
            }
            invoker.setup();
            this.invokerMap.put(node, invoker);
            invoker.setOperation(wsComponent.getOperationName());
        }

        // find inputs
        List<DataPort> inputPorts = node.getInputPorts();
        ODEClient odeClient = new ODEClient();
        List<WSComponentPort> inputComponents = odeClient.getInputs(this.workflow);
        for (DataPort port : inputPorts) {
            Object inputVal = findInputFromPort(port);

            /*
             * Need to override inputValue if it is odeClient
             */
            Node fromNode = port.getFromNode();
            if (port.getFromNode() instanceof InputNode) {
                for (WSComponentPort wsComponentPort : inputComponents) {
                    if (fromNode.getName().equals(wsComponentPort.getName())) {
                        inputVal = odeClient.parseValue(wsComponentPort, (String) inputVal);
                    }
                }
            }

            if (null == inputVal) {
                throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
            }
            invoker.setInput(port.getName(), inputVal);
        }
        invoker.invoke();
    }

    private void handleDynamicComponent(Node node) throws XBayaException {
        DynamicComponent dynamicComponent = (DynamicComponent) node.getComponent();
        String className = dynamicComponent.getClassName();
        String operationName = dynamicComponent.getOperationName();
        URL implJarLocation = dynamicComponent.getImplJarLocation();
        DynamicNode dynamicNode = (DynamicNode) node;
        LinkedList<Object> inputs = new LinkedList<Object>();
        List<DataPort> inputPorts = dynamicNode.getInputPorts();
        for (DataPort dataPort : inputPorts) {
            Object inputVal = findInputFromPort(dataPort);

            /*
             * Set type after get input value, and override inputValue if output type is array
             */
            Node fromNode = dataPort.getFromNode();
            QName type = null;
            if (fromNode instanceof InputNode) {
                type = BasicTypeMapping.STRING_QNAME;
            } else if (fromNode instanceof ConstantNode) {
                type = ((ConstantNode) fromNode).getType();
            } else if ((dataPort.getFromPort() instanceof WSPort)
                    && BasicTypeMapping.isArrayType(((WSPort) dataPort.getFromPort()).getComponentPort().getElement())) {
                WorkflowInvoker fromInvoker = this.invokerMap.get(fromNode);
                inputVal = BasicTypeMapping.getOutputArray(XmlConstants.BUILDER.parseFragmentFromString(fromInvoker
                        .getOutputs().toString()), dataPort.getFromPort().getName(), BasicTypeMapping
                        .getSimpleTypeIndex(((DataPort) dataPort.getFromPort()).getType()));
                type = ((DataPort) dataPort.getFromPort()).getType();
            } else {
                type = ((DataPort) dataPort.getFromPort()).getType();
            }

            if (null == inputVal) {
                throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
            }
            inputs.add(BasicTypeMapping.getObjectOfType(type, inputVal));

        }

        DynamicInvoker dynamicInvoker = new DynamicInvoker(className, implJarLocation, operationName, inputs.toArray());
        this.invokerMap.put(node, dynamicInvoker);
        dynamicInvoker.setup();
        dynamicInvoker.invoke();
        node.getGUI().setBodyColor(NodeState.FINISHED.color);
    }

    private void handleForEach(Node node) throws XBayaException {
        final ForEachNode forEachNode = (ForEachNode) node;
        EndForEachNode endForEachNode = null;
        Collection<Node> repeatNodes = node.getOutputPort(0).getToNodes();
        // we will support only one for now
        if (repeatNodes.size() != 1) {
            throw new WorkFlowInterpreterException("Only one node allowed inside foreach");
        }
        Iterator<Node> iterator = repeatNodes.iterator();
        if (iterator.hasNext()) {

            Node middleNode = iterator.next();

            if (!(middleNode instanceof WSNode)) {
                throw new WorkFlowInterpreterException("Encountered Node inside foreach that is not a WSNode"
                        + middleNode);
            }
            Iterator<Node> endForEachNodeItr = middleNode.getOutputPort(0).getToNodes().iterator();
            while (endForEachNodeItr.hasNext()) {
                Node node2 = endForEachNodeItr.next();
                if (!(node2 instanceof EndForEachNode)) {
                    throw new WorkFlowInterpreterException("Found More than one node inside foreach");
                } else {
                    endForEachNode = (EndForEachNode) node2;
                }

            }
            final WSNode foreachWSNode = (WSNode) middleNode;
            final LinkedList<String> listOfValues = new LinkedList<String>();

            Node forEachInputNode = forEachNode.getInputPort(0).getFromNode();
            // if input node for for-each is WSNode
            if (forEachInputNode instanceof WSNode) {
                WorkflowInvoker workflowInvoker = this.invokerMap.get(forEachInputNode);
                if (workflowInvoker != null) {
                    if (workflowInvoker instanceof GenericInvoker) {
                        /*
                         * TODO How this code handle object from GenericInvoker
                         */
                        String message = ((GenericInvoker) workflowInvoker).getOutputs().toString();
                        XmlElement msgElmt = XmlConstants.BUILDER.parseFragmentFromString(message);
                        Iterator children = msgElmt.children().iterator();
                        while (children.hasNext()) {
                            Object object = children.next();
                            if (object instanceof XmlElement) {
                                XmlElement child = (XmlElement) object;
                                Iterator valItr = child.children().iterator();
                                if (valItr.hasNext()) {
                                    Object object2 = valItr.next();
                                    if (object2 instanceof String) {
                                        listOfValues.add(object2.toString());
                                    }
                                }
                            }
                        }
                    } else if (workflowInvoker instanceof WorkflowInvokerWrapperForGFacInvoker) {
                        String outputName = forEachInputNode.getOutputPort(0).getName();
                        org.xmlpull.v1.builder.XmlElement msgElmt = (org.xmlpull.v1.builder.XmlElement) workflowInvoker
                                .getOutput(outputName);
                        Iterator children = msgElmt.children();
                        while (children.hasNext()) {
                            Object object = children.next();
                            if (object instanceof org.xmlpull.v1.builder.XmlElement) {
                                org.xmlpull.v1.builder.XmlElement child = (org.xmlpull.v1.builder.XmlElement) object;
                                Iterator valItr = child.children();
                                if (valItr.hasNext()) {
                                    Object object2 = valItr.next();
                                    if (object2 instanceof String) {
                                        listOfValues.add(object2.toString());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    throw new WorkFlowInterpreterException("Did not find inputs from WS to foreach");
                }
                // if input node for for-each is input
            } else if (forEachInputNode instanceof InputNode) {
                for (DataPort dataPort : forEachNode.getInputPorts()) {
                    Object val = findInputFromPort(dataPort);
                    if (null == val) {
                        throw new WorkFlowInterpreterException("Unable to find input for the node:" + node.getID());
                    }
                    String[] vals = val.toString().split(",");
                    listOfValues.addAll(Arrays.asList(vals));
                }
            } else {
                throw new WorkFlowInterpreterException("Did not find inputs to foreach");
            }

            if (listOfValues.size() > 0) {
                forEachNode.getGUI().setBodyColor(NodeState.EXECUTING.color);
                foreachWSNode.getGUI().setBodyColor(NodeState.EXECUTING.color);
                endForEachNode.getGUI().setBodyColor(NodeState.EXECUTING.color);
                final EndForEachNode tempendForEachNode = endForEachNode;
                final SystemComponentInvoker systemInvoker = new SystemComponentInvoker();
                this.invokerMap.put(endForEachNode, systemInvoker);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            runInThread(listOfValues, forEachNode, foreachWSNode, tempendForEachNode, systemInvoker);
                        } catch (XBayaException e) {
                            WorkflowInterpreter.this.engine.getErrorWindow().error(e);
                        }
                    }

                }.start();
            } else {
                throw new WorkFlowInterpreterException("No array values found for foreach");
            }
        }
    }

    private void handleIf(Node node) throws XBayaException {
        IfNode ifNode = (IfNode) node;

        /*
         * Get all input to check condition
         */
        String booleanExpression = ifNode.getXPath();
        if (booleanExpression == null) {
            throw new WorkFlowInterpreterException("XPath for if cannot be null");
        }

        List<DataPort> inputPorts = node.getInputPorts();
        int i = 0;
        for (DataPort port : inputPorts) {
            Object inputVal = findInputFromPort(port);

            if (null == inputVal) {
                throw new WorkFlowInterpreterException("Unable to find inputs for the node:" + node.getID());
            }

            booleanExpression = booleanExpression.replaceAll("\\$" + i, "'" + inputVal + "'");
        }

        // Now the XPath expression
        try {
            XPathFactory xpathFact = XPathFactory.newInstance();
            XPath xpath = xpathFact.newXPath();
            Boolean result = (Boolean) xpath.evaluate(booleanExpression, booleanExpression, XPathConstants.BOOLEAN);

            /*
             * Set control port to make execution flow continue according to condition
             */
            for (ControlPort controlPort : node.getControlOutPorts()) {
                if (controlPort.getName().equals(IfComponent.TRUE_PORT_NAME)) {
                    controlPort.setConditionMet(result.booleanValue());
                } else if (controlPort.getName().equals(IfComponent.FALSE_PORT_NAME)) {
                    controlPort.setConditionMet(!result.booleanValue());
                }
            }

            node.getGUI().setBodyColor(NodeState.FINISHED.color);

        } catch (XPathExpressionException e) {
            throw new WorkFlowInterpreterException("Cannot evaluate XPath in If Condition: " + booleanExpression);
        }
    }

    private void handleEndIf(Node node) throws XBayaException {
        EndifNode endIfNode = (EndifNode) node;
        SystemComponentInvoker invoker = new SystemComponentInvoker();

        List<DataPort> ports = endIfNode.getOutputPorts();
        for (int outputPortIndex = 0, inputPortIndex = 0; outputPortIndex < ports.size(); outputPortIndex++, inputPortIndex = inputPortIndex + 2) {

            Object inputVal = findInputFromPort(endIfNode.getInputPort(inputPortIndex));

            Object inputVal2 = findInputFromPort(endIfNode.getInputPort(inputPortIndex + 1));

            if ((inputVal == null && inputVal2 == null) || (inputVal != null && inputVal2 != null)) {
                throw new WorkFlowInterpreterException("EndIf gets wrong input number" + "Port:" + inputPortIndex
                        + " and " + (inputPortIndex + 1));
            }

            Object value = inputVal != null ? inputVal : inputVal2;
            invoker.addOutput(endIfNode.getOutputPort(outputPortIndex).getID(), value);
        }

        this.invokerMap.put(node, invoker);

        node.getGUI().setBodyColor(NodeState.FINISHED.color);
    }

    private void runInThread(LinkedList<String> listOfValues, ForEachNode forEachNode, WSNode foreachWSNode,
            EndForEachNode endForEachNode, SystemComponentInvoker tempInvoker) throws XBayaException {
        WSComponent wsComponent = foreachWSNode.getComponent();
        QName portTypeQName = wsComponent.getPortTypeQName();

        WorkflowInvoker invoker = null;
        LinkedList<WorkflowInvoker> invokerList = new LinkedList<WorkflowInvoker>();
        for (Iterator<String> iterator = listOfValues.iterator(); iterator.hasNext();) {
            String input = iterator.next();
            String wsdlLocation = getEPR(foreachWSNode);
            final String gfacURLString = this.engine.getConfiguration().getGFacURL().toString();

            /*
             * Duplication start Code below copied from handleWSComponent
             */
            if (null == wsdlLocation) {
                if (gfacURLString.startsWith("https")) {
                    GSSCredential proxy = null;
                    if (this.mode == GUI_MODE) {
                        this.myProxyChecker.loadIfNecessary();
                        MyProxyClient myProxyClient = this.engine.getMyProxyClient();
                        proxy = myProxyClient.getProxy();
                    } else {
                        proxy = SecurityUtil.getGSSCredential(this.username, this.password,
                                this.configuration.getMyProxyServer());
                    }

                    LeadContextHeader leadCtxHeader = null;
                    try {
                        if (this.mode == GUI_MODE) {
                            leadCtxHeader = WSDLUtil.buildLeadContextHeader(this.workflow, this.configuration,
                                    new MonitorConfiguration(this.configuration.getBrokerURL(), this.topic, true,
                                            this.configuration.getMessageBoxURL()), foreachWSNode.getID(), null);
                        } else {
                            leadCtxHeader = WSDLUtil.buildLeadContextHeader(this.workflow,
                                    this.configuration,
                                    new MonitorConfiguration(this.configuration.getBrokerURL(), this.topic, true,
                                            this.configuration.getMessageBoxURL()), foreachWSNode.getID(), null);
                        }
                    } catch (URISyntaxException e) {
                        throw new XBayaException(e);
                    }

                    leadCtxHeader.setServiceId(foreachWSNode.getID());
                    try {
                        leadCtxHeader.setWorkflowId(new URI(this.workflow.getName()));

                        /*
                         * We do this so that the wsdl resolver can is setup wsdlresolver.getInstance is static so once
                         * this is done rest of the loading should work.
                         */
                        XBayaSecurity.init();

                    } catch (URISyntaxException e) {
                        throw new XBayaRuntimeException(e);
                    }

                    /*
                     * Resource Mapping Header
                     */
                    if (this.resourceMapping != null) {
                        leadCtxHeader.setResourceMapping(this.resourceMapping);
                    }

                    /*
                     * If there is a instance control component connects to this component send information in soap
                     * header
                     */
                    for (Node n : foreachWSNode.getControlInPort().getFromNodes()) {
                        if (n instanceof InstanceNode) {
                            // TODO make it as constant
                            LeadResourceMapping x = new LeadResourceMapping("AMAZON");

                            x.addAttribute("ACCESS_KEY", AmazonCredential.getInstance().getAwsAccessKeyId());
                            x.addAttribute("SECRET_KEY", AmazonCredential.getInstance().getAwsSecretAccessKey());

                            if (((InstanceNode) n).isStartNewInstance()) {
                                x.addAttribute("AMI_ID", ((InstanceNode) n).getIdAsValue());
                                x.addAttribute("INS_TYPE", ((InstanceNode) n).getInstanceType());
                            } else {
                                x.addAttribute("INS_ID", ((InstanceNode) n).getIdAsValue());
                            }

                            x.addAttribute("USERNAME", ((InstanceNode) n).getUsername());

                            // set to leadHeader
                            leadCtxHeader.setResourceMapping(x);
                        }
                    }

                    invoker = new WorkflowInvokerWrapperForGFacInvoker(portTypeQName, gfacURLString, this.configuration
                            .getMessageBoxURL().toString(), leadCtxHeader,
                            this.notifier.createServiceNotificationSender(foreachWSNode.getID()));
                } else {
                    invoker = new GenericInvoker(portTypeQName,
                            WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(foreachWSNode.getComponent().getWSDL()),
                            foreachWSNode.getID(), this.configuration.getMessageBoxURL().toString(), gfacURLString,
                            this.notifier, this.configuration, null);
                }

            } else {
                if (!wsdlLocation.endsWith("?wsdl")) {
                    wsdlLocation += "?wsdl";
                }
                invoker = new GenericInvoker(portTypeQName, wsdlLocation, foreachWSNode.getID(), this.configuration
                        .getMessageBoxURL().toString(), gfacURLString, this.notifier);
            }
            /*
             * End duplication
             */

            invoker.setup();
            invoker.setOperation(wsComponent.getOperationName());
            invokerList.add(invoker);
            // find inputs
            List<DataPort> inputPorts = foreachWSNode.getInputPorts();
            for (DataPort port : inputPorts) {
                Object inputVal = findInputFromPort(port);

                /*
                 * Handle ForEachNode
                 */
                Node fromNode = port.getFromNode();
                if (fromNode instanceof ForEachNode) {
                    inputVal = input;
                }

                if (null == inputVal) {
                    throw new WorkFlowInterpreterException("Unable to find inputs for the node:"
                            + foreachWSNode.getID());
                }
                invoker.setInput(port.getName(), inputVal);
            }
            invoker.invoke();
        }

        String outputStr = "";
        for (Iterator<WorkflowInvoker> iterator = invokerList.iterator(); iterator.hasNext();) {
            WorkflowInvoker workflowInvoker = iterator.next();
            Object output = workflowInvoker.getOutput(foreachWSNode.getOutputPort(0).getName());
            outputStr += "\n<value>" + output + "</value>";
        }
        tempInvoker.addOutput(endForEachNode.getOutputPort(0).getName(), outputStr);
        forEachNode.getGUI().setBodyColor(NodeState.FINISHED.color);
        foreachWSNode.getGUI().setBodyColor(NodeState.FINISHED.color);
        endForEachNode.getGUI().setBodyColor(NodeState.FINISHED.color);

    }

    private ArrayList<Node> getReadyOutputNodesDynamically() {
        ArrayList<Node> list = new ArrayList<Node>();
        List<NodeImpl> nodes = this.workflow.getGraph().getNodes();
        for (Node node : nodes) {
            if (node instanceof OutputNode && node.getGUI().getBodyColor() == NodeGUI.DEFAULT_BODY_COLOR
                    && node.getInputPort(0).getFromNode().getGUI().getBodyColor() == NodeState.FINISHED.color) {

                list.add(node);
            }
        }
        return list;
    }

    private int getRemainNodesDynamically() {
        int failed = 0;
        if (this.retryFailed) {
            failed = this.getFailedNodeCountDynamically();
        }
        return this.getWaitingNodeCountDynamically() + this.getRunningNodeCountDynamically() + failed;
    }

    private ArrayList<Node> getInputNodesDynamically() {
        return getInputNodes(this.workflow);
    }

    private ArrayList<Node> getInputNodes(Workflow wf) {
        ArrayList<Node> list = new ArrayList<Node>();
        List<NodeImpl> nodes = wf.getGraph().getNodes();
        for (Node node : nodes) {
            String name = node.getComponent().getName();
            if (InputComponent.NAME.equals(name) || ConstantComponent.NAME.equals(name)
                    || S3InputComponent.NAME.equals(name)) {
                list.add(node);
            }
        }
        return list;
    }

    private ArrayList<Node> getReadyNodesDynamically() {
        ArrayList<Node> list = new ArrayList<Node>();
        ArrayList<Node> waiting = this.getWaitingNodesDynamically();
        ArrayList<Node> finishedNodes = this.getFinishedNodesDynamically();
        for (Node node : waiting) {
            Component component = node.getComponent();
            if (component instanceof WSComponent || component instanceof DynamicComponent
                    || component instanceof SubWorkflowComponent || component instanceof ForEachComponent
                    || component instanceof EndForEachComponent || component instanceof IfComponent
                    || component instanceof InstanceComponent) {

                /*
                 * Check for control ports from other node
                 */
                ControlPort control = node.getControlInPort();
                boolean controlDone = true;
                if (control != null) {
                    for (EdgeImpl edge : control.getEdges()) {
                        controlDone = controlDone && ((ControlPort) edge.getFromPort()).isConditionMet();
                    }
                }

                /*
                 * Check for input ports
                 */
                List<DataPort> inputPorts = node.getInputPorts();
                boolean inputsDone = true;
                for (DataPort dataPort : inputPorts) {
                    inputsDone = inputsDone && finishedNodes.contains(dataPort.getFromNode());
                }
                if (inputsDone && controlDone) {
                    list.add(node);
                }
            } else if (component instanceof EndifComponent) {
                /*
                 * EndIfComponent can run if number of input equals to number of output that it expects
                 */
                int expectedOutput = node.getOutputPorts().size();
                int actualInput = 0;
                List<DataPort> inputPorts = node.getInputPorts();
                for (DataPort dataPort : inputPorts) {
                    if (finishedNodes.contains(dataPort.getFromNode()))
                        actualInput++;
                }

                if (expectedOutput == actualInput) {
                    list.add(node);
                }
            } else if (component instanceof TerminateInstanceComponent) {
                /*
                 * All node connected to controlIn port must be done
                 */
                ControlPort control = node.getControlInPort();
                boolean controlDone = true;
                if (control != null) {
                    for (EdgeImpl edge : control.getEdges()) {
                        controlDone = controlDone && finishedNodes.contains(edge.getFromPort().getFromNode());
                    }
                }

                /*
                 * Check for input ports
                 */
                List<DataPort> inputPorts = node.getInputPorts();
                boolean inputsDone = true;
                for (DataPort dataPort : inputPorts) {
                    inputsDone = inputsDone && finishedNodes.contains(dataPort.getFromNode());
                }
                if (inputsDone && controlDone) {
                    list.add(node);
                }

            } else if (InputComponent.NAME.equals(component.getName())
                    || S3InputComponent.NAME.equals(component.getName())
                    || OutputComponent.NAME.equals(component.getName())
                    || MemoComponent.NAME.equals(component.getName())) {
                // no op
            } else {
                throw new WorkFlowInterpreterException("Component Not handled :" + component.getName());
            }
        }

        if (this.retryFailed) {
            /*
             * Calculate Rerun time for each failed Node
             */
            for (Node node2 : this.getFailedNodesDynamically()) {
                if (this.retryCounter.containsKey(node2)) {
                    int rerunTimes = this.retryCounter.get(node2).intValue();
                    if (rerunTimes < MAXIMUM_RETRY_TIME) {
                        this.retryCounter.put(node2, Integer.valueOf(++rerunTimes));
                        list.add(node2);
                    } else {
                        // if some component fail so many times, stop the workflow
                        if (this.mode == GUI_MODE) {
                            this.notifyPause();
                        } else {
                            this.workflow.setExecutionState(XBayaExecutionState.STOPPED);
                        }
                    }
                } else {
                    this.retryCounter.put(node2, Integer.valueOf(1));
                    list.add(node2);
                }
            }
        }

        return list;

    }

    private ArrayList<Node> getFinishedNodesDynamically() {
        return this.getNodesWithBodyColor(NodeState.FINISHED.color);
    }

    private ArrayList<Node> getFailedNodesDynamically() {
        return this.getNodesWithBodyColor(NodeState.FAILED.color);
    }

    private ArrayList<Node> getWaitingNodesDynamically() {
        return this.getNodesWithBodyColor(NodeGUI.DEFAULT_BODY_COLOR);
    }

    private ArrayList<Node> getNodesWithBodyColor(Color color) {
        ArrayList<Node> list = new ArrayList<Node>();
        List<NodeImpl> nodes = this.workflow.getGraph().getNodes();
        for (Node node : nodes) {
            if (node.getGUI().getBodyColor() == color) {
                list.add(node);
            }
        }
        return list;
    }

    private int getRunningNodeCountDynamically() {
        return this.getNodeCountWithBodyColor(NodeState.EXECUTING.color);
    }

    private int getFailedNodeCountDynamically() {
        int failed = 0;
        /*
         * Take rerun time for each failed Node into consideration
         */
        for (Node node2 : this.getFailedNodesDynamically()) {
            if (this.retryCounter.containsKey(node2)) {
                int rerunTimes = this.retryCounter.get(node2).intValue();
                if (rerunTimes < MAXIMUM_RETRY_TIME) {
                    failed++;
                }
            } else {
                failed++;
            }
        }
        return failed;
    }

    private int getWaitingNodeCountDynamically() {
        return this.getNodeCountWithBodyColor(NodeGUI.DEFAULT_BODY_COLOR);
    }

    private int getNodeCountWithBodyColor(Color color) {
        int sum = 0;
        List<NodeImpl> nodes = this.workflow.getGraph().getNodes();
        for (Node node : nodes) {
            if (node.getGUI().getBodyColor() == color) {
                ++sum;
            }
        }
        return sum;
    }

    private String getEPR(WSNode wsNode) {
        Iterable<WsdlService> services = wsNode.getComponent().getWSDL().services();
        Iterator<WsdlService> iterator = services.iterator();
        if (iterator.hasNext()) {
            Iterable<WsdlPort> ports = iterator.next().ports();
            Iterator<WsdlPort> portIterator = ports.iterator();
            if (portIterator.hasNext()) {
                WsdlPort port = portIterator.next();
                Iterable children = port.xml().children();
                Iterator childIterator = children.iterator();
                while (childIterator.hasNext()) {
                    Object next = childIterator.next();
                    if (next instanceof XmlElementWithViewsImpl) {
                        org.xmlpull.infoset.XmlAttribute epr = ((XmlElementWithViewsImpl) next).attribute("location");
                        return epr.getValue();
                    }
                }
            }
        }
        return null;
    }
}