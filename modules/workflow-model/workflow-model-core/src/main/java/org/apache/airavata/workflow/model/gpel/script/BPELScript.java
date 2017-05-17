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
package org.apache.airavata.workflow.model.gpel.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.ComponentPort;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.system.BlockNode;
import org.apache.airavata.workflow.model.graph.system.ConstantNode;
import org.apache.airavata.workflow.model.graph.system.EndBlockNode;
import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
import org.apache.airavata.workflow.model.graph.system.EndifNode;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.apache.airavata.workflow.model.graph.system.IfNode;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.MemoNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.wf.Workflow;
//import org.gpel.GpelConstants;
//import org.gpel.model.GpelAssign;
//import org.gpel.model.GpelAssignCopy;
//import org.gpel.model.GpelAssignCopyFrom;
//import org.gpel.model.GpelAssignCopyTo;
//import org.gpel.model.GpelCondition;
//import org.gpel.model.GpelElse;
//import org.gpel.model.GpelFlow;
//import org.gpel.model.GpelForEach;
//import org.gpel.model.GpelIf;
//import org.gpel.model.GpelInvoke;
//import org.gpel.model.GpelProcess;
//import org.gpel.model.GpelReceive;
//import org.gpel.model.GpelReply;
//import org.gpel.model.GpelScope;
//import org.gpel.model.GpelSequence;
//import org.gpel.model.GpelVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

//import xsul5.wsdl.WsdlPortType;
//import xsul5.wsdl.plnk.PartnerLinkRole;
//import xsul5.wsdl.plnk.PartnerLinkType;

public class BPELScript {

    /**
     * GPELNS
     */
    public static final String GPELNS = "http://schemas.gpel.org/2005/grid-process/";

    /**
     * GPEL
     */
    public static final String GPEL = "gpel";

    /**
     * BPEL
     */
    public static final String BPEL = "bpel";

    /**
     * BPEL2_NS
     */
    public static final String BPEL2_NS = "http://docs.oasis-open.org/wsbpel/2.0/process/executable";

    /**
     * Name of workflow partner link
     */
    public static final String WORKFLOW_PARTNER_LINK = "workflowUserPartner";

    private static final String TARGET_NS_NAME = "http://www.extreme.indiana.edu/xwf/bpel/";

    private static final String WORKFLOW_INPUT_NAME = "WorkflowInput";

    private static final String WORKFLOW_OUTPUT_NAME = "WorkflowOutput";

    private static final String PARTNER_LINK_NAME_SUFFIX = "Partner";

    private static final String PARTNER_LINK_TYPE_SUFFIX = "LT";

    private static final String MY_ROLE_SUFFIX = "Provider";

    private static final String PARTNER_ROLE_SUFFIX = "Service";

    private static final String INPUT_SUFFIX = "Input";

    private static final String OUTPUT_SUFFIX = "Output";

    // It's empty to match with node ID.
    private static final String INVOKE_NAME_PREFIX = "";

    private static final String TYPENS_SUFFIX = "typens";

    private static final String ARRAY_SUFIX = "Array";

    private static final String FOREACH_VALUE_SUFFIX = "Value";

    private static final Logger logger = LoggerFactory.getLogger(BPELScript.class);

    private Workflow workflow;

    private Graph graph;

    private WorkflowWSDL workflowWSDL;

    /**
     * List of nodes that are not processed yet
     */
    private List<Node> remainNodes;

//    private GpelProcess process;

    private XmlNamespace targetNamespace;

    private XmlNamespace typesNamespace;

    private String workflowPrefix;

    private XmlNamespace bpelNS;

    /**
     *
     * Constructs a BPELScript.
     *
     * @param workflow
     */
    public BPELScript(Workflow workflow) {
        this(workflow, "Run");
    }

    /**
     * Constructs a BPELScript.
     *
     * @param workflow
     */
    public BPELScript(Workflow workflow, String operationName) {
        this.workflow = workflow;
        this.graph = workflow.getGraph();
        this.workflowWSDL = new WorkflowWSDL(this.workflow, operationName);
    }

    /**
     * Returns the GPEL Process.
     *
     * @return The GPEL Process
     */
//    public GpelProcess getGpelProcess() {
//        return this.process;
//    }

    /**
     * @return the WSDL of the workflow
     */
    public WorkflowWSDL getWorkflowWSDL() {
        return this.workflowWSDL;
    }

    /**
     * Returns the WSDLs of components in the workflow.
     *
     * @return The WSDLs of components.
     */
    public Collection<XmlElement> getWSDLs() {
        Collection<XmlElement> wsdls = new ArrayList<XmlElement>();
        for (Node node : this.graph.getNodes()) {
            if (node instanceof WSNode) {
                WSNode wsNode = (WSNode) node;
                WSComponent component = wsNode.getComponent();
                wsdls.add(component.toXML());
            }
        }
        return wsdls;
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
            ComponentPort componentPort = inputPort.getComponentPort();
            if (componentPort instanceof WSComponentPort) {
                WSComponentPort wsComponentPort = (WSComponentPort) componentPort;
                if (wsComponentPort.isOptional()) {
                    // optional input.
                    continue;
                }
            }
            Collection<Port> fromPorts = inputPort.getFromPorts();
            if (fromPorts.size() == 0) {
                Node node = inputPort.getNode();
                String message = node.getID() + " has an unconnected input " + inputPort.getName();
                warnings.add(message);
            }
        }

        // Input nodes need to be connected.
        List<InputNode> inputNodes = GraphUtil.getNodes(this.graph, InputNode.class);
        for (InputNode inputNode : inputNodes) {
            if (inputNode.getPort().getToPorts().size() == 0) {
                String message = inputNode.getID() + " is not connected to any service.";
                warnings.add(message);
            }
        }

        // Cycle
        if (GraphUtil.containsCycle(this.graph)) {
            String message = "There is a cycle in the workflow.";
            warnings.add(message);
        }

        // XXX bypass some checks for debugging.
        String debug = System.getProperty("xbaya.debug");
        if (!"true".equalsIgnoreCase(debug)) {

            // split/merge are not supported.
            List<ForEachNode> splitNodes = GraphUtil.getNodes(this.graph, ForEachNode.class);
            List<EndForEachNode> mergeNodes = GraphUtil.getNodes(this.graph, EndForEachNode.class);
            if (splitNodes.size() > 0 || mergeNodes.size() > 0) {
                String message = "Split/merge are not supported yet.";
                warnings.add(message);
            }

            // block are not supported.
            List<BlockNode> blockNodes = GraphUtil.getNodes(this.graph, BlockNode.class);
            List<EndBlockNode> endBlockNodes = GraphUtil.getNodes(this.graph, EndBlockNode.class);
            if (blockNodes.size() > 0 || endBlockNodes.size() > 0) {
                String message = "Blocks/EndBlocks are not supported yet.";
                warnings.add(message);
            }

            // // receive is not supported.
            // List<ReceiveNode> receiveNodes = GraphUtil.getNodes(this.graph,
            // ReceiveNode.class);
            // if (receiveNodes.size() > 0) {
            // String message = "Receive is not supported yet.";
            // warnings.add(message);
            // }
        }

        if (warnings.size() > 0) {
            return false;
        } else {
            // No error.
            return true;
        }
    }

//    /**
//     * @throws GraphException
//     */
//    public void create(BPELScriptType type) throws GraphException {
//        try {
//            // Create WSDL for the workflow.
//            // This has to be done before generating the BPEL document.
//            this.workflowWSDL.create();
//
//            this.remainNodes = new LinkedList<Node>(this.graph.getNodes());
//
//            String bpelTargetNamespace = TARGET_NS_NAME + this.graph.getID() + "/";
//
//            this.workflowPrefix = StringUtil.convertToJavaIdentifier(this.graph.getName());
//
//            if (BPELScriptType.BPEL2 == type) {
//                GpelConstants.GPEL_NS = XmlInfosetBuilder.newInstance().newNamespace(BPEL, BPEL2_NS);
//                this.bpelNS = XmlInfosetBuilder.newInstance().newNamespace(BPEL, BPEL2_NS);
//                this.process = new GpelProcess(bpelNS, bpelTargetNamespace);
//            } else if (BPELScriptType.GPEL == type) {
//                GpelConstants.GPEL_NS = XmlInfosetBuilder.newInstance().newNamespace(GPEL, GPELNS);
//                this.bpelNS = XmlInfosetBuilder.newInstance().newNamespace(GPEL, GPELNS);
//                this.process = new GpelProcess(bpelNS, bpelTargetNamespace);
//            } else {
//                throw new GraphException("Unknown BPEL type " + type);
//            }
//
//            // Target namespace of the workflow WSDL
//            this.targetNamespace = this.process.xml().declareNamespace(this.workflowWSDL.getTargetNamespace());
//
//            // Types namespace of the workflow WSDL
//            this.typesNamespace = this.process.xml().declareNamespace(this.workflowWSDL.getTypesNamespace());
//
//            // xsd
//            XMLUtil.declareNamespaceIfNecessary(WSConstants.XSD_NS.getPrefix(), WSConstants.XSD_NS.getName(), false,
//                    this.process.xml());
//
//            this.process.setActivity(createMainSequence());
//
//            // comment
//            addComment();
//
//            // Validate
//            this.process.xmlValidate();
//
//            logger.debug(this.process.xmlStringPretty());
//        } catch (RuntimeException e) {
//            throw new GraphException(e);
//        }
//    }

    /**
     * @param nodeID
     * @return The partner link name.
     */
    public static String createPartnerLinkName(String nodeID) {
        return nodeID + PARTNER_LINK_NAME_SUFFIX;
    }

    private void addComment() {
//        XmlComment comment = this.process.xml().newComment(
//                "\nThis document is automatically generated by " + WorkflowConstants.APPLICATION_NAME_ + " "
//                        + ApplicationVersion.VERSION + ".\n");
//        this.process.xml().insertChild(0, "\n");
//        this.process.xml().insertChild(0, comment);
//        this.process.xml().insertChild(0, "\n");
    }

//    private GpelSequence createMainSequence() throws GraphException {
//        GpelSequence sequence = new GpelSequence(this.bpelNS);
//
//        // Remove InputNodes and MemoNodes.
//        removeUnnecessaryNodes(this.remainNodes);
//
//        addInitialReceive(sequence);
//
//        addBlock(this.remainNodes, sequence);
//
//        addFinalReply(sequence);
//
//        if (this.remainNodes.size() > 0) {
//            throw new GraphException("Some node(s) are not connected.");
//        }
//
//        return sequence;
//    }
//
//    private void addInitialReceive(GpelSequence sequence) {
//        // Create a partner link
//        String partnerLinkName = WORKFLOW_PARTNER_LINK;
//        XmlNamespace partnerLinkTypeNS = this.workflowWSDL.getTargetNamespace();
//        String partnerLinkTypeName = this.workflowPrefix + PARTNER_LINK_TYPE_SUFFIX;
//        String myRollName = this.workflowPrefix + MY_ROLE_SUFFIX;
//
//        this.process.addPartnerLink(partnerLinkName, partnerLinkTypeNS, partnerLinkTypeName, myRollName, null);
//        this.workflowWSDL.addPartnerLinkTypeAndRoll(partnerLinkTypeName, myRollName,
//                this.workflowWSDL.getPortTypeQName());
//
//        // Create a variable
//        this.process.addMessageVariable(WORKFLOW_INPUT_NAME, this.targetNamespace,
//                this.workflowWSDL.getWorkflowInputMessageName());
//
//        GpelReceive receive = new GpelReceive(this.bpelNS, WORKFLOW_PARTNER_LINK, this.workflowWSDL.getPortTypeQName(),
//                this.workflowWSDL.getWorkflowOperationName());
//        receive.setGpelVariableName(WORKFLOW_INPUT_NAME);
//        sequence.addActivity(receive);
//    }
//
//    private void addFinalReply(GpelSequence sequence) throws GraphException {
//        // Create a variable
//        this.process.addMessageVariable(WORKFLOW_OUTPUT_NAME, this.targetNamespace,
//                this.workflowWSDL.getWorkflowOutputMessageName());
//
//        List<GpelAssignCopy> copies = new ArrayList<GpelAssignCopy>();
//        List<OutputNode> outputNodes = GraphUtil.getNodes(this.graph, OutputNode.class);
//        this.remainNodes.removeAll(outputNodes);
//        for (OutputNode outputNode : outputNodes) {
//            Port port = outputNode.getPort();
//            GpelAssignCopyFrom from = createAssignCopyFrom(port);
//            GpelAssignCopyTo to = createAssignCopyTo(port, false);
//
//            copies.add(new GpelAssignCopy(this.bpelNS, from, to));
//        }
//
//        if (copies.size() != 0) {
//            // When there is no outputs, we don't create assign.
//            GpelAssign assign = new GpelAssign(this.bpelNS, copies);
//            sequence.addActivity(assign);
//        }
//
//        GpelReply reply = new GpelReply(this.bpelNS, WORKFLOW_PARTNER_LINK, this.workflowWSDL.getPortTypeQName(),
//                this.workflowWSDL.getWorkflowOperationName());
//        reply.setVariableName(WORKFLOW_OUTPUT_NAME);
//        sequence.addActivity(reply);
//    }
//
//    /**
//     * @param block
//     * @param sequence
//     * @throws GraphException
//     */
//    private void addBlock(Collection<Node> block, GpelSequence sequence) throws GraphException {
//        List<Node> nextNodes = getNextExecutableNodes(block);
//        while (nextNodes.size() > 0) {
//            block.removeAll(nextNodes);
//            removeUnnecessaryNodes(nextNodes);
//            if (nextNodes.size() == 0) {
//                // Everything was uncessary nodes (constants, etc.). Move on.
//            } else if (nextNodes.size() == 1) {
//                addSingle(nextNodes.get(0), block, sequence);
//            } else if (nextNodes.size() > 1) {
//                // XXX The algorithm used here is not efficient. It introduces
//                // unnessary barriers.
//                addFlow(nextNodes, block, sequence);
//            } else {
//                // Should not happen.
//                throw new WorkflowRuntimeException("nextNodes.size(): " + nextNodes.size());
//            }
//            nextNodes = getNextExecutableNodes(block);
//        }
//    }
//
//    private void addFlow(List<Node> nextNodes, Collection<Node> block, GpelSequence sequence) throws GraphException {
//        GpelFlow flow = new GpelFlow(this.bpelNS);
//        for (Node node : nextNodes) {
//            GpelSequence childSequence = new GpelSequence(this.bpelNS);
//            flow.addActivity(childSequence);
//            addSingle(node, block, childSequence);
//        }
//        sequence.addActivity(flow);
//    }
//
//    // TODO: Add xml to BPEL
//    private void addSingle(Node node, Collection<Node> block, GpelSequence sequence) throws GraphException {
//        logger.debug("Processing + " + node.getID());
//        if (node instanceof WSNode) {
//            addInvoke((WSNode) node, sequence);
//        } else if (node instanceof ConstantNode) {
//            // nothing
//        } else if (node instanceof ForEachNode) {
//            addForEach((ForEachNode) node, block, sequence);
//        } else if (node instanceof EndForEachNode) {
//            // nothing.
//        } else if (node instanceof IfNode) {
//            addIf((IfNode) node, block, sequence);
//        } else if (node instanceof EndifNode) {
//            // nothing
//        } else if (node instanceof ReceiveNode) {
//            addReceive((ReceiveNode) node, sequence);
//        } else if (node instanceof BlockNode) {
//            addBlock((BlockNode) node, block, sequence);
//        } else if (node instanceof EndBlockNode) {
//            // nothing
//        } else if (node instanceof StreamSourceNode) {
//            addStreamSource((StreamSourceNode) node, sequence);
//        } else if (node instanceof ExitNode) {
//            addExit((ExitNode) node, sequence);
//        } else if (node instanceof ResourceNode) {
//            // nothing
//        } else {
//
//            throw new GraphException(node.getClass().getName() + " is not supported.");
//        }
//    }
//
//    /**
//     * @param node
//     * @param sequence
//     */
//    private void addStreamSource(StreamSourceNode node, GpelSequence sequence) {
//        GpelFlow flow = new GpelFlow(this.bpelNS);
//        new GpelSequence(this.bpelNS);
//        sequence.addActivity(flow);
//
//    }
//
//    /**
//     * @param node
//     * @param sequence
//     */
//    private void addExit(ExitNode node, GpelSequence sequence) {
//        sequence.xml().addElement(this.bpelNS, "exit");
//    }
//
//    private void addInvoke(WSNode node, GpelSequence sequence) throws GraphException {
//        String id = node.getID();
//
//        WSComponent wsdlComponent = node.getComponent();
//        String operation = wsdlComponent.getOperationName();
//
//        QName portTypeQName = wsdlComponent.getPortTypeQName();
//        XmlNamespace namespace = XMLUtil.declareNamespaceIfNecessary(id.toLowerCase(), portTypeQName.getNamespaceURI(),
//                false, this.process.xml());
//
//        // Variable
//        String inputVariableName = id + INPUT_SUFFIX;
//        this.process.addMessageVariable(inputVariableName, namespace, portTypeQName.getLocalPart());
//        String outputVariableName = id + OUTPUT_SUFFIX;
//        this.process.addMessageVariable(outputVariableName, namespace, portTypeQName.getLocalPart());
//
//        // Assign
//        List<GpelAssignCopy> copies = new ArrayList<GpelAssignCopy>();
//        for (Port port : node.getInputPorts()) {
//            Port fromPort = port.getFromPort();
//            if (fromPort == null) {
//                // optional input
//                continue;
//            }
//            GpelAssignCopyFrom from = createAssignCopyFrom(port);
//            GpelAssignCopyTo to = createAssignCopyTo(port, true);
//
//            GpelAssignCopy copy = new GpelAssignCopy(this.bpelNS, from, to);
//            copies.add(copy);
//        }
//
//        GpelAssign assign = new GpelAssign(this.bpelNS, copies);
//        sequence.addActivity(assign);
//
//        PartnerLinkRole partnerRoll = this.workflowWSDL.getPartnerRoll(portTypeQName);
//        if (partnerRoll == null) {
//            String partnerLinkTypeName = id + PARTNER_LINK_TYPE_SUFFIX;
//            String partnerRollName = id + PARTNER_ROLE_SUFFIX;
//            partnerRoll = this.workflowWSDL.addPartnerLinkTypeAndRoll(partnerLinkTypeName, partnerRollName,
//                    portTypeQName);
//        }
//        PartnerLinkType partnerLinkType = partnerRoll.getPartnerLinkType();
//
//        // partnerLink
//        String partnerLinkName = createPartnerLinkName(id);
//        XmlNamespace partnerLinkTypeNS = this.targetNamespace;
//        this.process.addPartnerLink(partnerLinkName, partnerLinkTypeNS, partnerLinkType.getName(), null,
//                partnerRoll.getName());
//
//        // Invoke
//        GpelInvoke invoke = new GpelInvoke(this.bpelNS, partnerLinkName, namespace, portTypeQName.getLocalPart(),
//                operation);
//        invoke.setName(INVOKE_NAME_PREFIX + id);
//        invoke.setInputVariableName(inputVariableName);
//        invoke.setOutputVariableName(outputVariableName);
//
//        sequence.addActivity(invoke);
//    }

//    /**
//     * Creates BpelAssignCopyFrom for a specified port.
//     *
//     * @param port
//     * @return The BpelAssignCopyFrom created
//     * @throws GraphException
//     */
//    private GpelAssignCopyFrom createAssignCopyFrom(Port port) throws GraphException {
//        GpelAssignCopyFrom from = new GpelAssignCopyFrom(this.bpelNS);
//
//        Port fromPort = port.getFromPort();
//        Node fromNode = fromPort.getNode();
//        if (fromNode instanceof InputNode) {
//            from.setVariable(WORKFLOW_INPUT_NAME);
//            from.setPart(WorkflowWSDL.INPUT_PART_NAME);
//            from.setQuery("/" + this.typesNamespace.getPrefix() + ":"
//                    + this.workflowWSDL.getWorkflowInputMessageElelmentName() + "/" + fromNode.getID());
//        } else if (fromNode instanceof ConstantNode) {
//            ConstantNode constNode = (ConstantNode) fromNode;
//            Object value = constNode.getValue();
//            // The namaspace and name of the literal element will be set
//            // correctly in from.setLiteral().
//            XmlElement literalElement = XMLUtil.BUILDER.newFragment(GpelAssignCopyFrom.LITERAL_EL);
//            literalElement.addChild(value);
//            from.setLiteral(literalElement);
//        } else if (fromNode instanceof WSNode) {
//            String fromID = fromNode.getID();
//            WSComponent fromWsdlComponent = (WSComponent) fromNode.getComponent();
//
//            WSComponentPort fromWsdlPort = (WSComponentPort) fromPort.getComponentPort();
//
//            from.setVariable(fromID + OUTPUT_SUFFIX);
//            from.setPart(fromWsdlComponent.getOutputPartName());
//
//            if (fromWsdlPort.isSchemaUsed()) {
//                String typesTargetNamespace = fromWsdlPort.getTargetNamespace();
//                XmlNamespace namespace = XMLUtil.declareNamespaceIfNecessary(fromID.toLowerCase() + TYPENS_SUFFIX,
//                        typesTargetNamespace, false, this.process.xml());
//
//                from.setQuery("/" + namespace.getPrefix() + ":" + fromWsdlComponent.getOutputTypeName() + "/"
//                        + fromWsdlPort.getName());
//            } else {
//                // No query needed?
//            }
//        } else if (fromNode instanceof ForEachNode) {
//            from.setVariable(fromNode.getID() + FOREACH_VALUE_SUFFIX);
//        } else if (fromNode instanceof EndForEachNode) {
//            from.setVariable(fromNode.getID() + ARRAY_SUFIX);
//        } else if (fromNode instanceof EndifNode) {
//            // endif has multiple outputs, so we use port ID here.
//            from.setVariable(fromPort.getID() + OUTPUT_SUFFIX);
//        } else if (fromNode instanceof ReceiveNode) {
//            if (fromPort instanceof EPRPort) {
//                from.setPartnerLink(fromNode.getID() + PARTNER_LINK_NAME_SUFFIX);
//                from.setEndpointReference("myRole");
//            } else {
//                from.setVariable(fromNode.getID() + INPUT_SUFFIX);
//            }
//        } else if (fromNode instanceof InstanceNode) {
//            // no op
//        } else {
//            throw new GraphException("Unexpected node," + fromNode.getClass().getName() + " is connected");
//        }
//        return from;
//    }
//
//    /**
//     * Creates BpelAssignCopyFrom for a specified port.
//     *
//     * @param toPort
//     * @param input
//     * @return The GpelAssignCopyTo created
//     */
//    private GpelAssignCopyTo createAssignCopyTo(Port toPort, boolean input) {
//        GpelAssignCopyTo to = new GpelAssignCopyTo(this.bpelNS);
//
//        Node toNode = toPort.getNode();
//        if (toNode instanceof OutputNode) {
//            to.setVariable(WORKFLOW_OUTPUT_NAME);
//            to.setPart(WorkflowWSDL.OUTPUT_PART_NAME);
//            to.setQuery("/" + this.typesNamespace.getPrefix() + ":"
//                    + this.workflowWSDL.getWorkflowOutputMessageElementName() + "/" + toNode.getID());
//        } else {
//            WSComponentPort toComponentPort = (WSComponentPort) toPort.getComponentPort();
//
//            String toID = toNode.getID();
//            WSComponent toWSComponent = (WSComponent) toNode.getComponent();
//            to.setVariable(toID + INPUT_SUFFIX);
//            to.setPart(toWSComponent.getInputPartName());
//
//            if (toComponentPort.isSchemaUsed()) {
//                // Normal case.
//                // e.g. <part name="name" type="typens:fooType">
//                String typesTargetNamespace = toComponentPort.getTargetNamespace();
//                XmlNamespace namespace = XMLUtil.declareNamespaceIfNecessary(toID.toLowerCase() + TYPENS_SUFFIX,
//                        typesTargetNamespace, false, this.process.xml());
//
//                String typeName = input ? toWSComponent.getInputTypeName() : toWSComponent.getOutputTypeName();
//                to.setQuery("/" + namespace.getPrefix() + ":" + typeName + "/" + toComponentPort.getName());
//            } else {
//                // e.g. <part name="name" type="xsd:string">
//                // No query is needed?
//            }
//        }
//        return to;
//    }

    private void removeUnnecessaryNodes(List<Node> block) {
        List<Node> unnecessaryNodes = new ArrayList<Node>();
        for (Node node : block) {
            if (node instanceof InputNode || node instanceof MemoNode || node instanceof ConstantNode) {
                unnecessaryNodes.add(node);
            }
        }
        block.removeAll(unnecessaryNodes);
    }

    private List<Node> getNextExecutableNodes(Collection<Node> nodes) throws GraphException {
        List<Node> nextNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (isExecutable(node, nodes)) {
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
     * @param nodes
     *            List of nodes remained.
     * @return true if the specified node can be executed next; false otherwise
     * @throws GraphException
     */
    private boolean isExecutable(Node node, Collection<Node> nodes) throws GraphException {
        if (node instanceof OutputNode) {
            return false;
        }

        // Check data dependency.
        for (Port port : node.getInputPorts()) {
            Collection<Port> fromPorts = port.getFromPorts();
            for (Port fromPort : fromPorts) {
                if (fromPort instanceof EPRPort) {
                    continue;
                }
                Node fromNode = fromPort.getNode();
                if (nodes.contains(fromNode)) {
                    // There is a node that should be executed before this
                    // node.
                    return false;
                }
            }
        }

        // Check control dependency.
        Port port = node.getControlInPort();
        if (port != null) {
            Collection<Node> fromNodes = port.getFromNodes();
            for (Node fromNode : fromNodes) {
                if (nodes.contains(fromNode)) {
                    return false;
                }
            }
        }

        // special handling
        // This has to be at the end.
        if (node instanceof ForEachNode) {
            return isExecutable((ForEachNode) node, nodes, ForEachNode.class, EndForEachNode.class);
        } else if (node instanceof IfNode) {
            return isExecutable((IfNode) node, nodes, IfNode.class, EndifNode.class);
        }

        return true;
    }

    private <S extends Node, E extends Node> boolean isExecutable(S node, Collection<Node> nodes, Class<S> startClass,
            Class<E> endClass) throws GraphException {
        // copy remainNodes so that it doesn't break the original one.
        LinkedList<Node> copiedRemainNodes = new LinkedList<Node>(nodes);
        Set<Node> block = new HashSet<Node>();
        getSpecialBlock(node, 0, block, startClass, endClass);

        copiedRemainNodes.remove(node);
        while (block.size() > 0) {
            List<Node> doneNodeInBlock = new LinkedList<Node>();
            for (Node nodeInBlock : block) {
                if (isExecutable(nodeInBlock, copiedRemainNodes)) {
                    copiedRemainNodes.remove(nodeInBlock);
                    doneNodeInBlock.add(nodeInBlock);
                }
            }
            if (doneNodeInBlock.size() == 0) {
                // Cannot proceed anymore. This means that some nodes are depend
                // on outer nodes that haven't finished.
                return false;
            }
            block.removeAll(doneNodeInBlock);
        }
        return true;
    }

    /**
     * @param node
     * @param depth
     * @param block
     *            It's a set so that duplicated nodes won't be added.
     * @param startClass
     * @param endClass
     * @throws GraphException
     */
    private void getSpecialBlock(Node node, int depth, Set<Node> block, Class startClass, Class endClass)
            throws GraphException {
        List<Node> nextNodes = GraphUtil.getNextNodes(node);
        for (Node nextNode : nextNodes) {
            if (nextNode instanceof OutputNode) {
                throw new GraphException("Nodes after " + startClass.getName()
                        + " cannot be connected to the output without going through " + endClass.getName() + ".");
            } else if (endClass.isInstance(nextNode)) {
                block.add(nextNode);
                if (depth == 0) {
                    // Stop the recursion here.
                } else {
                    getSpecialBlock(nextNode, depth - 1, block, startClass, endClass);
                }
            } else if (startClass.isInstance(nextNode)) {
                // handle embedded forEach
                block.add(nextNode);
                getSpecialBlock(nextNode, depth + 1, block, startClass, endClass);
            } else {
                block.add(nextNode);
                getSpecialBlock(nextNode, depth, block, startClass, endClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <S extends Node, E extends Node> E findEndNode(Node node, int depth, Class<S> startClass, Class<E> endClass)
            throws GraphException {
        List<Node> nextNodes = GraphUtil.getNextNodes(node);
        for (Node nextNode : nextNodes) {
            if (nextNode instanceof OutputNode) {
                throw new GraphException("Nodes after " + startClass.getName()
                        + " cannot be connected to the output without going through " + endClass.getName() + ".");
            } else if (endClass.isInstance(nextNode)) {
                if (depth == 0) {
                    // Stop the recursion here.
                    return (E) nextNode; // This cast is OK.
                } else {
                    return findEndNode(nextNode, depth - 1, startClass, endClass);
                }
            } else if (startClass.isInstance(nextNode)) {
                // handle embedded forEach
                return findEndNode(nextNode, depth + 1, startClass, endClass);
            } else {
                return findEndNode(nextNode, depth, startClass, endClass);
            }
        }
        throw new GraphException("Cannot find matching  " + endClass.getName() + " for " + startClass.getName() + ".");
    }

//    private void addForEach(ForEachNode splitNode, Collection<Node> parentBlock, GpelSequence sequence)
//            throws GraphException {
//        Set<Node> forEachBlock = getForEachBlock(splitNode);
//        parentBlock.removeAll(forEachBlock);
//
//        GpelSequence subSequence = new GpelSequence(this.bpelNS);
//        GpelScope scope = new GpelScope(this.bpelNS, subSequence);
//
//        String arrayName = splitNode.getID() + ARRAY_SUFIX;
//        // TODO This should be type instead of messageType
//        this.process.addMessageVariable(arrayName, WSConstants.XSD_NS, WSConstants.XSD_ANY_TYPE.getLocalPart());
//
//        // Extract array from the previous node.
//        GpelAssignCopyFrom arrayFrom = createAssignCopyFrom(splitNode.getInputPort(0));
//        GpelAssignCopyTo arrayTo = new GpelAssignCopyTo(this.bpelNS);
//        arrayTo.setVariable(arrayName);
//        GpelAssignCopy arrayCopy = new GpelAssignCopy(this.bpelNS, arrayFrom, arrayTo);
//        GpelAssign arrayAssign = new GpelAssign(this.bpelNS, arrayCopy);
//        sequence.addActivity(arrayAssign);
//
//        // Extract a item from array
//        String valueName = splitNode.getID() + FOREACH_VALUE_SUFFIX;
//        // TODO set local variable in scope instead of process
//        // TODO This should be type instead of messageType
//        this.process.addMessageVariable(valueName, WSConstants.XSD_NS, WSConstants.XSD_ANY_TYPE.getLocalPart());
//        GpelAssignCopyFrom valueFrom = new GpelAssignCopyFrom(this.bpelNS);
//        valueFrom.setVariable(arrayName);
//        valueFrom.setQuery("$" + arrayName + "/*[$i]");
//        GpelAssignCopyTo valueTo = new GpelAssignCopyTo(this.bpelNS);
//        valueTo.setVariable(valueName);
//        GpelAssignCopy valueCopy = new GpelAssignCopy(this.bpelNS, valueFrom, valueTo);
//        GpelAssign valueAssign = new GpelAssign(this.bpelNS, valueCopy);
//
//        subSequence.addActivity(valueAssign);
//
//        addBlock(forEachBlock, subSequence);
//
//        Node mergeNode = getMergeNode(splitNode);
//        String outputName = mergeNode.getID() + ARRAY_SUFIX;
//        // TODO This should be type instead of messageType
//        this.process.addMessageVariable(outputName, WSConstants.XSD_NS, WSConstants.XSD_ANY_TYPE.getLocalPart());
//        GpelAssignCopyFrom outputFrom = createAssignCopyFrom(mergeNode.getInputPort(0).getFromPort());
//        GpelAssignCopyTo outputTo = new GpelAssignCopyTo(this.bpelNS);
//        outputTo.setVariable(outputName);
//        outputTo.setQuery("/value[$i]");
//        GpelAssignCopy outputCopy = new GpelAssignCopy(this.bpelNS, outputFrom, outputTo);
//        GpelAssign outputAssign = new GpelAssign(this.bpelNS, outputCopy);
//        subSequence.addActivity(outputAssign);
//
//        GpelForEach forEach = new GpelForEach(this.bpelNS, "i", "1", "count($" + arrayName + "/*)",
//                true /* parallel */, scope);
//
//        sequence.addActivity(forEach);
//    }

    private Set<Node> getForEachBlock(ForEachNode node) throws GraphException {
        Set<Node> forEachBlock = new HashSet<Node>();
        getSpecialBlock(node, 0, forEachBlock, ForEachNode.class, EndForEachNode.class);
        return forEachBlock;
    }

    private EndForEachNode getMergeNode(ForEachNode node) throws GraphException {
        return findEndNode(node, 0, ForEachNode.class, EndForEachNode.class);
    }

//    private void addIf(IfNode ifNode, Collection<Node> parentBlock, GpelSequence sequence) throws GraphException {
//        //
//        // Condition
//        //
//        String booleanExpression = ifNode.getXPath();
//        if (booleanExpression == null) {
//            throw new GraphException("XPath cannot be null");
//        }
//        // replace $1, $2,... with actual value.
//        List<? extends Port> inputPorts = ifNode.getInputPorts();
//        ArrayList<GpelAssignCopy> copies = new ArrayList<GpelAssignCopy>();
//        for (int i = 0; i < inputPorts.size(); i++) {
//            Port port = inputPorts.get(i);
//            Port fromPort = port.getFromPort();
//            if (fromPort != null) {
//                String variableName = port.getID() + INPUT_SUFFIX;
//
//                GpelVariable ifVar = new GpelVariable(this.process.xml().getNamespace(), variableName);
//                XmlNamespace xsdNS = process.xml().lookupNamespaceByName(WSConstants.XSD_NS_URI);
//                if (null != xsdNS && xsdNS.getPrefix() != null) {
//                    ifVar.xml().setAttributeValue("element",
//                            xsdNS.getPrefix() + ":" + WSConstants.XSD_ANY_TYPE.getLocalPart());
//                } else {
//                    this.process.xml().declareNamespace(WSConstants.XSD_NS);
//                    ifVar.xml().setAttributeValue("element",
//                            WSConstants.XSD_NS.getPrefix() + ":" + WSConstants.XSD_ANY_TYPE.getLocalPart());
//                }
//                this.process.getVariables().addVariable(ifVar);
//
//                GpelAssignCopyFrom from = createAssignCopyFrom(fromPort);
//                GpelAssignCopyTo to = new GpelAssignCopyTo(this.bpelNS);
//                to.setVariable(variableName);
//                GpelAssignCopy copy = new GpelAssignCopy(this.bpelNS, from, to);
//                copies.add(copy);
//
//                booleanExpression = booleanExpression.replaceAll("\\$" + i, "\\$" + variableName);
//            }
//        }
//        if (copies.size() > 0) {
//            GpelAssign assign = new GpelAssign(this.bpelNS, copies);
//            sequence.addActivity(assign);
//        }
//        GpelCondition condition = new GpelCondition(this.bpelNS, booleanExpression);
//
//        //
//        // If block
//        //
//        EndifNode endifNode = getEndifNode(ifNode);
//        GpelSequence ifSequence = createIfSequence(ifNode, endifNode, true, parentBlock);
//        GpelIf gpelIf = new GpelIf(this.bpelNS, condition, ifSequence);
//
//        //
//        // Else block
//        //
//        GpelSequence elseSequence = createIfSequence(ifNode, endifNode, false, parentBlock);
//        GpelElse gpelElse = new GpelElse(this.bpelNS, elseSequence);
//        gpelIf.setElse(gpelElse);
//
//        //
//        // Create global variables for endif.
//        //
//        for (Port outputPort : endifNode.getOutputPorts()) {
//            String variable = outputPort.getID() + OUTPUT_SUFFIX;
//            GpelVariable ifVar = new GpelVariable(this.process.xml().getNamespace(), variable);
//            XmlNamespace xsdNS = process.xml().lookupNamespaceByName(WSConstants.XSD_NS_URI);
//            if (null != xsdNS && xsdNS.getPrefix() != null) {
//                ifVar.xml().setAttributeValue("element",
//                        xsdNS.getPrefix() + ":" + WSConstants.XSD_ANY_TYPE.getLocalPart());
//            } else {
//                this.process.xml().declareNamespace(WSConstants.XSD_NS);
//                ifVar.xml().setAttributeValue("element",
//                        WSConstants.XSD_NS.getPrefix() + ":" + WSConstants.XSD_ANY_TYPE.getLocalPart());
//            }
//            this.process.getVariables().addVariable(ifVar);
//        }
//
//        sequence.addActivity(gpelIf);
//    }
//
//    private GpelSequence createIfSequence(IfNode ifNode, EndifNode endifNode, boolean ifBlock,
//            Collection<Node> parentBlock) throws GraphException {
//        Set<Node> block = getIfBlock(ifNode, ifBlock);
//        parentBlock.removeAll(block);
//        GpelSequence sequence = new GpelSequence(this.bpelNS);
//        addBlock(block, sequence);
//
//        // Create a copy to global variable.
//        List<DataPort> outputPorts = endifNode.getOutputPorts();
//        ArrayList<GpelAssignCopy> copies = new ArrayList<GpelAssignCopy>();
//        for (int i = 0; i < outputPorts.size(); i++) {
//            DataPort outputPort = outputPorts.get(i);
//            String variable = outputPort.getID() + OUTPUT_SUFFIX;
//            int index = ifBlock ? i : i + outputPorts.size();
//            DataPort inputPort = endifNode.getInputPort(index);
//            Port fromPort = inputPort.getFromPort();
//            GpelAssignCopyFrom from = createAssignCopyFrom(fromPort);
//            GpelAssignCopyTo to = new GpelAssignCopyTo(this.bpelNS);
//            to.setVariable(variable);
//            GpelAssignCopy copy = new GpelAssignCopy(this.bpelNS, from, to);
//            copies.add(copy);
//        }
//        GpelAssign assign = new GpelAssign(this.bpelNS, copies);
//        sequence.addActivity(assign);
//
//        return sequence;
//    }

    private Set<Node> getIfBlock(Node node, boolean ifBlock) throws GraphException {
        Set<Node> block = new HashSet<Node>();

        int index = ifBlock ? 0 : 1;
        Port controlOutPort = node.getControlOutPorts().get(index);
        for (Node nextNode : controlOutPort.getToNodes()) {
            block.add(nextNode);
            getSpecialBlock(nextNode, 0, block, IfNode.class, EndifNode.class);
        }
        return block;
    }

    private EndifNode getEndifNode(IfNode node) throws GraphException {
        return findEndNode(node, 0, IfNode.class, EndifNode.class);
    }

//    /**
//     * @param node
//     * @param block
//     * @param sequence
//     */
//    private void addReceive(ReceiveNode node, GpelSequence sequence) {
//        String id = node.getID();
//        String operationName = id;
//
//        // Create this operation and type in WSDL.
//        WsdlPortType portType = this.workflowWSDL.addReceivePortType(operationName, node);
//        QName portTypeQName = portType.getQName();
//
//        // Partner link
//        String partnerLinkName = createPartnerLinkName(id);
//        XmlNamespace partnerLinkTypeNS = this.targetNamespace;
//        String partnerLinkTypeName = id + PARTNER_LINK_TYPE_SUFFIX;
//        String myRollName = id + MY_ROLE_SUFFIX;
//        this.process.addPartnerLink(partnerLinkName, partnerLinkTypeNS, partnerLinkTypeName, myRollName, null);
//        this.workflowWSDL.addPartnerLinkTypeAndRoll(partnerLinkTypeName, myRollName, portTypeQName);
//
//        GpelReceive receive = new GpelReceive(this.bpelNS, partnerLinkName, portTypeQName, operationName);
//        String variableName = id + INPUT_SUFFIX;
//        this.process.addMessageVariable(variableName, WSConstants.XSD_NS, variableName);
//        receive.setGpelVariableName(variableName);
//        sequence.addActivity(receive);
//    }
//
//    private void addBlock(BlockNode blockNode, Collection<Node> parentBlock, GpelSequence sequence)
//            throws GraphException {
//
//        //
//        // normal block
//        //
//        EndBlockNode endBlockNode = getEndBlockNode(blockNode);
//        GpelSequence normalSequence = createBlockSequence(blockNode, endBlockNode, true, parentBlock);
//        GpelScope scope = new GpelScope(this.bpelNS, normalSequence);
//
//        //
//        // exception block
//        //
//        // GpelSequence compensationSequence = createBlockSequence(blockNode,
//        // endBlockNode, false, parentBlock);
//        // TODO GpelExceptionHandler handler
//        // = new GpelExceptionHandler(compensationSequence);
//        // scope.add(handler);
//
//        sequence.addActivity(scope);
//    }
//
//    private GpelSequence createBlockSequence(BlockNode blockNode, EndBlockNode endBlockNode, boolean blockBlock,
//            Collection<Node> parentBlock) throws GraphException {
//        Set<Node> block = getBlockBlock(blockNode, blockBlock);
//        parentBlock.removeAll(block);
//        GpelSequence sequence = new GpelSequence(this.bpelNS);
//        addBlock(block, sequence);
//
//        // Create a copy to global variable.
//        List<DataPort> outputPorts = endBlockNode.getOutputPorts();
//        ArrayList<GpelAssignCopy> copies = new ArrayList<GpelAssignCopy>();
//        for (int i = 0; i < outputPorts.size(); i++) {
//            DataPort outputPort = outputPorts.get(i);
//            String variable = outputPort.getID() + OUTPUT_SUFFIX;
//            int index = blockBlock ? i : i + outputPorts.size();
//            DataPort inputPort = endBlockNode.getInputPort(index);
//            Port fromPort = inputPort.getFromPort();
//            GpelAssignCopyFrom from = createAssignCopyFrom(fromPort);
//            GpelAssignCopyTo to = new GpelAssignCopyTo(this.bpelNS);
//            to.setVariable(variable);
//            GpelAssignCopy copy = new GpelAssignCopy(this.bpelNS, from, to);
//            copies.add(copy);
//        }
//        GpelAssign assign = new GpelAssign(this.bpelNS, copies);
//        sequence.addActivity(assign);
//
//        return sequence;
//    }

    private Set<Node> getBlockBlock(Node node, boolean blockBlock) throws GraphException {
        Set<Node> block = new HashSet<Node>();

        int index = blockBlock ? 0 : 1;
        Port controlOutPort = node.getControlOutPorts().get(index);
        for (Node nextNode : controlOutPort.getToNodes()) {
            block.add(nextNode);
            getSpecialBlock(nextNode, 0, block, BlockNode.class, EndBlockNode.class);
        }
        return block;
    }

    private EndBlockNode getEndBlockNode(BlockNode node) throws GraphException {
        return findEndNode(node, 0, BlockNode.class, EndBlockNode.class);
    }

}