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

package org.apache.airavata.xbaya.wf;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;

import org.apache.airavata.common.exception.UtilsException;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaExecutionState;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.XBayaVersion;
import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentFactory;
import org.apache.airavata.xbaya.component.ws.WSComponentKey;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.graph.ws.WSGraph;
import org.apache.airavata.xbaya.graph.ws.WSGraphFactory;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.ode.ODEBPELTransformer;
import org.apache.airavata.xbaya.ode.ODEDeploymentDescriptor;
import org.apache.airavata.xbaya.ode.ODEWSDLTransformer;
import org.apache.airavata.xbaya.ode.WSDLCleaner;
import org.apache.airavata.xbaya.streaming.StreamReceiveNode;
import org.apache.airavata.xbaya.streaming.StreamTransformer;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.gpel.GpelConstants;
import org.gpel.model.GpelProcess;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlInfosetBuilder;
import org.xmlpull.infoset.XmlNamespace;
import org.apache.airavata.common.utils.WSDLUtil;


import xsul5.MLogger;
import xsul5.XmlConstants;
import xsul5.wsdl.WsdlDefinitions;

public class Workflow {

    /**
     * Namespace prefix
     */
    public static final String NS_PREFIX_XWF = "xwf";

    /**
     * Namespace URI
     */
    public static final String NS_URI_XWF = XBayaConstants.NS_URI_XBAYA + "xwf";

    /**
     * Namespace
     */
    public static final XmlNamespace NS_XWF = XMLUtil.BUILDER.newNamespace(NS_PREFIX_XWF, NS_URI_XWF);

    /**
     * WORKFLOW_TAG
     */
    public static final String WORKFLOW_TAG = "workflow";

    private static final String VERSION_ATTRIBUTE = "version";

    private static final String WSDLS_TAG = "wsdls";

    private static final String WSDL_TAG = "wsdl";

    private static final String BPEL_TAG = "bpel";

    private static final String WORKFLOW_WSDL_TAG = "workflowWSDL";

    private static final String IMAGE_TAG = "image";

    private static final String ID_ATTRIBUTE = "id";

    private static final MLogger logger = MLogger.getLogger();

    private WSGraph graph;

    private BufferedImage image;

    private URI gpelTemplateID;

    private URI gpelInstanceID;

    private GpelProcess gpelProcess;

    private WsdlDefinitions workflowWSDL;

    private WsdlDefinitions odeInvokableWSDL;

    private WsdlDefinitions odeWorkflowWSDL;

    private GpelProcess odeProcess;

    private Map<String, WsdlDefinitions> odeWsdlMap;

    /**
     * used only during the parsing xwf or loading from GPEL.
     */
    private Map<String, WsdlDefinitions> wsdlMap;

    private XmlElement odeDeploymentDiscriptor;

    private QName qname;

    private XBayaExecutionState executionState = XBayaExecutionState.NONE;

    private WsdlDefinitions tridentWSDL;

    /**
     * Constructs a Workflow.
     */
    public Workflow() {
        this.wsdlMap = new HashMap<String, WsdlDefinitions>();

        // Create a empty graph here to avoid null checks.
        this.graph = WSGraphFactory.createGraph();
    }

    public static Workflow getWorkflow(Workflow parentWorkflow, List<Node> subworkflowNodes, String subworkflowName)
            throws GraphException {
        HashMap<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node node : subworkflowNodes) {
            nodeMap.put(node.getID(), node);
        }

        Workflow subWorkflow = parentWorkflow.clone();
        final LinkedList<NodeImpl> genericSubWorkflowNodes = GraphUtil.getGenericSubWorkflowNodes(parentWorkflow
                .getGraph());
        if (null == subworkflowName) {
            subWorkflow.setName(subWorkflow.getName() + "_subWorkflow_" + genericSubWorkflowNodes.size());
        } else {
            subWorkflow.setName(subworkflowName);
        }
        List<NodeImpl> allOldNodes = subWorkflow.getGraph().getNodes();
        LinkedList<NodeImpl> removeList = new LinkedList<NodeImpl>();
        for (NodeImpl nodeImpl : allOldNodes) {
            if (null == nodeMap.get(nodeImpl.getID())) {
                removeList.add(nodeImpl);
            }
        }
        for (NodeImpl nodeImpl : removeList) {
            subWorkflow.removeNode(nodeImpl);
        }

        List<NodeImpl> nodes = subWorkflow.getGraph().getNodes();
        for (int i = 0; i < nodes.size(); ++i) {
            NodeImpl nodeImpl = nodes.get(i);
            List<DataPort> inputPorts = nodeImpl.getInputPorts();
            int count = 0;
            for (DataPort dataPort : inputPorts) {
                if (dataPort.getFromNode() == null) {
                    Node inputNode = subWorkflow.addNode(new InputComponent());
                    inputNode.setPosition(new Point(Math.max(0, nodeImpl.getPosition().x - 200),
                            nodeImpl.getPosition().y + count * 50));
                    subWorkflow.getGraph().addEdge(inputNode.getOutputPort(0), dataPort);
                    ++count;
                }
            }
            count = 0;
            List<DataPort> outputPorts = nodeImpl.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                if (dataPort.getToNodes().size() == 0) {
                    Node outputNode = subWorkflow.addNode(new OutputComponent());
                    outputNode.setPosition(new Point(nodeImpl.getPosition().x + 200, nodeImpl.getPosition().y + count
                            * 50));
                    subWorkflow.getGraph().addEdge(dataPort, outputNode.getInputPort(0));
                    ++count;
                }
            }

        }

        return subWorkflow;
    }

    /**
     * Constructs a Workflow.
     *
     * @param workflowString Workflow XML in String.
     * @throws GraphException
     * @throws ComponentException
     */

    public Workflow(String workflowString) throws GraphException, ComponentException {
        this();
        try {
            XmlElement workflowElement = XMLUtil.stringToXmlElement(workflowString);
            parse(workflowElement);
        } catch (RuntimeException e) {
            throw new GraphException(e);
        }
    }

    /**
     * Constructs a Workflow.
     *
     * @param workflowElement
     * @throws GraphException
     * @throws ComponentException
     */
    public Workflow(XmlElement workflowElement) throws GraphException, ComponentException {
        this();
        parse(workflowElement);
    }

    public HashMap<String, LinkedList<Node>> partition() throws XBayaException {
        HashMap<String, LinkedList<Node>> partitionSets = this.graph.labelIntroduceJoinsAndGetSubSets();
        return partitionSets;

    }

    /**
     * This is used for ODE
     *
     * @return The Template ID like id
     */
    public URI getUniqueWorkflowName() {

        try {
            return new URI(XBayaConstants.LEAD_NS + "/" + this.getName());
        } catch (URISyntaxException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    public URI getNameSpace() {
        try {
            return new URI(XBayaConstants.LEAD_NS);
        } catch (URISyntaxException e) {
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * Returns the gpelInstanceID.
     *
     * @return The gpelInstanceID
     */
    public URI getGPELInstanceID() {
        return this.gpelInstanceID;
    }

    /**
     * Sets gpelInstanceID.
     *
     * @param gpelInstanceID The gpelInstanceID to set.
     */
    public void setGPELInstanceID(URI gpelInstanceID) {
        this.gpelInstanceID = gpelInstanceID;
    }

    /**
     * Returns the name.
     *
     * @return The name
     */
    public String getName() {
        return this.graph.getName();
    }

    /**
     * Sets name.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.graph.setName(name);
    }

    /**
     * Returns the description.
     *
     * @return The description
     */
    public String getDescription() {
        return this.graph.getDescription();
    }

    /**
     * Sets description.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.graph.setDescription(description);
    }

    /**
     * @return The metadata, appinfo.
     */
    public XmlElement getMetadata() {
        return this.graph.getMetadata();
    }

    /**
     * @param metadata
     */
    public void setMetadata(XmlElement metadata) {
        this.graph.setMetadata(metadata);
    }

    /**
     * @return The output metadata, appinfo.
     */
    public XmlElement getInputMetadata() {
        return this.graph.getInputMetadata();
    }

    /**
     * @return The input metadata, appinfo.
     */
    public XmlElement getOutputMetadata() {
        return this.graph.getOutputMetadata();
    }

    /**
     * Returns the graph.
     *
     * @return The graph
     */
    public WSGraph getGraph() {
        return this.graph;
    }

    /**
     * Sets graph.
     *
     * @param graph The graph to set.
     */
    public void setGraph(WSGraph graph) {
        this.graph = graph;
    }

    /**
     * Returns the image.
     *
     * @return The image
     */
    public BufferedImage getImage() {
        return this.image;
    }

    /**
     * Sets image.
     *
     * @param image The image to set.
     */
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Returns the gpelProcess.
     *
     * @return The gpelProcess
     */
    public GpelProcess getGpelProcess() {
        return this.gpelProcess;
    }

    /**
     * Sets gpelProcess.
     *
     * @param gpelProcess The gpelProcess to set.
     */
    public void setGpelProcess(GpelProcess gpelProcess) {
        this.gpelProcess = gpelProcess;
    }

    /**
     * Returns the workflowWSDL.
     *
     * @return The workflowWSDL
     */
    public WsdlDefinitions getWorkflowWSDL() {
        return this.workflowWSDL;
    }

    /**
     * Sets workflowWSDL.
     *
     * @param workflowWSDL The workflowWSDL to set.
     */
    public void setWorkflowWSDL(WsdlDefinitions workflowWSDL) {
        this.workflowWSDL = workflowWSDL;
    }

    /**
     * @return The set of WSDLs
     */
    public Map<String, WsdlDefinitions> getWSDLs() {

        Map<String, WsdlDefinitions> wsdls = new LinkedHashMap<String, WsdlDefinitions>();
        Map<WsdlDefinitions, String> ids = new HashMap<WsdlDefinitions, String>();
        // Use LinkedHashMap to preserve the order of WSDLs, which is useful for
        // some unit tests.

        for (WSNode node : GraphUtil.getNodes(this.graph, WSNode.class)) {
            WsdlDefinitions wsdl = node.getComponent().getWSDL();
            if (wsdls.containsValue(wsdl)) {
                String id = ids.get(wsdl);
                node.setWSDLID(id);
            } else {
                // Assign unique key
                String name = WSDLUtil.getWSDLName(wsdl);
                String id = StringUtil.convertToJavaIdentifier(name);
                while (wsdls.containsKey(id)) {
                    id = StringUtil.incrementName(id);
                }
                wsdls.put(id, wsdl);
                ids.put(wsdl, id);
                node.setWSDLID(id);
            }
        }
        return wsdls;
    }

    /**
     * This method is called by GPELClient during loading a workflow.
     *
     * @param id
     * @param wsdl
     */
    public void addWSDL(String id, WsdlDefinitions wsdl) {
        logger.finest("id: " + id);
        this.wsdlMap.put(id, wsdl);
    }

    /**
     * Creates a node from a specified component and adds it to the graph.
     *
     * @param component The specified component
     * @return The node added
     */
    public Node addNode(Component component) {
        Node node = component.createNode(this.graph);
        return node;
    }

    /**
     * Removes a specified node from the graph.
     *
     * @param node The specified node
     * @throws GraphException
     */
    public void removeNode(Node node) throws GraphException {
        this.graph.removeNode(node);
    }

    /**
     * Imports a specified workflow to the current workflow.
     *
     * @param workflow The specified workflow to import
     * @throws GraphException
     */
    public void importWorkflow(Workflow workflow) throws GraphException {
        this.graph.importGraph(workflow.getGraph());
    }

    /**
     * Returns the inputs of the workflow.
     *
     * @return The inputs of the workflow.
     * @throws ComponentException
     */
    public List<WSComponentPort> getInputs() throws ComponentException {
        if (this.workflowWSDL == null) {
            throw new IllegalStateException();
        }
        WSComponent component = WSComponentFactory.createComponent(this.workflowWSDL);
        return component.getInputPorts();
    }

    /**
     * Returns the outputs of the workflow.
     *
     * @return The outputs of the workflow.
     * @throws ComponentException
     */
    public List<WSComponentPort> getOutputs() throws ComponentException {
        if (this.workflowWSDL == null) {
            throw new IllegalStateException();
        }
        WSComponent component = WSComponentFactory.createComponent(this.workflowWSDL);
        return component.getOutputPorts();
    }

    /**
     * Returns the XML Text of the workflow.
     *
     * @return The XML Text of the workflow
     */
    @Deprecated
    public String toXMLText() {
        return XMLUtil.xmlElementToString(toXML());
    }

    /**
     * Returns the XmlElement of the workflow.
     *
     * @return The XmlElement of the workflow
     */
    public XmlElement toXML() {
        // This must be before graph.toXML() to set WSDL ID to each node.
        Map<String, WsdlDefinitions> wsdls = getWSDLs();

        XmlElement workflowElement = XMLUtil.BUILDER.newFragment(NS_XWF, WORKFLOW_TAG);

        // Version
        workflowElement.setAttributeValue(NS_XWF, VERSION_ATTRIBUTE, XBayaVersion.VERSION);

        // Date
        // TODO add modification time
        // XmlElement modifiedTimeElement = graphElement.addElement(
        // XgraphSchema.NS, "modifiedTime");
        // modifiedTimeElement.addChild(new GregorianCalendar().toString());

        // Graph
        workflowElement.addElement(this.graph.toXML());

        // WSDLs
        XmlElement wsdlsElement = workflowElement.addElement(NS_XWF, WSDLS_TAG);
        for (String id : wsdls.keySet()) {
            WsdlDefinitions wsdl = wsdls.get(id);
            XmlElement wsdlElement = wsdlsElement.addElement(NS_XWF, WSDL_TAG);
            wsdlElement.setAttributeValue(NS_XWF, ID_ATTRIBUTE, id);
            wsdlElement.setText(XMLUtil.xmlElementToString(wsdl.xml()));
        }

        // Image
        if (this.image != null) {
            try {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                ImageIO.write(this.image, XBayaConstants.PNG_FORMAT_NAME, outStream);
                byte[] bytes = outStream.toByteArray();
                byte[] base64 = Base64.encodeBase64Chunked(bytes);

                XmlElement imageElement = workflowElement.addElement(NS_XWF, IMAGE_TAG);
                imageElement.setText(new String(base64));
            } catch (IOException e) {
                // No image
                logger.caught(e);
            }
        }

        // BPEL
        if (this.gpelProcess != null) {
            XmlElement bpelElement = workflowElement.addElement(NS_XWF, BPEL_TAG);
            bpelElement.setText(this.gpelProcess.xmlStringPretty());
        }

        // Workflow WSDL
        if (this.workflowWSDL != null) {
            XmlElement workflowWSDLElement = workflowElement.addElement(NS_XWF, WORKFLOW_WSDL_TAG);
            workflowWSDLElement.setText(this.workflowWSDL.xmlStringPretty());
        }

        return workflowElement;
    }

    /**
     * Binds WSNodes to components
     *
     * @throws ComponentException
     * @throws GraphException
     */
    public void bindComponents() throws ComponentException, GraphException {
        // This map is to avoid creating multiple instances for the a component
        Map<WSComponentKey, WSComponent> components = new HashMap<WSComponentKey, WSComponent>();
        for (WSNode node : GraphUtil.getWSNodes(this.graph)) {
            String id = node.getWSDLID();
            logger.finest("id: " + id);
            WsdlDefinitions wsdl = this.wsdlMap.get(id);

            if (wsdl == null) {
                // XXX This happens while loading a workflow that is created by
                // the version 2.2.6_2 or below from GPEL.
                // Need to look for wsdl manually.
                // id should be look like
                // {http://www.extreme.indiana.edu/math/}Adder
                for (WsdlDefinitions w : this.wsdlMap.values()) {
                    QName name = WSDLUtil.getWSDLQName(w);
                    if (name.toString().equals(id)) {
                        wsdl = w;
                        break;
                    }
                }
            }
            if (wsdl == null) {
                continue;
            }
            try{
            QName portType = node.getPortTypeQName();
            if (portType == null) {
                // XXX This happens while parsing xwf created by the version
                // 2.2.6_1 or below.
                portType = WSDLUtil.getFirstPortTypeQName(wsdl);
            }
            String operation = node.getOperationName();
            if (operation == null) {
                // XXX This happens while parsing xwf created by the version
                // 2.2.6_1 or below.
                operation = WSDLUtil.getFirstOperationName(wsdl, portType);
            }
            WSComponentKey key = new WSComponentKey(id, portType, operation);

            WSComponent component;
            if (components.containsKey(key)) {
                component = components.get(key);
            } else {
                component = WSComponentFactory.createComponent(wsdl, portType, operation);
                components.put(key, component);
            }
            node.setComponent(component);
            }catch (UtilsException e){
                logger.throwing(e);
            }
        }

        this.graph.fixParameterNodes();
        GraphUtil.propagateTypes(this.graph);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Workflow clone() {
        XmlElement originalXML = toXML();
        try {
            XmlElement newXML = XMLUtil.deepClone(originalXML);
            Workflow newWorkflow = new Workflow(newXML);
            return newWorkflow;
        } catch (GraphException e) {
            // This should not happen.
            throw new XBayaRuntimeException(e);
        } catch (XBayaException e) {
            // This should not happen.
            throw new XBayaRuntimeException(e);
        } catch (UtilsException e) {
            // This should not happen.
            throw new XBayaRuntimeException(e);
        }
    }

    /**
     * @param graph
     * @return The workflow
     */
    public static Workflow graphToWorkflow(WSGraph graph) {
        Workflow workflow = new Workflow();
        workflow.setGraph(graph);

        workflow.setName(graph.getName());
        workflow.setDescription(graph.getDescription());
        return workflow;
    }

    /**
     * @param workflowElement
     * @throws GraphException
     * @throws ComponentException
     */
    private void parse(XmlElement workflowElement) throws GraphException, ComponentException {
        // Graph
        XmlElement graphElement = workflowElement.element(GraphSchema.GRAPH_TAG);
        this.graph = WSGraphFactory.createGraph(graphElement);
        WsdlDefinitions wsdl = null;
        XmlElement wsdlsElement = workflowElement.element(WSDLS_TAG);
        for (XmlElement wsdlElement : wsdlsElement.elements(null, WSDL_TAG)) {
            String wsdlText = wsdlElement.requiredText();
            try {
                wsdl = WSDLUtil.stringToWSDL(wsdlText);
            } catch (UtilsException e) {
                logger.throwing(e);
            }
            String id = wsdlElement.attributeValue(NS_XWF, ID_ATTRIBUTE);
            if (id == null || id.length() == 0) {
                // xwf up to 2.2.6_2 doesn't have ID.
                id = WSDLUtil.getWSDLQName(wsdl).toString();
            }
            addWSDL(id, wsdl);
        }

        bindComponents();

        // Image
        XmlElement imageElement = workflowElement.element(IMAGE_TAG);
        if (imageElement != null) {
            String base64 = imageElement.requiredText();
            byte[] bytes = Base64.decodeBase64(base64.getBytes());
            try {
                this.image = ImageIO.read(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                // This should not happen and it's OK that image is broken. We
                // can reproduce it anytime.
                logger.caught(e);
            }
        }

        XmlElement bpelElement = workflowElement.element(BPEL_TAG);
        if (bpelElement != null) {
            try {
                String bpelString = bpelElement.requiredText();
                XmlNamespace gpelNS = XmlInfosetBuilder.newInstance().newNamespace(BPELScript.GPEL, BPELScript.GPELNS);
                GpelConstants.GPEL_NS = gpelNS;
                this.gpelProcess = new GpelProcess(XMLUtil.stringToXmlElement(bpelString));
            } catch (RuntimeException e) {
                String error = "Failed to parse the BPEL document.";
                throw new GraphException(error, e);
            }
        }

        XmlElement workflowWSDLElement = workflowElement.element(WORKFLOW_WSDL_TAG);
        if (workflowWSDLElement != null) {
            try {
                String wsdlText = workflowWSDLElement.requiredText();
                this.workflowWSDL = new WsdlDefinitions(XMLUtil.stringToXmlElement(wsdlText));
            } catch (RuntimeException e) {
                String error = "Failed to parse the workflow WSDL.";
                throw new GraphException(error, e);
            }
        }
    }

    public XmlElement getODEDeploymentDescriptor(URI dscUrl, String odeEprEndingWithPort) throws GraphException,
            ComponentException {
        if (this.odeDeploymentDiscriptor == null) {
            this.odeDeploymentDiscriptor = new ODEDeploymentDescriptor().generate(this.getName(),
                    getOdeWorkflowWSDL(dscUrl, odeEprEndingWithPort), getOdeProcess(dscUrl, odeEprEndingWithPort),
                    getOdeServiceWSDLs(dscUrl, odeEprEndingWithPort));
        }
        return this.odeDeploymentDiscriptor;

    }

    /**
     * Returns the odeWorkflowWSDL.
     *
     * @return The odeWorkflowWSDL
     * @throws GraphException
     * @throws ComponentException
     */
    public WsdlDefinitions getOdeInvokableWSDL(URI dscUrl, String odeEprEndingWithPort) throws GraphException,
            ComponentException {
        if (this.odeInvokableWSDL == null) {
            generateODEScripts(dscUrl, odeEprEndingWithPort);
        }
        return this.odeInvokableWSDL;
    }

    /**
     * Returns the odeProcess.
     *
     * @return The odeProcess
     * @throws ComponentException
     * @throws GraphException
     */
    public GpelProcess getOdeProcess(URI dscUrl, String odeEprEndingWithPort) throws GraphException, ComponentException {
        if (this.odeProcess == null) {
            generateODEScripts(dscUrl, odeEprEndingWithPort);
        }
        return this.odeProcess;
    }

    /**
     * Returns the odeWsdlMap.
     *
     * @return The odeWsdlMap
     * @throws ComponentException
     * @throws GraphException
     */
    public Map<String, WsdlDefinitions> getOdeServiceWSDLs(URI dscUrl, String odeEprEndingWithPort)
            throws GraphException, ComponentException {
        if (this.odeWsdlMap == null) {
            generateODEScripts(dscUrl, odeEprEndingWithPort);
        }

        return this.odeWsdlMap;
    }

    /**
     * Returns the odeWorkflowWSDL.
     *
     * @return The odeWorkflowWSDL
     * @throws ComponentException
     * @throws GraphException
     */
    public WsdlDefinitions getOdeWorkflowWSDL(URI dscUrl, String odeEprEndingWithPort) throws GraphException,
            ComponentException {
        if (this.odeWorkflowWSDL == null) {
            generateODEScripts(dscUrl, odeEprEndingWithPort);
        }

        return this.odeWorkflowWSDL;
    }

    public WsdlDefinitions getTridentWorkflowWSDL(URI dscUrl, String odeEprEndingWithPort) throws GraphException,
            ComponentException {
        if (this.tridentWSDL == null) {
            generateODEScripts(dscUrl, odeEprEndingWithPort);
        }

        return this.tridentWSDL;
    }

    private void generateODEScripts(URI dscUrl, String odeEprEndingWithPort) throws GraphException, ComponentException {
        this.getGraph().setID(this.getName());

        // find whether its Streaming
        List<NodeImpl> nodes = this.graph.getNodes();
        Node activeNode = null;
        boolean streaming = false;
        String operationName = null;
        for (NodeImpl node : nodes) {
            if (node instanceof StreamReceiveNode) {
                streaming = true;
                activeNode = node;
                operationName = ((WSNode) activeNode).getOperationName();
                break;
            }
        }

        BPELScript script = null;

        // if this is streaming there is a possibility that the active node
        // has a wsdl with the operation name "Run", so add control at the end.
        if (streaming && operationName.startsWith("Run")) {
            script = new BPELScript(this, operationName + "_control");
        } else {
            script = new BPELScript(this);
        }
        ODEWSDLTransformer wsdlTransformer = new ODEWSDLTransformer();
        script.create(BPELScriptType.BPEL2);
        this.odeProcess = script.getGpelProcess();
        this.odeProcess.setTargetNamespace(XBayaConstants.LEAD_NS);

        WsdlDefinitions abstractWorkflowWsdl = script.getWorkflowWSDL().getWsdlDefinitions();
        this.odeWorkflowWSDL = abstractWorkflowWsdl;
        try {
            this.odeInvokableWSDL = WSDLUtil.stringToWSDL(abstractWorkflowWsdl.xmlString());
            wsdlTransformer.makeWorkflowWSDLConcrete(this.odeInvokableWSDL, this.getName(), dscUrl);
            wsdlTransformer.setOdeLocation(odeEprEndingWithPort, this.getName(), this.odeInvokableWSDL);

            this.odeWsdlMap = new HashMap<String, WsdlDefinitions>();
            Collection<XmlElement> itr = script.getWSDLs();
            for (XmlElement xmlElement : itr) {
                WsdlDefinitions wsdl = WSDLUtil.stringToWSDL(XmlConstants.BUILDER.serializeToString(xmlElement));
                String id = xmlElement.attributeValue(NS_XWF, ID_ATTRIBUTE);
                if (id == null || id.length() == 0) {
                    // xwf up to 2.2.6_2 doesn't have ID.
                    id = WSDLUtil.getWSDLQName(wsdl).toString();
                    if (null == id || "".equals(id) || (id.startsWith("{") && id.endsWith("}"))) {
                        QName wsdlQname = new QName(NS_XWF.getName(), WSDLUtil.getFirstOperationName(wsdl,
                                WSDLUtil.getFirstPortTypeQName(wsdl)));
                        id = wsdlQname.toString();
                        wsdl.xml().setAttributeValue("name", wsdlQname.getLocalPart());
                    }
                }
                WSDLCleaner.cleanWSDL(wsdl);
                this.odeWsdlMap.put(id, wsdl);
            }
        } catch (Exception e) {
            logger.throwing(e);
        }
        new ODEBPELTransformer()
                .generateODEBPEL(this.odeProcess, this.getName(), this.odeWorkflowWSDL, this.odeWsdlMap);

        wsdlTransformer.trasnformToODEWsdls(this.getName(), dscUrl, this.odeWorkflowWSDL, this.odeWsdlMap);

        String wsdlString = XMLUtil.xmlElementToString(this.odeWorkflowWSDL.xml());
        this.tridentWSDL = new WsdlDefinitions(XMLUtil.stringToXmlElement(wsdlString));
        new TridentTransformer().process(this.tridentWSDL);

        if (streaming) {
            StreamTransformer streamTransformer = new StreamTransformer();

            Set<String> keySet = odeWsdlMap.keySet();
            String oldWorkflowName = this.getName().substring("Control_".length());

            streamTransformer.removeReply(odeProcess);
            for (String key : keySet) {
                if (key.equals(oldWorkflowName)) {
                    streamTransformer.removeOutputMessageReferences(this.odeWsdlMap.get(key));
                }
            }
            try {
                streamTransformer.addReceive(this.odeWorkflowWSDL, activeNode, this);
                streamTransformer.removeOutputMessageReferences(this.odeWorkflowWSDL);
            } catch (CloneNotSupportedException e) {
                throw new XBayaRuntimeException(e);
            }

        } else {
            // StreamTransformer streamTransformer = new StreamTransformer();
            // streamTransformer.removeReply(odeProcess);
            // streamTransformer.removeOutputMessageReferences(this.odeWorkflowWSDL);
        }

    }

    /**
     * @return
     */
    public QName getQname() {

        return this.qname = new QName(XBayaConstants.LEAD_NS, this.getName());

    }

    /**
     * @param templateID
     */
    public void setGPELTemplateID(URI templateID) {
        this.gpelTemplateID = templateID;

    }

    /**
     * @return
     */
    public URI getGPELTemplateID() {
        return this.gpelTemplateID;
    }

    public boolean equals(Workflow workflow) {
        return this.graph.equals(workflow.getGraph());
    }

    /**
     * @return
     */
    public synchronized XBayaExecutionState getExecutionState() {
        return this.executionState;
    }

    /**
     * @param state
     */
    public synchronized void setExecutionState(XBayaExecutionState state) {
        this.executionState = state;
    }

}