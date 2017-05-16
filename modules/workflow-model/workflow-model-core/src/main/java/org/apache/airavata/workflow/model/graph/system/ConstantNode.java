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
package org.apache.airavata.workflow.model.graph.system;

import java.util.List;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.system.ConstantComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class ConstantNode extends SystemNode {

    private static final String DATA_TYPE_QNAME_TAG = "dataType";

    private static final String VALUE_TAG_NAME = "value";

    private static final Logger logger = LoggerFactory.getLogger(ConstantNode.class);

    private DataType type;

    private Object value;

    /**
     * Creates a InputNode.
     * 
     * @param graph
     */
    public ConstantNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs a InputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public ConstantNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new ConstantComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        super.setName(name);
        // Creates the ID to the new one based on the new name. This is
        // for readability of workflow scripts.
        createID();
    }

    /**
     * Checks if this ConstantNode is connected.
     * 
     * @return true if this InputNode is connected; false otherwise;
     */
    public boolean isConnected() {
        if (getEdges().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the type of the parameter
     * 
     * @return The type of the parameter (e.g. string, int)
     */
    public DataType getType() {
        return this.type;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The default value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the port of this InputNode.
     * 
     * Note that an InputNode always has only one output port.
     * 
     * @return The port
     */
    public DataPort getPort() {
        return getOutputPorts().get(0);
    }

    /**
     * Returns the first port that this input node is connected to.
     * 
     * @return The first port that this input node is connected to
     */
    public Port getConnectedPort() {
        return getPort().getEdge(0).getToPort();
    }

    /**
     * Checks if the user input is valid.
     * 
     * @param input
     *            The user input
     * @return true if the user input is valid against the parameter type; false otherwise
     */
    public boolean isInputValid(String input) {
        logger.debug("Input:" + input);
        // TODO now returning true because only string is used.
        return true;
    }

    /**
     * Called whan an Edge was added to the parameter port. Change the name of this node.
     * 
     * @throws GraphException
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.impl.EdgeImpl)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        super.edgeWasAdded(edge);
        // TODO this method can be removed.
        Port toPort = edge.getToPort();

        if (edge instanceof DataEdge) {
            DataPort toDataPort = (DataPort) toPort;
            DataType toType = toDataPort.getType();

            List edges = getEdges();
            if (edges.size() == 1) {
                // The first edge.
                this.type = toType;
            } else if (edges.size() > 1) {
                // Not the first edge.
                if (!toType.equals(WSConstants.XSD_ANY_TYPE) && !this.type.equals(toType)) {
                    throw new GraphException("Cannot connect ports with different types.");
                }

            } else {
                // Should not happen.
                throw new WorkflowRuntimeException("edges.size(): " + edges.size());
            }
        }
    }

    /**
     * Called whan an Edge was removed from the parameter port. Change the name of the node.
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasRemoved(org.apache.airavata.workflow.model.graph.impl.EdgeImpl)
     */
    @Override
    protected void edgeWasRemoved(Edge removedEdge) {
        super.edgeWasRemoved(removedEdge);
        // TODO this method can be removed.
        List<DataEdge> edges = getEdges();
        if (edges.size() == 0) {
            // The last edge was removed.
            this.type = null;
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.system.SystemNode#portTypeChanged(org.apache.airavata.workflow.model.graph.system.SystemDataPort)
     */
    @Override
    protected void portTypeChanged(SystemDataPort port) throws GraphException {
        super.portTypeChanged(port);
        this.type = port.getType();
    }

    @Override
    protected void parseComponent(XmlElement componentElement) {
        // No need to parse the XML.
        setComponent(new ConstantComponent());
    }

    @Override
    protected void parseConfiguration(XmlElement configElement) {
        super.parseConfiguration(configElement);

        XmlElement typeElement = configElement.element(null, DATA_TYPE_QNAME_TAG);
        if (typeElement != null) {
            String qnameText = typeElement.requiredText();
            if (qnameText != null && !qnameText.equals("")) {
                this.type = DataType.valueOf(qnameText);
//                this.type = QName.valueOf(qnameText);
            }
        }

        XmlElement element = configElement.element(null, VALUE_TAG_NAME);
        if (element != null) {
            // It might be a String or XmlElement
            for (Object child : element.children()) {
                if (child instanceof String) {
                    if (((String) child).trim().length() == 0) {
                        // Skip white space before xml element.
                        continue;
                    }
                }
                this.value = child;
                break;
            }
            // this.defaultValue = element.requiredText();
        }
    }

    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_CONSTANT);
        return nodeElement;
    }

    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);
        if (this.type != null) {
            XmlElement qnameElement = configElement.addElement(GraphSchema.NS, DATA_TYPE_QNAME_TAG);
            qnameElement.addChild(this.type.toString());
        }
        if (this.value != null) {
            XmlElement element = configElement.addElement(GraphSchema.NS, VALUE_TAG_NAME);
            element.addChild(this.value);
        }
        return configElement;
    }

    private List<DataEdge> getEdges() {
        DataPort port = getPort();
        List<DataEdge> edges = port.getEdges();
        return edges;
    }
}