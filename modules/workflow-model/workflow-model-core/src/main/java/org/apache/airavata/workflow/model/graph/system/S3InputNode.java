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
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.system.S3InputComponent;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.ws.WSPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class S3InputNode extends InputNode {

    private static final String VALUE_TAG_NAME = "value";

    private static final Logger logger = LoggerFactory.getLogger(S3InputNode.class);

    private Object defaultValue;

    /**
     * Creates an InputNode.
     * 
     * @param graph
     */
    public S3InputNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs an InputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public S3InputNode(XmlElement nodeElement) throws GraphException {
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
            component = new S3InputComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * Returns the type of the parameter
     * 
     * @return The type of the parameter (e.g. string, int)
     */
    @Override
    public DataType getParameterType() {
        List<DataEdge> edges = getEdges();
        DataType parameterType = super.getParameterType();
        if (parameterType == null && getEdges().size() > 0) {
            // This happens when the graph XML doesn't have parameterType.
            DataEdge edge = edges.get(0);
            DataPort toPort = edge.getToPort();
            parameterType = toPort.getType();
        }
        return parameterType;
    }

    /**
     * Returns the default value.
     * 
     * @return The defaultValue.
     */
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Sets the default value.
     * 
     * @param defaultValue
     *            The default value to set.
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the port of this InputNode.
     * 
     * Note that an InputNode always has only one output port.
     * 
     * @return The port
     */
    @Override
    public SystemDataPort getPort() {
        return (SystemDataPort) getOutputPorts().get(0);
    }

    /**
     * Returns the first port that this input node is connected to.
     * 
     * @return The first port that this input node is connected to
     */
    @Override
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
        // TODO type checks
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

        // TODO organize this.
        if (edge instanceof DataEdge) {
            DataEdge dataEdge = (DataEdge) edge;
            DataPort toPort = dataEdge.getToPort();
            DataType toType = toPort.getType();

            List<DataEdge> edges = getEdges();
            if (edges.size() == 1) {
                // The first edge.
                setParameterType(toType);

                if (!isConfigured() && toPort instanceof WSPort) {
                    // Copy
                    copyDefaultConfiguration((WSPort) toPort);
                }
            } else if (edges.size() > 1) {
                // Not the first edge.
                DataType parameterType = getParameterType();
                if (!toType.equals(WSConstants.XSD_ANY_TYPE) && !parameterType.equals(toType)) {
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
        // TODO organize this.
        List<DataEdge> edges = getEdges();
        if (edges.size() == 0) {
            setParameterType(null);

            if (!isConfigured()) {
                // Reset
                setName(getComponent().getName());
                setDescription("");
                setDefaultValue(null);
                setMetadata(null);
            }

        } else {
            Edge edge = edges.get(0);
            Port toPort = edge.getToPort();
            WSPort toWsPort = (WSPort) toPort;
            DataType toType = toWsPort.getType();
            setParameterType(toType);

            if (!isConfigured()) {
                // Copy
                copyDefaultConfiguration(toWsPort);
            }
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.system.SystemNode#portTypeChanged(org.apache.airavata.workflow.model.graph.system.SystemDataPort)
     */
    @Override
    protected void portTypeChanged(SystemDataPort port) throws GraphException {
        super.portTypeChanged(port);
        setParameterType(port.getType());
    }

    @Override
    protected void parseComponent(XmlElement componentElement) {
        // No need to parse the XML.
        setComponent(new S3InputComponent());
    }

    @Override
    protected void parseConfiguration(XmlElement configElement) {
        super.parseConfiguration(configElement);
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
                this.defaultValue = child;
                break;
            }
            // this.defaultValue = element.requiredText();
        }
    }

    @Override
    public XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_INPUT);
        return nodeElement;
    }

    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = super.addConfigurationElement(nodeElement);
        if (this.defaultValue != null) {
            XmlElement element = configElement.addElement(GraphSchema.NS, VALUE_TAG_NAME);
            element.addChild(this.defaultValue);
        }
        return configElement;
    }

    /**
     * @param toWSPort
     */
    private void copyDefaultConfiguration(WSPort toWSPort) {
        // TODO support recursive search for WSPort in case the input is
        // connected to special nodes.
        setName(toWSPort.getName());
        WSComponentPort componentPort = toWSPort.getComponentPort();
        setDescription(componentPort.getDescription());
        setDefaultValue(componentPort.getDefaultValue());
        setMetadata(componentPort.getAppinfo());
    }

}