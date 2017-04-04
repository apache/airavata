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
package org.apache.airavata.workflow.model.graph.impl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphFactory;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.workflow.model.graph.system.StreamSourceNode;
import org.apache.airavata.workflow.model.graph.system.SystemDataPort;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.apache.airavata.workflow.model.utils.ApplicationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public abstract class GraphImpl implements Graph {

    private static final Logger logger = LoggerFactory.getLogger(GraphImpl.class);

    /**
     * Unique ID of this workflow
     */
    private String id;

    /**
     * Name of the workflow. It has to have one.
     */
    private String name = "Workflow";

    /**
     * Default to empty string to avoid null check.
     */
    private String description = "";

    private List<NodeImpl> nodes = new LinkedList<NodeImpl>();

    private List<PortImpl> ports = new LinkedList<PortImpl>();

    private List<EdgeImpl> edges = new LinkedList<EdgeImpl>();

    private GraphFactory factory;

    /**
     * @param factory
     */
    public GraphImpl(GraphFactory factory) {
        this.factory = factory;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getID()
    */
        public String getID() {
             if (this.id == null) {
                 this.id = this.name;
                 // If its still null
                 if (null == this.id) {
                    throw new WorkflowRuntimeException("The workflow ID is null");
                 }
             }
            return this.id;
        }


    /**
     * This will only be done for the ODE
     * 
     * @param id
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getDescription()
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getNodes()
     */
    public List<NodeImpl> getNodes() {
        return this.nodes;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getPorts()
     */
    public List<PortImpl> getPorts() {
        return this.ports;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getEdges()
     */
    public List<EdgeImpl> getEdges() {
        return this.edges;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#removeNode(org.apache.airavata.workflow.model.graph.Node)
     */
    public void removeNode(Node node) throws GraphException {
        if (node == null) {
            throw new IllegalArgumentException("null");
        }
        if (!this.nodes.contains(node)) {
            throw new GraphException("The graph doesn't contain the node that is being removed.");
        }

        NodeImpl nodeImpl = (NodeImpl) node;

        // Have to be very careful to remove the node.
        // The extended for loop cannot be used to remove the elements.

        // Remove edges connected to input ports.
        for (Iterator<DataPort> portItr = nodeImpl.getInputPorts().iterator(); portItr.hasNext();) {
            DataPort port = portItr.next();
            for (Iterator<DataEdge> edgeItr = port.getEdges().iterator(); edgeItr.hasNext();) {
                DataEdge edge = edgeItr.next();
                // Remove the edge from from-port.
                DataPort fromPort = edge.getFromPort();
                fromPort.removeEdge(edge);
                // remove the edge from this port. This is necessary so that
                // type update works properly.
                edgeItr.remove();

                // Remove the edge from the graph.
                this.edges.remove(edge);
                fromPort.getNode().edgeWasRemoved(edge);
            }
            // Remove the port from the node.
            portItr.remove();
            // Remove the port from the graph.
            this.ports.remove(port);
        }

        // Remove edges connected to output ports.
        for (Iterator<DataPort> portItr = nodeImpl.getOutputPorts().iterator(); portItr.hasNext();) {
            DataPort port = portItr.next();
            for (Iterator<DataEdge> edgeItr = port.getEdges().iterator(); edgeItr.hasNext();) {
                DataEdge edge = edgeItr.next();
                DataPort toPort = edge.getToPort();
                toPort.removeEdge(edge);
                edgeItr.remove();
                this.edges.remove(edge);
                toPort.getNode().edgeWasRemoved(edge);
            }
            portItr.remove();
            this.ports.remove(port);
        }

        for (Iterator<ControlPort> portItr = nodeImpl.getControlOutPorts().iterator(); portItr.hasNext();) {
            PortImpl port = portItr.next();
            for (Iterator<? extends EdgeImpl> edgeItr = port.getEdges().iterator(); edgeItr.hasNext();) {
                EdgeImpl edge = edgeItr.next();
                PortImpl toPort = edge.getToPort();
                toPort.removeEdge(edge);
                edgeItr.remove();
                this.edges.remove(edge);
                toPort.getNode().edgeWasRemoved(edge);
            }
            portItr.remove();
            this.ports.remove(port);
        }

        PortImpl controlInPort = nodeImpl.getControlInPort();
        if (controlInPort != null) {
            for (Iterator<? extends EdgeImpl> edgeItr = controlInPort.getEdges().iterator(); edgeItr.hasNext();) {
                EdgeImpl edge = edgeItr.next();
                PortImpl fromPort = edge.getFromPort();
                fromPort.removeEdge(edge);
                edgeItr.remove();
                this.edges.remove(edge);
                fromPort.getNode().edgeWasRemoved(edge);
            }
            this.ports.remove(controlInPort);
        }

        PortImpl eprPort = nodeImpl.getEPRPort();
        if (eprPort != null) {
            for (Iterator<? extends EdgeImpl> edgeItr = eprPort.getEdges().iterator(); edgeItr.hasNext();) {
                EdgeImpl edge = edgeItr.next();
                PortImpl toPort = edge.getToPort();
                toPort.removeEdge(edge);
                edgeItr.remove();
                this.edges.remove(edge);
                toPort.getNode().edgeWasRemoved(edge);
            }
            this.ports.remove(eprPort);
        }

        this.nodes.remove(node);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getNode(java.lang.String)
     */
    public NodeImpl getNode(String nodeID) {
        for (NodeImpl node : this.nodes) {
            if (nodeID.equals(node.getID())) {
                return node;
            }
        }
        return null;
    }

    /**
     * @param port
     * @throws GraphException
     */
    public void removePort(Port port) throws GraphException {
        if (port == null) {
            throw new IllegalArgumentException("null");
        }
        if (!this.ports.contains(port)) {
            throw new GraphException("The graph doesn't contain the port that is being removed.");
        }

        // copy it so that we can remove edge without worrying about the
        // iteration issue.
        ArrayList<Edge> edgesToBeRemoved = new ArrayList<Edge>(port.getEdges());
        for (Edge edge : edgesToBeRemoved) {
            removeEdge(edge);
        }

        this.ports.remove(port);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#getPort(java.lang.String)
     */
    public PortImpl getPort(String portID) {
        for (PortImpl port : this.ports) {
            if (portID.equals(port.getID())) {
                return port;
            }
        }
        return null;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#addEdge(org.apache.airavata.workflow.model.graph.Port,
     *      org.apache.airavata.workflow.model.graph.Port)
     */
    public Edge addEdge(Port fromPort, Port toPort) throws GraphException {
        if (containsEdge(fromPort, toPort)) {
            // The edge already exists. Doesn't create a new one.
            return null;
        }

        if (!this.ports.contains(fromPort) || !this.ports.contains(toPort)) {
            // The specified port doesn't belong to the graph.
            throw new GraphException("The graph doesn't contain the specified port.");
        }

        PortImpl fromPortImpl = (PortImpl) fromPort;
        PortImpl toPortImpl = (PortImpl) toPort;
        NodeImpl fromNode = fromPortImpl.getNode();
        NodeImpl toNode = toPortImpl.getNode();

        EdgeImpl edge = this.factory.createEdge(fromPort, toPort);

        edge.setFromPort(fromPortImpl);
        edge.setToPort(toPortImpl);
        fromPortImpl.addEdge(edge);
        toPortImpl.addEdge(edge);
        addEdge(edge);

        try {
            fromNode.edgeWasAdded(edge);
            toNode.edgeWasAdded(edge);
            return edge;
        } catch (GraphException e) {
            removeEdge(edge);
            throw e;
        }

    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.Graph#removeEdge(org.apache.airavata.workflow.model.graph.Edge)
     */
    public void removeEdge(Edge edge) throws GraphException {
        if (!this.edges.contains(edge)) {
            throw new GraphException("The graph doesn't contain the specified edge.");
        }
        EdgeImpl edgeImpl = (EdgeImpl) edge;

        PortImpl fromPort = edgeImpl.getFromPort();
        PortImpl toPort = edgeImpl.getToPort();

        NodeImpl fromNode = fromPort.getNode();
        NodeImpl toNode = toPort.getNode();

        fromPort.removeEdge(edgeImpl);
        toPort.removeEdge(edgeImpl);

        this.edges.remove(edgeImpl);

        // This has to be after removing edges.
        fromNode.edgeWasRemoved(edge);
        toNode.edgeWasRemoved(edge);
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.Graph#removeEdge(org.apache.airavata.workflow.model.graph.Port,
     *      org.apache.airavata.workflow.model.graph.Port)
     */
    public void removeEdge(Port fromPort, Port toPort) throws GraphException {
        Collection<? extends Edge> fromEdges = fromPort.getEdges();
        for (Edge fromEdge : fromEdges) {
            if (fromEdge.getToPort() == toPort) {
                // It's OK to remove this way because it will exit the loop
                // right away.
                removeEdge(fromEdge);
                return;
            }
        }
        throw new WorkflowRuntimeException("No edge exist between two ports.");
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#containsEdge(org.apache.airavata.workflow.model.graph.Port,
     *      org.apache.airavata.workflow.model.graph.Port)
     */
    public boolean containsEdge(Port fromPort, Port toPort) {
        for (Edge fromEdge : fromPort.getEdges()) {
            Collection<? extends Edge> toEdges = toPort.getEdges();
            if (toEdges.contains(fromEdge)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.Graph#importGraph(org.apache.airavata.workflow.model.graph.Graph)
     */
    public void importGraph(Graph graph) throws GraphException {

        // Does not support other implementations.
        if (!(graph instanceof GraphImpl)) {
            throw new GraphException("Cannot import this graph implementation");
        }

        GraphImpl graphImpl = (GraphImpl) graph;

        for (NodeImpl node : graphImpl.getNodes()) {
            addNode(node);
            // Recreates the ID so that it doesn't conflict with the existing
            // one.
            node.createID();
            // Changes the position.
            Point position = node.getPosition();
            node.setPosition(new Point(position.x + 5, position.y + 5));
        }

        for (PortImpl port : graphImpl.getPorts()) {
            addPort(port);
        }

        for (EdgeImpl edge : graphImpl.getEdges()) {
            addEdge(edge);
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Graph#toXML()
     */
    public XmlElement toXML() {

        XmlElement graphElement = XMLUtil.BUILDER.newFragment(GraphSchema.NS, GraphSchema.GRAPH_TAG);

        graphElement.setAttributeValue(GraphSchema.NS, GraphSchema.XBAYA_VERSION_ATTRIBUTE, ApplicationVersion.VERSION.getVersion());

        XmlElement idElement = graphElement.addElement(GraphSchema.NS, GraphSchema.GRAPH_ID_TAG);
        idElement.addChild(getID());

        if (this.name != null) {
            XmlElement nameElement = graphElement.addElement(GraphSchema.NS, GraphSchema.GRAPH_NAME_TAG);
            nameElement.addChild(getName());
        }

        if (this.description != null) {
            XmlElement descriptionElement = graphElement.addElement(GraphSchema.NS, GraphSchema.GRAPH_DESCRIPTION_TAG);
            descriptionElement.addChild(getDescription());
        }

        toXML(graphElement);

        for (NodeImpl node : this.nodes) {
            XmlElement nodeElement = node.toXML();
            graphElement.addChild(nodeElement);
        }

        for (PortImpl port : this.ports) {
            XmlElement portElement = port.toXML();
            graphElement.addChild(portElement);
        }

        for (EdgeImpl edge : this.edges) {
            XmlElement edgeElement = edge.toXML();
            graphElement.addChild(edgeElement);
        }

        return graphElement;
    }

    @Override
    public JsonObject toJSON() {
        JsonObject graphObject = new JsonObject();

        graphObject.addProperty(GraphSchema.XBAYA_VERSION_ATTRIBUTE, ApplicationVersion.VERSION.getVersion());
        graphObject.addProperty(GraphSchema.GRAPH_ID_TAG, getID());
        graphObject.addProperty(GraphSchema.GRAPH_NAME_TAG, getName());
        graphObject.addProperty(GraphSchema.GRAPH_DESCRIPTION_TAG, getDescription());

        JsonArray nodeArray = new JsonArray();
        for (NodeImpl node : this.nodes) {
            nodeArray.add(node.toJSON());
        }
        graphObject.add(GraphSchema.NODE_TAG, nodeArray);

        JsonArray portArray = new JsonArray();
        for (PortImpl port : this.ports) {
            portArray.add(port.toJSON());
        }
        graphObject.add(GraphSchema.PORT_TAG, portArray);

        JsonArray edgeArray = new JsonArray();
        for (EdgeImpl edge : this.edges) {
            edgeArray.add(edge.toJSON());
        }
        graphObject.add(GraphSchema.EDGE_TAG, edgeArray);

        return graphObject;
    }

    /**
     * @param graphElement
     */
    protected void toXML(@SuppressWarnings("unused") XmlElement graphElement) {
        // For subclass to overwrite.
    }

    /**
     * @param graphElement
     * @throws GraphException
     */
    protected void parse(XmlElement graphElement) throws GraphException {
        String version = graphElement.attributeValue(GraphSchema.NS, GraphSchema.XBAYA_VERSION_ATTRIBUTE);
        logger.debug("parsing a workflow created by version " + version);

        XmlElement idElement = graphElement.element(GraphSchema.GRAPH_ID_TAG);
        if (idElement != null) {
            this.id = idElement.requiredText();
        }

        XmlElement nameElement = graphElement.element(GraphSchema.GRAPH_NAME_TAG);
        if (nameElement != null) {
            this.name = nameElement.requiredText();
        }

        XmlElement descriptionElement = graphElement.element(GraphSchema.GRAPH_DESCRIPTION_TAG);
        if (descriptionElement != null) {
            this.description = descriptionElement.requiredText();
        }

        for (XmlElement nodeElement : graphElement.elements(null, GraphSchema.NODE_TAG)) {
            NodeImpl nodeImpl = this.factory.createNode(nodeElement);
            // need to call this to set this graph to the node.
            addNode(nodeImpl);
        }

        for (XmlElement portElement : graphElement.elements(null, GraphSchema.PORT_TAG)) {
            PortImpl port = this.factory.createPort(portElement);
            // need to call this to set this graph to the port.
            this.addPort(port);
        }

        for (XmlElement edgeElement : graphElement.elements(null, GraphSchema.EDGE_TAG)) {
            EdgeImpl edge = this.factory.createEdge(edgeElement);
            // need to call this to set this graph to the edge.
            this.addEdge(edge);
        }

        indexToPointer();
    }

    protected void parse(JsonObject graphObject) throws GraphException{
        JsonPrimitive  jsonPrimitive = graphObject.getAsJsonPrimitive(GraphSchema.GRAPH_ID_TAG);
        if (jsonPrimitive != null) {
            this.id = jsonPrimitive.getAsString();
        }
        jsonPrimitive = graphObject.getAsJsonPrimitive(GraphSchema.GRAPH_NAME_TAG);
        if (jsonPrimitive != null) {
            this.name = jsonPrimitive.getAsString();
        }
        jsonPrimitive = graphObject.getAsJsonPrimitive(GraphSchema.GRAPH_DESCRIPTION_TAG);
        if (jsonPrimitive != null) {
           this.description = jsonPrimitive.getAsString();
        }

        JsonArray jArray = graphObject.getAsJsonArray(GraphSchema.NODE_TAG);
        for (JsonElement jsonElement : jArray) {
            addNode(this.factory.createNode((JsonObject) jsonElement));
        }

        jArray = graphObject.getAsJsonArray(GraphSchema.PORT_TAG);
        for (JsonElement jsonElement : jArray) {
            addPort(this.factory.createPort((JsonObject) jsonElement));
        }

        jArray = graphObject.getAsJsonArray(GraphSchema.EDGE_TAG);
        for (JsonElement jsonElement : jArray) {
            addEdge(this.factory.createEdge((JsonObject)jsonElement));
        }

        indexToPointer();
    }

    /**
     * Adds a node.
     * 
     * @param node
     *            the node to add
     */
    protected void addNode(NodeImpl node) {
        node.setGraph(this);
        // if this is a Stream now put it at the begining
        if (node instanceof StreamSourceNode) {
            this.nodes.add(0, node);
        } else {
            this.nodes.add(node);
        }
    }

    /**
     * @param port
     */
    protected void addPort(PortImpl port) {
        port.setGraph(this);
        this.ports.add(port);
    }

    /**
     * Converts indexes to references. This method is called after reading the graph from an XML file.
     * 
     * @throws GraphException
     */
    protected void indexToPointer() throws GraphException {
        for (NodeImpl node : this.nodes) {
            node.indexToPointer();
        }
        for (PortImpl port : this.ports) {
            port.indexToPointer();
        }
        for (EdgeImpl edge : this.edges) {
            edge.indexToPointer();
        }
    }

    /**
     * @param edge
     */
    private void addEdge(EdgeImpl edge) {
        edge.setGraph(this);
        this.edges.add(edge);
    }

    // private void createID() {
    // Date date = new Date();
    // SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss_S");
    // String time = format.format(date);
    //
    // this.id = StringUtil.convertToJavaIdentifier(this.name) + "_" + time;
    // }

    /**
     * @throws GraphException
     */
    public void fixParameterNodes() {
        // XXX fix the ports of parameter nodes for 2.6.3 or before.
        for (InputNode node : GraphUtil.getNodes(this, InputNode.class)) {
            DataPort oldPort = node.getOutputPort(0);
            if (oldPort instanceof WSPort) {
                node.getOutputPorts().remove(oldPort);
                this.ports.remove(oldPort);
                SystemDataPort newPort = new SystemDataPort();
                this.ports.add(newPort);
                newPort.setKind(Kind.DATA_OUT);
                newPort.setName(oldPort.getName());
                newPort.setGraph(this);
                newPort.setNode(node);
                newPort.createID();
                node.getOutputPorts().add(newPort);
                for (DataEdge edge : oldPort.getEdges()) {
                    edge.setFromPort(newPort);
                    newPort.getEdges().add(edge);
                }
            }
        }
        for (OutputNode node : GraphUtil.getNodes(this, OutputNode.class)) {
            DataPort oldPort = node.getInputPort(0);
            if (oldPort instanceof WSPort) {
                node.getInputPorts().remove(oldPort);
                this.ports.remove(oldPort);
                SystemDataPort newPort = new SystemDataPort();
                this.ports.add(newPort);
                newPort.setKind(Kind.DATA_IN);
                newPort.setName(oldPort.getName());
                newPort.setGraph(this);
                newPort.setNode(node);
                newPort.createID();
                node.getInputPorts().add(newPort);
                for (DataEdge edge : oldPort.getEdges()) {
                    edge.setToPort(newPort);
                    newPort.getEdges().add(edge);
                }
            }
        }
    }

    /**
     * This returns the number of input Nodes, this will be useful when adding unique Id for nodes
     * @return
     */
    public int getCurrentInputNodeCount(){
        int index=0;
        for(Node node:nodes){
            if(node instanceof InputNode){
                index++;
            }
        }
        return index;
    }
    /**
    * This returns the number of input Nodes, this will be useful when adding unique Id for nodes
      * @return
      */
     public int getCurrentOutputNodeCount(){
         int index=0;
         for(Node node:nodes){
             if(node instanceof OutputNode){
                 index++;
             }
         }
         return index;
     }


}