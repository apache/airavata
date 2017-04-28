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
package org.apache.airavata.xbaya.jython.script;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.system.ConstantNode;
import org.apache.airavata.workflow.model.graph.system.EndifNode;
import org.apache.airavata.workflow.model.graph.system.IfNode;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.MemoNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaVersion;

public class JythonScript {

    /**
     * GFAC_VARIABLE
     */
    public static final String GFAC_VARIABLE = "gfacURL";

    /**
     * BROKER_LOC_VARIABLE
     */
    public static final String BROKER_URL_VARIABLE = "brokerURL";

    /**
     * MESSAGE_BOX_URL_VARIABLE
     */
    public static final String MESSAGE_BOX_URL_VARIABLE = "msgBoxURL";

    /**
     * TOPIC_VARIABLE
     */
    public static final String TOPIC_VARIABLE = "topic";

//    private static final String NOTIFICATION_CLASS = StringUtil.getClassName(NotificationSender.class);

    private static final String WORKFLOW_STARTED_METHOD = "workflowStarted";

    private static final String WORKFLOW_COMPLETED_METHOD = "workflowFinished";

    private static final String WORKFLOW_INCOMPLETED_METHOD = "workflowFailed";

    private static final String SETUP_METHOD = "setup";

    private static final String SET_OPERATION_METHOD = "setOperation";

    private static final String SET_INPUT_METHOD = "setInput";

    private static final String GET_OUTPUT_METHOD = "getOutput";

    private static final String WAIT_METHOD = "waitToFinish";

    private static final String INVOKE_METHOD = "invoke";

    private static final String GET_PROPERTY_METHOD = "getProperty";

    private static final String NOTIFICATION_VARIABLE = "notifier";

    private static final String PROPERTIES_VARIABLE = "properties";

    private static final String INVOKER_SUFFIX = "_invoker";

    private static final String QNAME_SUFFIX = "_qname";

    private static final String VALUE_SUFFIX = "_value";

    private static final String TAB = "    ";

    /**
     * Suffix to put after node ID to create WSDL ID.
     */
    private static final String WSDL_SUFFIX = "_wsdl";

    private XBayaConfiguration configuration;

    private Workflow workflow;

    private Graph graph;

    /**
     * Collection of nodes that are not invoked yet
     */
    private Collection<Node> notYetInvokedNodes;

    /**
     * Collection of nodes that are executing now, and not finished yet.
     */
    private Collection<Node> executingNodes;

    private Collection<InputNode> inputNodes;

    private Collection<OutputNode> outputNodes;

    /**
     * List of command-line arguments
     */
    private List<String> arguments;

    private String scriptString;

    /**
     * Constructs a JythonScript.
     * 
     * @param workflow
     * @param configuration
     */
    public JythonScript(Workflow workflow, XBayaConfiguration configuration) {
        this.workflow = workflow;
        this.configuration = configuration;
        this.graph = this.workflow.getGraph();

        this.arguments = new ArrayList<String>();

        this.notYetInvokedNodes = new LinkedList<Node>();
        for (Node node : this.graph.getNodes()) {
            if (!(node instanceof MemoNode)) {
                this.notYetInvokedNodes.add(node);
            }
        }
        this.executingNodes = new LinkedList<Node>();
        this.inputNodes = GraphUtil.getInputNodes(this.graph);
        this.outputNodes = GraphUtil.getOutputNodes(this.graph);
    }

    /**
     * Returns the WSDL ID.
     * 
     * @param node
     * 
     * @return the WSDL ID
     */
    public static String getWSDLID(Node node) {
        return node.getID() + WSDL_SUFFIX;
    }

    /**
     * @return The Jython script string
     */
    public String getJythonString() {
        return this.scriptString;
    }

    /**
     * 
     * @param parameters
     * @return the jython script with prefilled argument
     */
    public String getJythonString(List<String> parameters) {
        int index = this.scriptString.indexOf("# Process command-line arguments.");
        StringBuilder builder = new StringBuilder(this.scriptString.substring(0, index));
        builder.append("sys.argv = [");
        for (String string : parameters) {
            builder.append("'");
            builder.append(string);
            builder.append("',");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        builder.append("\n");
        builder.append(this.scriptString.substring(index));
        return builder.toString();
    }

    /**
     * @param warnings
     *            returns the warning messages.
     * @return true if the workflow is valid; false otherwise.
     */
    public boolean validate(List<String> warnings) {
        // Empty
        if (this.graph.getNodes().size() == 0) {
            String message = "The workflow is empty.";
            warnings.add(message);
        }

        // Input ports need to be connected.
        Collection<Port> inputPorts = GraphUtil.getPorts(this.graph, Port.Kind.DATA_IN);
        for (Port inputPort : inputPorts) {
            Collection<Port> fromPorts = inputPort.getFromPorts();
            if (fromPorts.size() == 0) {
                Node node = inputPort.getNode();
                String message = node.getID() + " has an unconnected input " + inputPort.getName();
                warnings.add(message);
            }
        }

        // Input nodes need to be connected.
        for (InputNode inputNode : this.inputNodes) {
            if (inputNode.getPort().getToPorts().size() == 0) {
                String message = inputNode.getID() + " is not connected to any service.";
                warnings.add(message);
            }
        }

        // Cycle
        if (GraphUtil.containsCycle(this.graph)) {
            String message = "There is a cycle in the workflow, only acyclic workflows are supported";
            warnings.add(message);
        }

        // Constants are not supported.
        List<ConstantNode> constantNodes = GraphUtil.getNodes(this.graph, ConstantNode.class);
        if (constantNodes.size() > 0) {
            String message = "Constants are not supported for Jython scripts.";
            warnings.add(message);
        }

        // If/endif are not supported.
        List<IfNode> ifNodes = GraphUtil.getNodes(this.graph, IfNode.class);
        List<EndifNode> endifNodes = GraphUtil.getNodes(this.graph, EndifNode.class);
        if (ifNodes.size() > 0 || endifNodes.size() > 0) {
            String message = "If/endif are not supported for Jython scripts.";
            warnings.add(message);
        }

        if (warnings.size() > 0) {
            return false;
        } else {
            // No error.
            return true;
        }
    }

    /**
     * @throws org.apache.airavata.workflow.model.graph.GraphException
     */
    public void create() throws GraphException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        writeHeader(printWriter);
        writeParameters(printWriter);
        writeWSDLLocations(printWriter);
        writeCommandLineArguments(printWriter);
        writeSetup(printWriter);
        writeInvocations(printWriter);
        writeOutputs(printWriter);
        writeWaitAll(printWriter);
        writeFooter(printWriter);

        printWriter.close();
        this.scriptString = stringWriter.toString();
    }

    /**
     * @param pw
     */
    private void writeCommandLineArguments(PrintWriter pw) {
        pw.println("def usage():");
        pw.println(TAB + "print '''");
        pw.println("Options: -help");
        pw.println(TAB + TAB + " -f properties_file");
        for (String argument : this.arguments) {
            pw.println(TAB + TAB + " -" + argument + " value");
        }
        pw.println("'''");
        pw.println(TAB + "sys.exit(0)");
        pw.println();

        pw.println("# Process command-line arguments.");
        pw.println("if sys.argv[0][0] != '-':");
        pw.println(TAB + "sys.argv = sys.argv[1:]");
        pw.println("while sys.argv:");
        pw.println(TAB + "if sys.argv[0] == '-f':");
        pw.println(TAB + TAB + "# Read parameters from a file.");
        pw.println(TAB + TAB + "propertyFilename = sys.argv[1]");
        pw.println(TAB + TAB + "inputStream = FileInputStream(propertyFilename)");
        pw.println(TAB + TAB + PROPERTIES_VARIABLE + ".load(inputStream)");

        for (String argument : this.arguments) {
            pw.println(TAB + "elif sys.argv[0] == '-" + argument + "':");
            pw.println(TAB + TAB + PROPERTIES_VARIABLE + ".put('" + argument + "', sys.argv[1])");
        }

        pw.println(TAB + "else:");
        pw.println(TAB + TAB + "usage()");
        pw.println(TAB + "sys.argv = sys.argv[2:]");
        pw.println();
    }

    /**
     * Writes import statements
     * 
     * @param pw
     */
    private void writeHeader(PrintWriter pw) {
        pw.println("#");
        pw.println("# This script is automatically generated by " + XBayaConstants.APPLICATION_NAME + " "
                + XBayaVersion.VERSION + ".");
        pw.println("#");
        pw.println();
        pw.println("import sys, thread");
        pw.println("from java.lang import Throwable");
        pw.println("from java.util import Properties");
        pw.println("from java.io import FileInputStream");
        pw.println("from javax.xml.namespace import QName");
        pw.println();
    }

    /**
     * Handles parameters
     * 
     * @param pw
     */
    private void writeParameters(PrintWriter pw) {
        pw.println(PROPERTIES_VARIABLE + " = Properties()");
        pw.println();
        pw.println("# Set up defaut parameter values.");

        writeSetProperty(BROKER_URL_VARIABLE, XBayaConstants.DEFAULT_BROKER_URL, pw);
        writeSetProperty(MESSAGE_BOX_URL_VARIABLE, this.configuration.getMessageBoxURL(), pw);
        writeSetProperty(TOPIC_VARIABLE, XBayaConstants.DEFAULT_TOPIC, pw);

        for (InputNode paramNode : this.inputNodes) {
            writeParameter(paramNode, pw);
            this.notYetInvokedNodes.remove(paramNode);
        }

        pw.println();
    }

    /**
     * @param inputNode
     * @param pw
     */
    private void writeParameter(InputNode inputNode, PrintWriter pw) {
        String id = inputNode.getID();
        Object value = inputNode.getDefaultValue();
        String valueString = "";
        if (value instanceof String) {
            valueString = (String) value;
        }
        writeSetProperty(id, valueString, pw);
    }

    /**
     * @param pw
     */
    private void writeWSDLLocations(PrintWriter pw) {
        pw.println("# Set up default WSDL URLs.");
        for (WSNode node : GraphUtil.getWSNodes(this.graph)) {
            writeWSDLLocation(node, pw);
        }
        pw.println();

    }

    /**
     * @param node
     * @param pw
     */
    private void writeWSDLLocation(WSNode node, PrintWriter pw) {
        String defaultWsdlLocation = ""; // TODO
        writeSetProperty(getWSDLID(node), defaultWsdlLocation, pw);
    }

    private void writeSetProperty(String name, URI uri, PrintWriter pw) {
        writeSetProperty(name, StringUtil.toString(uri), pw);
    }

    private void writeSetProperty(String name, String value, PrintWriter pw) {
        if (value == null) {
            value = "";
        }
        pw.println(PROPERTIES_VARIABLE + ".setProperty(");
        pw.println(TAB + TAB + "'" + name + "',");
        pw.println(TAB + TAB + "'" + value + "')");

        // This will be used to read from command-line
        this.arguments.add(name);
    }

    /**
     * @param pw
     */
    private void writeSetup(PrintWriter pw) {
        // Initialize some variables.
        pw.println(GFAC_VARIABLE + " = " + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('" + GFAC_VARIABLE
                + "')");
        pw.println(TOPIC_VARIABLE + " = " + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('" + TOPIC_VARIABLE
                + "')");
        pw.println(BROKER_URL_VARIABLE + " = " + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('"
                + BROKER_URL_VARIABLE + "')");
        pw.println(MESSAGE_BOX_URL_VARIABLE + " = " + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('"
                + MESSAGE_BOX_URL_VARIABLE + "')");

        // Initialize a notification sender.
//        pw.println(NOTIFICATION_VARIABLE + " = " + NOTIFICATION_CLASS + "(" + BROKER_URL_VARIABLE + ", "
//                + TOPIC_VARIABLE + ")");

        // Send a START_WORKFLOW notification.
        pw.println(NOTIFICATION_VARIABLE + "." + WORKFLOW_STARTED_METHOD + "(");
        boolean first = true;
        for (InputNode inputNode : this.inputNodes) {
            String id = inputNode.getID();
            if (first) {
                first = false;
            } else {
                pw.println(",");
            }
            pw.print(TAB + id + "=" + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('" + id + "')");
        }
        pw.println(")");

        pw.println();

        // The biggining of try
        pw.println("try:");
    }

    /**
     * @param pw
     * @throws GraphException
     */
    private void writeInvocations(PrintWriter pw) throws GraphException {
        Collection<Node> nextNodes = getNextNodes();
        while (nextNodes.size() > 0) {
            // If there are more than one nodes to invoke, they can run
            // concurrently using threads.
            boolean thread = (nextNodes.size() > 1);
            for (Node node : nextNodes) {
                if (node instanceof WSNode) {
                    WSNode wsNode = (WSNode) node;
                    writeInvocation(wsNode, thread, pw);

                } else {
                    // TODO conditions, loops might come here.
                }
                this.notYetInvokedNodes.remove(node);
            }

            nextNodes = getNextNodes();
        }
    }

    /**
     * @param node
     * @param thread
     * @param pw
     */
    private void writeInvocation(WSNode node, boolean thread, PrintWriter pw) {
        String id = node.getID();
        String wsdlID = getWSDLID(node);

        WSComponent component = node.getComponent();
        QName portTypeQName = component.getPortTypeQName();
        String operation = component.getOperationName();

        pw.println(TAB + "# Invoke " + id + ".");
        pw.println(TAB + id + QNAME_SUFFIX + " = QName('" + portTypeQName.getNamespaceURI() + "', '"
                + portTypeQName.getLocalPart() + "')");
        pw.println(TAB + wsdlID + " = " + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('" + wsdlID + "')");
        pw.println(TAB + id + INVOKER_SUFFIX + " = " + "(" + id + QNAME_SUFFIX + ", " + wsdlID + ", '"
                + id + "',");
        pw.println(TAB + TAB + MESSAGE_BOX_URL_VARIABLE + ", " + GFAC_VARIABLE + ", " + NOTIFICATION_VARIABLE + ")");

        pw.println(TAB + "def " + INVOKE_METHOD + id + "():");
        pw.println(TAB + TAB + id + INVOKER_SUFFIX + "." + SETUP_METHOD + "()");
        pw.println(TAB + TAB + id + INVOKER_SUFFIX + "." + SET_OPERATION_METHOD + "('" + operation + "')");

        // Ports
        for (Port port : node.getInputPorts()) {
            String portName = port.getName();
            String value;
            Node fromNode = port.getFromNode();
            if (fromNode instanceof InputNode) {
                value = PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('" + fromNode.getID() + "')";
            } else {
                Port fromPort = port.getFromPort();
                value = "" + fromNode.getID() + INVOKER_SUFFIX + "." + GET_OUTPUT_METHOD + "('" + fromPort.getName()
                        + "')";

                // This might try to remove a node that has been removed
                // already, but it's OK.
                this.executingNodes.remove(fromNode);
            }
            pw.println(TAB + TAB + portName + VALUE_SUFFIX + " = " + value);
            pw.println(TAB + TAB + id + INVOKER_SUFFIX + "." + SET_INPUT_METHOD + "('" + portName + "', " + portName
                    + VALUE_SUFFIX + ")");
        }

        pw.println(TAB + TAB + "print 'Invoking " + id + ".'");
        pw.println(TAB + TAB + id + INVOKER_SUFFIX + "." + INVOKE_METHOD + "()");

        if (thread) {
            pw.println(TAB + "thread.start_new_thread(" + INVOKE_METHOD + id + ", ())");
        } else {
            pw.println(TAB + INVOKE_METHOD + id + "()");
        }

        pw.println();

        this.executingNodes.add(node);
    }

    private void writeOutputs(PrintWriter pw) throws GraphException {
        for (OutputNode outputNode : this.outputNodes) {
            writeOutput(outputNode, pw);
        }
    }

    private void writeOutput(OutputNode node, PrintWriter pw) throws GraphException {
        String id = node.getID();
        Port port = node.getPort();

        Node fromNode = port.getFromNode();
        if (fromNode == null) {
            throw new GraphException("Output parameter has to be connected to some node.");
        }
        Port fromPort = port.getFromPort();
        if (fromNode instanceof InputNode) {
            // The OutputNode is directly connected to an InputNode.
            pw.println(TAB + id + VALUE_SUFFIX + " = " + PROPERTIES_VARIABLE + "." + GET_PROPERTY_METHOD + "('"
                    + fromNode.getID() + "')");
        } else {
            pw.println(TAB + "# Wait output " + id);
            pw.println(TAB + id + VALUE_SUFFIX + " = " + fromNode.getID() + INVOKER_SUFFIX + "." + GET_OUTPUT_METHOD
                    + "('" + fromPort.getName() + "')");
        }
        pw.println(TAB + "print '" + id + " = ', " + id + VALUE_SUFFIX);

        // This might try to remove a node that has been removed
        // already, but it's OK.
        this.executingNodes.remove(fromNode);

        pw.println();
    }

    /**
     * @param pw
     */
    private void writeWaitAll(PrintWriter pw) {
        pw.println(TAB + "# Wait all executing services.");
        for (Node node : this.executingNodes) {
            writeWait(node, pw);
        }
        pw.println();
    }

    /**
     * @param node
     * @param pw
     */
    private void writeWait(Node node, PrintWriter pw) {
        String id = node.getID();
        pw.println(TAB + "print 'Waiting " + id + " to be done.'");
        pw.println(TAB + id + INVOKER_SUFFIX + "." + WAIT_METHOD + "()");
    }

    /**
     * @param pw
     */
    private void writeFooter(PrintWriter pw) {
        // Send a COMPLETE_WORKFLOW notification.
        pw.println(TAB + NOTIFICATION_VARIABLE + "." + WORKFLOW_COMPLETED_METHOD + "(");
        boolean first = true;
        for (OutputNode node : this.outputNodes) {
            if (first) {
                first = false;
            } else {
                pw.println(",");
            }
            String id = node.getID();
            pw.print(TAB + TAB + id + "=" + id + VALUE_SUFFIX);
        }
        pw.println(")");

        pw.println(TAB + "print 'Everything is done successfully.'");

        pw.println();
        pw.println("except Throwable, e:");
        pw.println(TAB + "print 'Error: ', e");
        pw.println(TAB + NOTIFICATION_VARIABLE + "." + WORKFLOW_INCOMPLETED_METHOD + "(e)");
    }

    private Collection<Node> getNextNodes() throws GraphException {
        Collection<Node> nextNodes = new ArrayList<Node>();
        for (Node node : this.notYetInvokedNodes) {
            if (isNextNode(node)) {
                nextNodes.add(node);
            }
        }
        return nextNodes;
    }

    /**
     * Checks is a specified node can be executed next. A node can be executed if all the previous node are done or
     * there is no input ports.
     * 
     * @param node
     *            the specified node
     * @return true if the specified node can be executed next; false otherwise
     * @throws GraphException
     */
    private boolean isNextNode(Node node) throws GraphException {
        if (node instanceof OutputNode) {
            return false;
        }
        for (Port port : node.getInputPorts()) {
            Collection<Node> fromNodes = port.getFromNodes();
            if (fromNodes.isEmpty()) {
                throw new GraphException("There is a port that is not connected to any.");
            } else {
                for (Node fromNode : fromNodes) {
                    if (this.notYetInvokedNodes.contains(fromNode)) {
                        // There is a node that should be executed before this
                        // node.
                        return false;
                    }
                }
            }
        }
        Port port = node.getControlInPort();
        if (port != null) {
            Collection<Node> fromNodes = port.getFromNodes();
            for (Node fromNode : fromNodes) {
                if (this.notYetInvokedNodes.contains(fromNode)) {
                    return false;
                }
            }
        }
        return true;
    }
}