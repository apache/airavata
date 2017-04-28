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
//package org.apache.airavata.xbaya.scufl.script;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//
//import javax.xml.namespace.QName;
//
//import org.apache.airavata.workflow.model.component.ws.WSComponent;
//import org.apache.airavata.workflow.model.graph.DataPort;
//import org.apache.airavata.workflow.model.graph.GraphException;
//import org.apache.airavata.workflow.model.graph.Node;
//import org.apache.airavata.workflow.model.graph.Port;
//import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
//import org.apache.airavata.workflow.model.graph.system.ConstantNode;
//import org.apache.airavata.workflow.model.graph.system.EndifNode;
//import org.apache.airavata.workflow.model.graph.system.IfNode;
//import org.apache.airavata.workflow.model.graph.system.InputNode;
//import org.apache.airavata.workflow.model.graph.system.MemoNode;
//import org.apache.airavata.workflow.model.graph.system.OutputNode;
//import org.apache.airavata.workflow.model.graph.util.GraphUtil;
//import org.apache.airavata.workflow.model.graph.ws.WSGraph;
//import org.apache.airavata.workflow.model.graph.ws.WSNode;
//import org.apache.airavata.workflow.model.wf.Workflow;
//import org.apache.airavata.xbaya.XBayaConfiguration;
//import org.xmlpull.infoset.impl.XmlElementWithViewsImpl;
//import org.xmlpull.v1.builder.XmlBuilderException;
//import org.xmlpull.v1.builder.XmlDocument;
//import org.xmlpull.v1.builder.XmlElement;
//import org.xmlpull.v1.builder.XmlInfosetBuilder;
//import org.xmlpull.v1.builder.XmlNamespace;
//
//import xsul.XmlConstants;
//import xsul5.wsdl.WsdlMessage;
//import xsul5.wsdl.WsdlMessagePart;
//import xsul5.wsdl.WsdlPort;
//import xsul5.wsdl.WsdlPortType;
//import xsul5.wsdl.WsdlPortTypeOperation;
//import xsul5.wsdl.WsdlService;
//
//public class ScuflScript {
//
//    private Workflow workflow;
//
//    private XBayaConfiguration configuration;
//
//    private WSGraph graph;
//
//    private ArrayList<String> arguments;
//
//    private List<InputNode> inputNodes;
//
//    private List<OutputNode> outputNodes;
//
//    private XmlInfosetBuilder builder = XmlConstants.BUILDER;
//
//    private LinkedList<Node> notYetInvokedNodes;
//
//    private LinkedList<Node> executingNodes;
//
//    private XmlNamespace scuflNS = builder.newNamespace("s", "http://org.embl.ebi.escience/xscufl/0.1alpha");
//
//    private Map<String, XmlElement> sources = new HashMap<String, XmlElement>();
//
//    private List<XmlElement> links = new ArrayList<XmlElement>();
//
//    private List<XmlElement> sinks = new ArrayList<XmlElement>();
//
//    private XmlDocument script;
//
//    public ScuflScript(Workflow workflow, XBayaConfiguration configuration) {
//        this.workflow = workflow;
//        this.configuration = configuration;
//        this.graph = this.workflow.getGraph();
//
//        this.arguments = new ArrayList<String>();
//
//        this.notYetInvokedNodes = new LinkedList<Node>();
//        for (Node node : this.graph.getNodes()) {
//            if (!(node instanceof MemoNode)) {
//                this.notYetInvokedNodes.add(node);
//            }
//        }
//        this.executingNodes = new LinkedList<Node>();
//        this.inputNodes = GraphUtil.getInputNodes(this.graph);
//        this.outputNodes = GraphUtil.getOutputNodes(this.graph);
//    }
//
//    public void create() throws GraphException {
//
//        XmlDocument doc = builder.newDocument();
//
//        XmlElement scufl = doc.addDocumentElement(scuflNS, "scufl");
//        scufl.addAttribute("version", "0.2");
//        scufl.addAttribute("log", "0");
//        XmlElement description = scufl.addElement(scuflNS, "workflowdescription");
//        description.addAttribute("lsid", "urn:lsid:net.sf.taverna:wfDefinition:" + UUID.randomUUID());
//        description.addAttribute("author", "");
//        description.addAttribute("title", workflow.getName());
//        writeServices(scufl);
//
//        writeSplitors(scufl);
//
//        // add links
//        for (XmlElement link : this.links) {
//            scufl.addElement(link);
//        }
//
//        // add source
//        for (String key : this.sources.keySet()) {
//            scufl.addElement(this.sources.get(key));
//        }
//
//        // add sinks
//        for (XmlElement sink : this.sinks) {
//            scufl.addElement(sink);
//        }
//
//        this.script = doc;
//
//    }
//
//    public String getScript() {
//        return builder.serializeToString(this.script);
//    }
//
//    /**
//     * @param scufl
//     * @throws GraphException
//     */
//    private void writeSplitors(XmlElement scufl) throws GraphException {
//
//        Collection<Node> nextNodes = getNextNodes();
//        while (nextNodes.size() > 0) {
//            for (Node node : nextNodes) {
//
//                if (node instanceof WSNode) {
//                    WSNode wsNode = (WSNode) node;
//                    node.getInputPorts();
//                    writeSplitorPerService(scufl, wsNode);
//
//                } else {
//                    // TODO conditions, loops might come here.
//                }
//                this.notYetInvokedNodes.remove(node);
//                nextNodes = getNextNodes();
//            }
//        }
//    }
//
//    /**
//     * @param scufl
//     * @param node
//     * @throws GraphException
//     */
//    private void writeSplitorPerService(XmlElement scufl, WSNode node) throws GraphException {
//        List<DataPort> inputPorts = node.getInputPorts();
//        XmlElement processor = scufl.addElement(scuflNS, "processor");
//        processor.addAttribute("name", getValidName(node) + "InputMessagePartXML");
//        XmlElement local = processor.addElement(scuflNS, "local");
//        local.addChild(0, "org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter");
//        XmlElement extensions = local.addElement(scuflNS, "extensions");
//
//        QName inputName = getInputElementName(node);
//        if (null == inputName) {
//            throw new GraphException("No Valid input type found for WS Node" + node.getName());
//        }
//        if (node.getOutputPorts().size() != 1) {
//            throw new GraphException("Number of out ports in WS node " + node.getName() + "is invalid:"
//                    + node.getOutputPorts().size());
//        }
//        DataPort outputPort = node.getOutputPort(0);
//
//        WSComponent component = node.getComponent();
//        String inputPartName = component.getInputPartName();
//        String inputTypeName = component.getInputTypeName();
//
//        XmlElement complexType = extensions.addElement(scuflNS, "complextype");
//        complexType.addAttribute("optional", "false");
//        complexType.addAttribute("unbounded", "false");
//        complexType.addAttribute("typename", inputTypeName);
//
//        String spliterName = inputPartName;
//        complexType.addAttribute("name", spliterName);
//        complexType.addAttribute("qname", inputName.toString());
//
//        XmlElement element = complexType.addElement(scuflNS, "elements");
//        for (DataPort port : inputPorts) {
//            if ("http://www.w3.org/2001/XMLSchema".equals(port.getType().getNamespaceURI())) {
//                XmlElement baseType = element.addElement(scuflNS, "basetype");
//                baseType.addAttribute("optional", "false");
//                baseType.addAttribute("unbounded", "false");
//                baseType.addAttribute("typename", port.getType().getLocalPart());
//                baseType.addAttribute("name", port.getName());
//                baseType.addAttribute("qname", inputTypeName + "&gt;" + port.getName());
//
//            }
//            // all the sources are written here
//            // the links from input nodes to the spiters are done here
//            // links from the from node output splitter to the this service's
//            // inputsplitter is done here
//
//            if (port.getFromNode() instanceof InputNode) {
//                XmlElement source = builder.newFragment(scuflNS, "source");
//                source.addAttribute("name", port.getFromNode().getID());
//                if (!sourceExist(port.getFromNode().getID())) {
//                    this.sources.put(port.getFromNode().getID(), source);
//                }
//                XmlElement link = builder.newFragment(scuflNS, "link");
//                link.addAttribute("source", port.getFromNode().getID());
//                link.addAttribute("sink", getValidName(node) + "InputMessagePartXML:" + port.getName());
//                this.links.add(link);
//
//            } else if (port.getFromNode() instanceof WSNode) {
//                XmlElement link = builder.newFragment(scuflNS, "link");
//                if (port.getFromNode().getOutputPorts().size() != 1) {
//                    throw new GraphException("Number of out ports in from WS node " + port.getFromNode().getName()
//                            + "is invalid:" + node.getOutputPorts().size());
//                }
//                link.addAttribute("source", getValidName((WSNode) port.getFromNode()) + "OutputMessagePartXML:"
//                        + port.getFromNode().getOutputPort(0).getName());
//                link.addAttribute("sink", getValidName(node) + "InputMessagePartXML:" + port.getName());
//                this.links.add(link);
//            } else {
//                throw new GraphException("Unhandled from node type:" + port.getFromNode() + " for node"
//                        + node.getName());
//            }
//        }
//
//        // link from the spliter to the service
//
//        XmlElement link = builder.newFragment(scuflNS, "link");
//        link.addAttribute("source", getValidName(node) + "InputMessagePartXML:output");
//        link.addAttribute("sink", getValidName(node) + ":" + spliterName);
//        this.links.add(link);
//
//        // link from service out to the ouput spliter
//
//        link = builder.newFragment(scuflNS, "link");
//        link.addAttribute("source", getValidName(node) + ":" + node.getComponent().getOutputPartName());
//        link.addAttribute("sink", getValidName(node) + "OutputMessagePartXML:input");
//        this.links.add(link);
//
//        // /outspiltor
//        XmlElement outProcessor = scufl.addElement(scuflNS, "processor");
//        outProcessor.addAttribute("name", getValidName(node) + "OutputMessagePartXML");
//        XmlElement outLocal = outProcessor.addElement(scuflNS, "local");
//        outLocal.addChild(0, "org.embl.ebi.escience.scuflworkers.java.XMLOutputSplitter");
//        XmlElement outExtensions = outLocal.addElement(scuflNS, "extensions");
//        XmlElement outComplextype = outExtensions.addElement(scuflNS, "complextype");
//        outComplextype.addAttribute("optional", "false");
//        outComplextype.addAttribute("unbounded", "false");
//        outComplextype.addAttribute("typename", component.getOutputTypeName());
//        outComplextype.addAttribute("name", component.getOutputPartName());
//        QName outputName = getOutputElementName(node);
//        if (null == outputName) {
//            throw new GraphException("No Valid output type found for WS Node" + node.getName());
//        }
//        outComplextype.addAttribute("qname", outputName.toString());
//        XmlElement elements = outComplextype.addElement(scuflNS, "elements");
//        XmlElement outBaseType = elements.addElement(scuflNS, "basetype");
//        outBaseType.addAttribute("optional", "false");
//        outBaseType.addAttribute("unbounded", "false");
//
//        outBaseType.addAttribute("typename", outputPort.getType().getLocalPart());
//        String Z = component.getOutputPort(0).getName();
//        outBaseType.addAttribute("name", Z);
//
//        outBaseType.addAttribute("qname", component.getOutputTypeName() + "&gt;" + Z);
//
//        List<DataPort> outputPorts = node.getOutputPorts();
//        for (DataPort port : outputPorts) {
//            List<Node> toNodes = port.getToNodes();
//            for (Node toNode : toNodes) {
//                if (toNode instanceof OutputNode) {
//                    if ("http://www.w3.org/2001/XMLSchema".equals(port.getType().getNamespaceURI())) {
//                        XmlElement sink = builder.newFragment(scuflNS, "sink");
//                        sink.addAttribute("name", toNode.getID());
//                        sinks.add(sink);
//                        link = builder.newFragment(scuflNS, "link");
//                        link.addAttribute("source", getValidName(node) + "OutputMessagePartXML:" + outputPort.getName());
//                        link.addAttribute("sink", toNode.getID());
//                        this.links.add(link);
//                    }
//                }
//            }
//        }
//
//    }
//
//    private boolean sourceExist(String name) {
//        Set<String> keys = this.sources.keySet();
//        for (String string : keys) {
//            if (name.equals(string))
//                return true;
//        }
//        return false;
//    }
//
//    /**
//     * @param node
//     * @return
//     * @throws GraphException
//     */
//    private QName getInputElementName(WSNode node) throws GraphException {
//        WSComponent component = node.getComponent();
//        String portTypeName = component.getPortTypeQName().getLocalPart();
//        WsdlPortType portType = component.getWSDL().getPortType(portTypeName);
//        WsdlPortTypeOperation operation = portType.getOperation(component.getOperationName());
//        QName message = operation.getInput().getMessage();
//        WsdlMessage wsdlMessage = component.getWSDL().getMessage(message.getLocalPart());
//        Iterator<WsdlMessagePart> iterator = wsdlMessage.parts().iterator();
//        QName inputName = null;
//        if (iterator.hasNext()) {
//            inputName = iterator.next().getElement();
//        } else {
//            throw new GraphException("No input part found for WS Node" + node.getName());
//        }
//        return inputName;
//    }
//
//    private QName getOutputElementName(WSNode node) throws GraphException {
//        WSComponent component = node.getComponent();
//        String portTypeName = component.getPortTypeQName().getLocalPart();
//        WsdlPortType portType = component.getWSDL().getPortType(portTypeName);
//        WsdlPortTypeOperation operation = portType.getOperation(component.getOperationName());
//        QName message = operation.getOutput().getMessage();
//        WsdlMessage wsdlMessage = component.getWSDL().getMessage(message.getLocalPart());
//        Iterator<WsdlMessagePart> iterator = wsdlMessage.parts().iterator();
//        QName inputName = null;
//        if (iterator.hasNext()) {
//            inputName = iterator.next().getElement();
//        } else {
//            throw new GraphException("No output part found for WS Node" + node.getName());
//        }
//        return inputName;
//    }
//
//    private void writeServices(XmlElement scufl) throws GraphException {
//
//        Collection<NodeImpl> nextNodes = this.graph.getNodes();
//        for (NodeImpl node : nextNodes) {
//            if (node instanceof WSNode) {
//                WSNode wsNode = (WSNode) node;
//                createWSProcess(wsNode, scufl);
//            }
//        }
//    }
//
//    private XmlElement createWSProcess(WSNode node, XmlElement scufl) throws GraphException, XmlBuilderException {
//
//        XmlElement processor = scufl.addElement(scuflNS, "processor");
//        String name = getValidName(node);
//        processor.addAttribute("name", name);
//        XmlElement description = processor.addElement(scuflNS, "description");
//        String txt = node.getComponent().getDescription();
//        if (null == txt) {
//            description.addChild(name);
//        } else {
//            description.addChild(txt);
//        }
//
//        XmlElement arbitrarywsdl = processor.addElement(scuflNS, "arbitrarywsdl");
//        XmlElement wsdl = arbitrarywsdl.addElement(scuflNS, "wsdl");
//
//        String epr = getEPR(node);
//        if (null == epr) {
//            throw new GraphException("EPR not found for the WS-node:" + builder.serializeToString(node));
//        }
//        wsdl.addChild(epr + "?wsdl");
//
//        XmlElement operation = arbitrarywsdl.addElement(scuflNS, "operation");
//        operation.addChild(node.getOperationName());
//
//        return processor;
//
//    }
//
//    /**
//     * @param node
//     * @return
//     */
//    private String getValidName(WSNode node) {
//        return node.getID();
//        // String name = node.getName();
//        // if (name.indexOf(":") != -1) {
//        // name = name.substring(0, name.indexOf(":"));
//        // }
//        // return name;
//    }
//
//    /**
//     * @param wsNode
//     */
//    private String getEPR(WSNode wsNode) {
//        Iterable<WsdlService> services = wsNode.getComponent().getWSDL().services();
//        Iterator<WsdlService> iterator = services.iterator();
//        if (iterator.hasNext()) {
//            Iterable<WsdlPort> ports = iterator.next().ports();
//            Iterator<WsdlPort> portIterator = ports.iterator();
//            if (portIterator.hasNext()) {
//                WsdlPort port = portIterator.next();
//                Iterable children = port.xml().children();
//                Iterator childIterator = children.iterator();
//                while (childIterator.hasNext()) {
//                    Object next = childIterator.next();
//                    if (next instanceof XmlElementWithViewsImpl) {
//                        org.xmlpull.infoset.XmlAttribute epr = ((XmlElementWithViewsImpl) next).attribute("location");
//                        return epr.getValue();
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    public boolean validate(List<String> warnings) {
//        // Empty
//        if (this.graph.getNodes().size() == 0) {
//            String message = "The workflow is empty.";
//            warnings.add(message);
//        }
//
//        // Input ports need to be connected.
//        Collection<Port> inputPorts = GraphUtil.getPorts(this.graph, Port.Kind.DATA_IN);
//        for (Port inputPort : inputPorts) {
//            Collection<Port> fromPorts = inputPort.getFromPorts();
//            if (fromPorts.size() == 0) {
//                Node node = inputPort.getNode();
//                String message = node.getID() + " has an unconnected input " + inputPort.getName();
//                warnings.add(message);
//            }
//        }
//
//        // Input nodes need to be connected.
//        for (InputNode inputNode : this.inputNodes) {
//            if (inputNode.getPort().getToPorts().size() == 0) {
//                String message = inputNode.getID() + " is not connected to any service.";
//                warnings.add(message);
//            }
//        }
//
//        // Cycle
//        if (GraphUtil.containsCycle(this.graph)) {
//            String message = "There is a cycle in the workflow.";
//            warnings.add(message);
//        }
//
//        // Constants are not supported.
//        List<ConstantNode> constantNodes = GraphUtil.getNodes(this.graph, ConstantNode.class);
//        if (constantNodes.size() > 0) {
//            String message = "Constants are not supported for Scufl scripts.";
//            warnings.add(message);
//        }
//
//        // If/endif are not supported.
//        List<IfNode> ifNodes = GraphUtil.getNodes(this.graph, IfNode.class);
//        List<EndifNode> endifNodes = GraphUtil.getNodes(this.graph, EndifNode.class);
//        if (ifNodes.size() > 0 || endifNodes.size() > 0) {
//            String message = "If/endif are not supported for Scufl scripts.";
//            warnings.add(message);
//        }
//
//        if (warnings.size() > 0) {
//            return false;
//        } else {
//            // No error.
//            return true;
//        }
//    }
//
//    private Collection<Node> getNextNodes() throws GraphException {
//        Collection<Node> nextNodes = new ArrayList<Node>();
//        for (Node node : this.notYetInvokedNodes) {
//            if (isNextNode(node)) {
//                nextNodes.add(node);
//            }
//        }
//        return nextNodes;
//    }
//
//    private boolean isNextNode(Node node) throws GraphException {
//        if (node instanceof OutputNode) {
//            return false;
//        }
//        for (Port port : node.getInputPorts()) {
//            Collection<Node> fromNodes = port.getFromNodes();
//            if (fromNodes.isEmpty()) {
//                throw new GraphException("There is a port that is not connected to any.");
//            } else {
//                for (Node fromNode : fromNodes) {
//                    if (this.notYetInvokedNodes.contains(fromNode)) {
//                        // There is a node that should be executed before this
//                        // node.
//                        return false;
//                    }
//                }
//            }
//        }
//        Port port = node.getControlInPort();
//        if (port != null) {
//            Collection<Node> fromNodes = port.getFromNodes();
//            for (Node fromNode : fromNodes) {
//                if (this.notYetInvokedNodes.contains(fromNode)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//}