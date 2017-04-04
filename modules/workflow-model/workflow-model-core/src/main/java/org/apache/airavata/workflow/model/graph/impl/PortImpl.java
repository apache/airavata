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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.ComponentPort;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.xmlpull.infoset.XmlElement;

/**
 * The Port class represents a port
 * 
 */
public abstract class PortImpl implements Port {

    /**
     * The graph this edge belogs to
     */
    private GraphImpl graph;

    protected String id;

    /**
     * The name of this port
     */
    private String name;

    /**
     * The kind of this port, either input port or output port
     */
    private Kind kind;

    /**
     * The node that this port belongs to
     */
    private NodeImpl node;

    /**
     * The Edges that this port is connected to
     */
    private List<EdgeImpl> edges;

    private ComponentPort componentPort;

    /**
     * The ID of the node that this port belongs to. This is used only during parsing the XML.
     */
    private String nodeID;

    /**
     * Creates a Port.
     */
    protected PortImpl() {
        this.edges = new LinkedList<EdgeImpl>();
    }

    /**
     * Constructs a PortImpl.
     * 
     * @param portElement
     */
    public PortImpl(XmlElement portElement) {
        this();
        parse(portElement);
    }

    public PortImpl(JsonObject portObject) {
        this();
        parse(portObject);
    }


    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getID()
     */
    public String getID() {
        if (this.id == null) {
            // old format
            this.id = createID();
        }
        return this.id;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getNode()
     */
    public NodeImpl getNode() {
        return this.node;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getEdges()
     */
    public List<? extends EdgeImpl> getEdges() {
        return this.edges;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getEdge(int)
     */
    public Edge getEdge(int index) {
        return this.edges.get(index);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getKind()
     */
    public Kind getKind() {
        return this.kind;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getFromPorts()
     */
    public List<Port> getFromPorts() {
        List<Port> fromPorts = new ArrayList<Port>();
        for (EdgeImpl edge : this.edges) {
            fromPorts.add(edge.getFromPort());
        }
        return fromPorts;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getFromPort()
     */
    public Port getFromPort() {
        if (this.edges.size() > 0) {
            Edge edge = this.edges.get(0);
            return edge.getFromPort();
        } else {
            return null;
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getFromNodes()
     */
    public List<Node> getFromNodes() {
        List<Node> fromNodes = new ArrayList<Node>();
        for (Port port : getFromPorts()) {
            fromNodes.add(port.getNode());
        }
        return fromNodes;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getFromNode()
     */
    public Node getFromNode() {
        Port fromPort = getFromPort();
        if (fromPort == null) {
            return null;
        } else {
            return fromPort.getNode();
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getToPorts()
     */
    public List<Port> getToPorts() {
        List<Port> toPorts = new ArrayList<Port>();
        for (Edge edge : this.edges) {
            toPorts.add(edge.getToPort());
        }
        return toPorts;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getToNodes()
     */
    public List<Node> getToNodes() {
        List<Node> toNodes = new ArrayList<Node>();
        for (Port port : getToPorts()) {
            toNodes.add(port.getNode());
        }
        return toNodes;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#setComponentPort(org.apache.airavata.workflow.model.component.ComponentPort)
     */
    public void setComponentPort(ComponentPort componentPort) {
        this.componentPort = componentPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Port#getComponentPort()
     */
    public ComponentPort getComponentPort() {
        if (this.componentPort == null) {
            int index;
            switch (this.kind) {
            case DATA_IN:
                index = this.node.getInputPorts().indexOf(this);
                this.componentPort = this.node.getComponent().getInputPort(index);
                break;
            case DATA_OUT:
                index = this.node.getOutputPorts().indexOf(this);
                this.componentPort = this.node.getComponent().getOutputPort(index);
                break;
            case CONTROL_IN:
                this.componentPort = this.node.getComponent().getControlInPort();
                break;
            case CONTROL_OUT:
                index = this.node.getControlOutPorts().indexOf(this);
                this.componentPort = this.node.getComponent().getControlOutPort(index);
                break;
            case EPR:
                this.componentPort = this.node.getComponent().getEPRPort();
                break;
            }
        }
        return this.componentPort;
    }

    /**
     * Sets the kind of this port.
     * 
     * @param kind
     *            The kind, either INPUT_PORT or OUTPUT_PORT
     */
    protected void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * Sets a graph this port belogs to.
     *
     * @param graph
     *            The graph
     */
    protected void setGraph(GraphImpl graph) {
        this.graph = graph;
    }

    public GraphImpl getGraph() {
        return graph;
    }

    /**
     * @param node
     */
    protected void setNode(NodeImpl node) {
        this.node = node;
    }

    /**
     * Adds an Edge.
     * 
     * @param edge
     *            The edge to add
     */
    protected void addEdge(EdgeImpl edge) {
        if (this.edges.contains(edge)) {
            throw new WorkflowRuntimeException("The edge is already addes");
        } else {
            this.edges.add(edge);
        }
    }

    /**
     * Removes an Edge.
     * 
     * @param edge
     *            The edge to remove.
     */
    protected void removeEdge(Edge edge) {
        if (this.edges.contains(edge)) {
            this.edges.remove(edge);
            // this.node.edgeWasRemoved(edge);
        } else {
            throw new WorkflowRuntimeException("The edge doesn't exist.");
        }
    }

    /**
     * @return The ID of this port
     */
    protected String createID() {
        String nid = getNode().getID();
        String portType;
        switch (this.kind) {
        case DATA_IN:
            portType = "in";
            break;
        case DATA_OUT:
            portType = "out";
            break;
        case CONTROL_IN:
            portType = "ctrl_in";
            break;
        case CONTROL_OUT:
            portType = "ctrl_out";
            break;
        case EPR:
            portType = "epr";
            break;
        default:
            // Should not happen.
            throw new WorkflowRuntimeException("Wrong type of the port: " + this.kind);
        }
        int index = getIndex();
        return nid + "_" + portType + "_" + index;
    }

    /**
     * Converts references to indexes.
     * 
     * @throws GraphException
     */
    protected void indexToPointer() throws GraphException {
        this.node = this.graph.getNode(this.nodeID);
        if (this.node == null) {
            throw new GraphException("Cannot find a node with the ID, " + this.nodeID + ".");
        }
    }

    /**
     * Parses XML
     * 
     * @param portElement
     */
    protected void parse(XmlElement portElement) {
        XmlElement idElement = portElement.element(GraphSchema.PORT_ID_TAG);
        this.id = idElement.requiredText();

        XmlElement nameElement = portElement.element(GraphSchema.PORT_NAME_TAG);
        if (nameElement != null) {
            // TODO control ports might have name?
            this.name = nameElement.requiredText();
        }

        XmlElement nodeElement = portElement.element(GraphSchema.PORT_NODE_TAG);
        this.nodeID = nodeElement.requiredText();
    }

    protected void parse(JsonObject portObject) {
        this.id = portObject.getAsJsonPrimitive(GraphSchema.PORT_ID_TAG).getAsString();

        JsonPrimitive jPrimitive = portObject.getAsJsonPrimitive(GraphSchema.PORT_NAME_TAG);
        if (jPrimitive != null) {
            this.name = jPrimitive.getAsString();
        }

        this.nodeID = portObject.getAsJsonPrimitive(GraphSchema.PORT_NODE_TAG).getAsString();
    }

    /**
     * @return the XML representation of this Port
     */
    protected XmlElement toXML() {
        XmlElement portElement = XMLUtil.BUILDER.newFragment(GraphSchema.NS, GraphSchema.PORT_TAG);

        XmlElement idElement = portElement.addElement(GraphSchema.NS, GraphSchema.PORT_ID_TAG);
        idElement.addChild(getID());

        if (this.name != null) {
            // TODO control ports might have name?
            XmlElement nameElement = portElement.addElement(GraphSchema.NS, GraphSchema.PORT_NAME_TAG);
            nameElement.addChild(this.name);
        }

        XmlElement nodeElement = portElement.addElement(GraphSchema.NS, GraphSchema.PORT_NODE_TAG);
        nodeElement.addChild(this.node.getID());

        return portElement;
    }

    protected JsonObject toJSON(){
        JsonObject portElement = new JsonObject();
        portElement.addProperty(GraphSchema.PORT_ID_TAG, getID());
        portElement.addProperty(GraphSchema.PORT_NAME_TAG, getName());
        portElement.addProperty(GraphSchema.PORT_NODE_TAG, this.node.getID());

        return portElement;
    }

    /**
     * Returns the port index within the node that this port belongs to.
     * 
     * @return the port index within the node that this port belongs to
     */
    public int getIndex() {
        int index;
        switch (this.kind) {
        case DATA_IN:
            index = getNode().getInputPorts().indexOf(this);
            break;
        case DATA_OUT:
            index = getNode().getOutputPorts().indexOf(this);
            break;
        case CONTROL_IN:
            index = 0; // Has only one.
            break;
        case CONTROL_OUT:
            index = getNode().getControlOutPorts().indexOf(this);
            break;
        case EPR:
            index = 0; // Has only one.
            break;
        default:
            // Shoud not happen.
            throw new RuntimeException("Wrong type of the port: " + this.kind);
        }
        return index;
    }

    public void setID(String id) {
        this.id = id;
    }
}