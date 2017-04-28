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
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

/**
 * The abstract implementation of the Node interface. This class should be hidden from the outsize of the package.
 * 
 */
public abstract class NodeImpl implements Node {

    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);

    protected String id;

    /**
     * A name of the node.
     */
    private String name = "";

    private Component component;

    private List<DataPort> outputPorts;

    private List<DataPort> inputPorts;

    private ControlPort controlInPort;

    private List<ControlPort> controlOutPorts;

    private PortImpl eprPort;

    private GraphImpl graph;

    private Point position;

    // The followings are used only during parsing the XML.

    private List<String> inputPortIDs;

    private List<String> outputPortIDs;

    private String controlInPortID;

    private List<String> controlOutPortIDs;

    private String eprPortID;

    private boolean breakOnExecution = false;

    protected String label;

    protected transient boolean requireJoin = false;
    
    private NodeExecutionState state = NodeExecutionState.WAITING;
    
    private List<NodeObserver> observers;

    /**
     * Creates a Node.
     */
    protected NodeImpl() {

        // Iinitialized to the empty string to avoid NullPointerException.
        this.name = "";

        this.position = new Point();
        this.inputPorts = new ArrayList<DataPort>();
        this.outputPorts = new ArrayList<DataPort>();
        this.controlOutPorts = new ArrayList<ControlPort>();

        this.inputPortIDs = new ArrayList<String>();
        this.outputPortIDs = new ArrayList<String>();
        this.controlOutPortIDs = new ArrayList<String>();
        
        observers=new ArrayList<Node.NodeObserver>();
    }

    protected NodeImpl(Graph graph) {
        this();
        this.graph = (GraphImpl) graph;
        this.graph.addNode(this);
    }

    /**
     * Constructs a NodeImpl.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public NodeImpl(XmlElement nodeElement) throws GraphException {
        this();
        parse(nodeElement);
    }

    public NodeImpl(JsonObject nodeObject) throws GraphException{
        this();
        parse(nodeObject);
    }

    /**
     * @return the ID of the node
     */
    public String getID() {
        return this.id;
    }

    /**
     * Creates unique node ID in the graph that this node belongs to.
     */
    public void createID() {
        String candidateID = StringUtil.convertToJavaIdentifier(this.name);
        Node node = this.graph.getNode(candidateID);
        while (node != null && node != this) {
            candidateID = StringUtil.incrementName(candidateID);
            node = this.graph.getNode(candidateID);
        }
        this.id = candidateID;

        for (PortImpl port : getAllPorts()) {
            port.createID();
        }
    }

    /**
     * Returns the name.
     * 
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the component.
     * 
     * @return The component
     */
    public Component getComponent() {
        return this.component;
    }

    /**
     * Sets the component.
     * 
     * @param component
     *            The component to set.
     */
    public void setComponent(Component component) {
        this.component = component;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Node#getGraph()
     */
    public Graph getGraph() {
        return this.graph;
    }

    /**
     * @param port
     */
    public void addOutputPort(DataPort port) {
        port.setKind(PortImpl.Kind.DATA_OUT);
        this.outputPorts.add(port);
        addPort(port);
    }

    /**
     * @param port
     * @throws GraphException
     */
    public void removeOutputPort(PortImpl port) throws GraphException {
        this.graph.removePort(port);
        this.outputPorts.remove(port);
    }

    /**
     * @param port
     */
    public void addInputPort(DataPort port) {
        port.setKind(PortImpl.Kind.DATA_IN);
        this.inputPorts.add(port);
        addPort(port);
    }

    /**
     * @param port
     * @throws GraphException
     */
    public void removeInputPort(PortImpl port) throws GraphException {
        this.graph.removePort(port);
        this.inputPorts.remove(port);
    }

    /**
     * Sets the location of the node.
     * 
     * @param point
     *            The location
     */
    public void setPosition(Point point) {
        this.position.x = point.x;
        this.position.y = point.y;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Node#getPosition()
     */
    public Point getPosition() {
        return this.position;
    }

    /**
     * Returns the List of output ports.
     * 
     * @return the List of output ports
     */
    public List<DataPort> getOutputPorts() {
        return this.outputPorts;
    }

    /**
     * Returns the List of input ports.
     * 
     * @return the List of input ports
     */
    public List<DataPort> getInputPorts() {
        return this.inputPorts;
    }

    /**
     * Returns the output port of the specified index.
     * 
     * @param index
     *            The specified index
     * @return the uses port of the specified index
     */
    public DataPort getOutputPort(int index) {
        if (index < 0 || index >= this.outputPorts.size()) {
            String message = "index has to be possitive and less than " + this.outputPorts.size();
            throw new IllegalArgumentException(message);
        }
        return this.outputPorts.get(index);
    }

    /**
     * Returns the input port of the specified index.
     * 
     * @param index
     *            The specified index
     * @return the input port of the specified index
     */
    public DataPort getInputPort(int index) {
        if (index < 0 || index >= this.inputPorts.size()) {
            throw new IllegalArgumentException();
        }
        return this.inputPorts.get(index);
    }

    /**
     * @return The controlInPort.
     */
    public ControlPort getControlInPort() {
        return this.controlInPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Node#getControlOutPorts()
     */
    public List<ControlPort> getControlOutPorts() {
        return this.controlOutPorts;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Node#getEPRPort()
     */
    public PortImpl getEPRPort() {
        return this.eprPort;
    }

    /**
     * Returns all ports that belong to this node.
     * 
     * @return All ports that belong to this node.
     */
    public Collection<PortImpl> getAllPorts() {
        ArrayList<PortImpl> ports = new ArrayList<PortImpl>();
        ports.addAll(this.inputPorts);
        ports.addAll(this.outputPorts);
        if (this.controlInPort != null) {
            ports.add(this.controlInPort);
        }
        ports.addAll(this.controlOutPorts);
        if (this.eprPort != null) {
            ports.add(this.eprPort);
        }
        return ports;
    }

    /**
     * Checks if this node contains a specified port.
     * 
     * @param port
     *            The specified port
     * @return true if this node contains port; false otherwise
     */
    public boolean containsPort(Port port) {
        boolean contain = this.inputPorts.contains(port) || this.outputPorts.contains(port);
        return contain;
    }

    /**
     * @param controlInPort
     */
    public void setControlInPort(ControlPort controlInPort) {
        controlInPort.setKind(Kind.CONTROL_IN);
        this.controlInPort = controlInPort;
        addPort(this.controlInPort);
    }

    /**
     * @param controlOutPort
     */
    public void addControlOutPort(ControlPort controlOutPort) {
        controlOutPort.setKind(Kind.CONTROL_OUT);
        this.controlOutPorts.add(controlOutPort);
        addPort(controlOutPort);
    }

    /**
     * @param eprPort
     */
    public void setEPRPort(PortImpl eprPort) {
        eprPort.setKind(Kind.EPR);
        this.eprPort = eprPort;
        addPort(eprPort);
    }

    /**
     * Sets a graph this node belogs to.
     * 
     * @param graph
     *            The graph
     */
    protected void setGraph(GraphImpl graph) {
        this.graph = graph;
    }

    protected void indexToPointer() throws GraphException {
        for (String portID : this.inputPortIDs) {
            PortImpl port = this.graph.getPort(portID);
            if (port == null) {
                throw new GraphException("Port, " + portID + ", does not exist.");
            }
            port.setKind(PortImpl.Kind.DATA_IN);
            port.setNode(this);
            this.inputPorts.add((DataPort) port);
        }

        for (String portID : this.outputPortIDs) {
            PortImpl port = this.graph.getPort(portID);
            if (port == null) {
                throw new GraphException("Port, " + portID + ", does not exist.");
            }
            port.setKind(PortImpl.Kind.DATA_OUT);
            port.setNode(this);
            this.outputPorts.add((DataPort) port);
        }

        if (this.controlInPortID != null) {
            PortImpl port = this.graph.getPort(this.controlInPortID);
            if (port == null) {
                throw new GraphException("Port, " + this.controlInPortID + ", does not exist.");
            }
            port.setKind(PortImpl.Kind.CONTROL_IN);
            port.setNode(this);
            this.controlInPort = (ControlPort) port;
        }

        for (String portID : this.controlOutPortIDs) {
            PortImpl port = this.graph.getPort(portID);
            if (port == null) {
                throw new GraphException("Port, " + portID + ", does not exist.");
            }
            port.setKind(PortImpl.Kind.CONTROL_OUT);
            port.setNode(this);
            this.controlOutPorts.add((ControlPort) port);
        }

        if (this.eprPortID != null) {
            PortImpl port = this.graph.getPort(this.eprPortID);
            if (port == null) {
                throw new GraphException("Port, " + this.eprPortID + ", does not exist.");
            }
            port.setKind(PortImpl.Kind.EPR);
            port.setNode(this);
            this.eprPort = port;
        }
    }

    /**
     * @param nodeElement
     * @throws GraphException
     */
    protected void parse(XmlElement nodeElement) throws GraphException {
        XmlElement idElement = nodeElement.element(GraphSchema.NODE_ID_TAG);
        this.id = idElement.requiredText();

        XmlElement nameElement = nodeElement.element(GraphSchema.NODE_NAME_TAG);
        this.name = nameElement.requiredText();

        // XmlElement labelElement = nodeElement
        // .element(GraphSchema.NODE_STREAM_LABEL_TAG);
        // if (null != labelElement) {
        // this.label = labelElement.requiredText();
        // }

        Iterable<XmlElement> inputPortElements = nodeElement.elements(null, GraphSchema.NODE_INPUT_PORT_TAG);
        for (XmlElement inputPort : inputPortElements) {
            this.inputPortIDs.add(inputPort.requiredText());
        }

        Iterable<XmlElement> outputPortElements = nodeElement.elements(null, GraphSchema.NODE_OUTPUT_PORT_TAG);
        for (XmlElement outputPort : outputPortElements) {
            this.outputPortIDs.add(outputPort.requiredText());
        }

        XmlElement controlInPortElement = nodeElement.element(GraphSchema.NODE_CONTROL_IN_PORT_TAG);
        if (controlInPortElement != null) {
            this.controlInPortID = controlInPortElement.requiredText();
        }

        Iterable<XmlElement> controlOutPortElements = nodeElement.elements(null, GraphSchema.NODE_CONTROL_OUT_PORT_TAG);
        for (XmlElement controlOutPort : controlOutPortElements) {
            this.controlOutPortIDs.add(controlOutPort.requiredText());
        }

        XmlElement eprPortElement = nodeElement.element(GraphSchema.NODE_EPR_PORT_TAG);
        if (eprPortElement != null) {
            this.eprPortID = eprPortElement.requiredText();
        }

        XmlElement xElement = nodeElement.element(GraphSchema.NODE_X_LOCATION_TAG);
        this.position.x = (int) Double.parseDouble(xElement.requiredText());

        XmlElement yElement = nodeElement.element(GraphSchema.NODE_Y_LOCATION_TAG);
        this.position.y = (int) Double.parseDouble(yElement.requiredText());

        XmlElement configElement = nodeElement.element(GraphSchema.NODE_CONFIG_TAG);
        if (configElement != null) {
            parseConfiguration(configElement);
        }

        XmlElement componentElement = nodeElement.element(GraphSchema.NODE_COMPONENT_TAG);
        if (componentElement != null) {
            // XXX Not used since the introduction of .xwf
            parseComponent(componentElement);
        }
    }

    protected void parse(JsonObject nodeObject) {
        this.id = nodeObject.getAsJsonPrimitive(GraphSchema.NODE_ID_TAG).getAsString();
        this.name = nodeObject.getAsJsonPrimitive(GraphSchema.NODE_NAME_TAG).getAsString();

        JsonArray jArray;
        if (nodeObject.get(GraphSchema.NODE_INPUT_PORT_TAG) != null) {
            jArray  = nodeObject.getAsJsonArray(GraphSchema.NODE_INPUT_PORT_TAG);
            for (JsonElement jsonElement : jArray) {
                this.inputPortIDs.add(jsonElement.getAsString());
            }

        }

        if (nodeObject.get(GraphSchema.NODE_OUTPUT_PORT_TAG) != null) {
            jArray = nodeObject.getAsJsonArray(GraphSchema.NODE_OUTPUT_PORT_TAG);
            for (JsonElement jsonElement : jArray) {
                this.outputPortIDs.add(jsonElement.getAsString());
            }

        }

        JsonElement jElement = nodeObject.get(GraphSchema.NODE_CONTROL_IN_PORT_TAG);
        if (jElement != null) {
           this.controlInPortID = jElement.getAsString();
        }

        if (nodeObject.get(GraphSchema.NODE_CONTROL_OUT_PORT_TAG) != null) {
            jArray = nodeObject.getAsJsonArray(GraphSchema.NODE_CONTROL_OUT_PORT_TAG);
            for (JsonElement jsonElement : jArray) {
                this.controlOutPortIDs.add(jsonElement.getAsString());
            }
        }

        jElement = nodeObject.get(GraphSchema.NODE_EPR_PORT_TAG);
        if (jElement != null) {
            this.eprPortID = jElement.getAsString();
        }

        this.position.x = nodeObject.get(GraphSchema.NODE_X_LOCATION_TAG).getAsInt();
        this.position.y = nodeObject.get(GraphSchema.NODE_Y_LOCATION_TAG).getAsInt();

        // Parse config element not sure why we used it.
        // Parse component element.
        JsonObject configObject = nodeObject.getAsJsonObject(GraphSchema.NODE_CONFIG_TAG);
        if (configObject != null) {
            parseConfiguration(configObject);
        }

    }

    /**
     * @param componentElement
     * @throws GraphException
     *             When the component is in wrong format. This might be thrown by the sub classes.
     */
    @SuppressWarnings("unused")
    @Deprecated
    protected void parseComponent(XmlElement componentElement) throws GraphException {
        logger.debug("Entering:" + componentElement);
        // Do nothing by default.
    }

    protected void parseConfiguration(XmlElement configElement) {
        logger.debug("Entering:" + configElement);
        // Do nothing by default.
    }

    protected void parseConfiguration(JsonObject configObject) {
        logger.debug("Entering:" + new Gson().toJson(configObject));
    }

    /**
     * @return the node xml
     */
    protected XmlElement toXML() {
        XmlElement nodeElement = XMLUtil.BUILDER.newFragment(GraphSchema.NS, GraphSchema.NODE_TAG);

        XmlElement idElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_ID_TAG);
        idElement.addChild(this.id);

        XmlElement nameElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_NAME_TAG);
        nameElement.addChild(this.name);

        // if (null != this.label) {
        // XmlElement labelElement = nodeElement.addElement(GraphSchema.NS,
        // GraphSchema.NODE_STREAM_LABEL_TAG);
        //
        // labelElement.addChild(this.label);
        // }
        // Output ports
        for (PortImpl port : this.outputPorts) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_OUTPUT_PORT_TAG);
            portElement.addChild(port.getID());
        }

        // Input ports
        for (PortImpl port : this.inputPorts) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_INPUT_PORT_TAG);
            portElement.addChild(port.getID());
        }

        // Control-in port
        if (this.controlInPort != null) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONTROL_IN_PORT_TAG);
            portElement.addChild(this.controlInPort.getID());
        }

        // EPR Port
        if (this.eprPort != null) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_EPR_PORT_TAG);
            portElement.addChild(this.eprPort.getID());
        }

        // Control-out ports
        for (PortImpl port : this.controlOutPorts) {
            XmlElement portElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONTROL_OUT_PORT_TAG);
            portElement.addChild(port.getID());
        }

        XmlElement xElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_X_LOCATION_TAG);
        xElement.addChild(Integer.toString(this.position.x));

        XmlElement yElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_Y_LOCATION_TAG);
        yElement.addChild(Integer.toString(this.position.y));

        addConfigurationElement(nodeElement);

        return nodeElement;
    }

    protected JsonObject toJSON() {
        JsonObject nodeObject = new JsonObject();
        nodeObject.addProperty(GraphSchema.NODE_ID_TAG, getID());
        nodeObject.addProperty(GraphSchema.NODE_NAME_TAG, getName());

        if (this.inputPorts.size() > 0) {
            JsonArray inputPortsArray = new JsonArray();
            for (PortImpl inputPort : this.inputPorts) {
                inputPortsArray.add(new JsonPrimitive(inputPort.getID()));
            }
            nodeObject.add(GraphSchema.NODE_INPUT_PORT_TAG, inputPortsArray);
        }

        if (this.outputPorts.size() > 0) {
            JsonArray outputPortsArray = new JsonArray();
            for (PortImpl outputPort : this.outputPorts) {
                outputPortsArray.add(new JsonPrimitive(outputPort.getID()));
            }
            nodeObject.add(GraphSchema.NODE_OUTPUT_PORT_TAG, outputPortsArray);
        }

        if (this.controlInPort != null) {
            nodeObject.addProperty(GraphSchema.NODE_CONTROL_IN_PORT_TAG, this.controlInPort.getID());
        }

        if (this.controlOutPorts.size() > 0) {
            JsonArray controlOutPortArray = new JsonArray();
            for (PortImpl controlOutPort : this.controlOutPorts) {
                controlOutPortArray.add(new JsonPrimitive(controlOutPort.getID()));
            }
            nodeObject.add(GraphSchema.NODE_CONTROL_OUT_PORT_TAG, controlOutPortArray);
        }

        nodeObject.addProperty(GraphSchema.NODE_X_LOCATION_TAG, Integer.toString(this.position.x));
        nodeObject.addProperty(GraphSchema.NODE_Y_LOCATION_TAG, Integer.toString(this.position.y));

        addConfigurationElement(nodeObject);

        return nodeObject;
    }

    /**
     * Adds a configuration element to a specified node element.
     * 
     * @param nodeElement
     *            The specified node element
     * @return The configuration element added
     */
    @SuppressWarnings("unused")
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        // Do nothing by default.
        return null;
    }

    protected JsonObject addConfigurationElement(JsonObject nodeObject) {
        // Do nothing by default.
        return null;
    }    /**
     * Called when an Edge was added. It doesn't do anything by default.
     * 
     * @param edge
     * @throws GraphException
     *             When the added edge is not allowed. This might be thrown by subclasses.
     */
    @SuppressWarnings("unused")
    protected void edgeWasAdded(Edge edge) throws GraphException {
        // Do nothing
    }

    /**
     * Called when an Edge was removed. It doesn't do anything by default.
     * 
     * @param edge
     */
    @SuppressWarnings("unused")
    protected void edgeWasRemoved(Edge edge) {
        // Do nothing
    }

    private void addPort(PortImpl port) {
        port.setNode(this);
        this.graph.addPort(port);
    }

    public boolean isBreak() {
        return this.breakOnExecution;
    }

    public void setBreak(boolean breakVal) {
        this.breakOnExecution = breakVal;
    }

    /**
	 * 
	 */
    public boolean isAllInPortsConnected() {
        for (Iterator<DataPort> iterator = this.inputPorts.iterator(); iterator.hasNext();) {
            DataPort port = iterator.next();
            if (port.getFromNode() == null) {
                return false;
            }

        }
        return true;

    }

    public DataPort getOutputPort(String fromPortID) {
        for (DataPort port : this.outputPorts) {
            if (port.getID().equals(fromPortID)) {
                return port;
            }
        }
        throw new WorkflowRuntimeException("Port with id not found :" + fromPortID);
    }

    public DataPort getInputPort(String id) {
        for (DataPort port : this.inputPorts) {
            if (port.getID().equals(id)) {
                return port;
            }
        }
        throw new WorkflowRuntimeException("Port with id not found :" + id);
    }



    /**
     * @return
     */
    public String getLabel() {
        return this.label;
    }

    public void setRequireJoin(boolean join) {
        this.requireJoin = join;
    }

    public boolean getRequireJoin() {
        return this.requireJoin;
    }
    
    @Override
    public NodeExecutionState getState() {
		return state;
	}
    
    @Override
    public void setState(NodeExecutionState state) {
		this.state = state;
		triggerNodeObservers(NodeUpdateType.STATE_CHANGED);
	}
    
    @Override
    public void registerObserver(NodeObserver o) {
    	observers.add(o);
    }
    
    @Override
    public void removeObserver(NodeObserver o) {
    	if (observers.contains(o)) {
			observers.remove(o);
		}
    }
    
    private void triggerNodeObservers(NodeUpdateType type){
    	for (NodeObserver o : observers) {
			try {
				o.nodeUpdated(type);
			} catch (Exception e) {
                logger.error(e.getMessage(), e);
			}
		}
    }
}