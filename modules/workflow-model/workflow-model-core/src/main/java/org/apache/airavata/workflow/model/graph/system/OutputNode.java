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

import javax.xml.namespace.QName;

import com.google.gson.JsonObject;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.system.OutputComponent;
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
import org.xmlpull.infoset.XmlElement;

public class OutputNode extends ParameterNode {

    /**
     * Creates a OutputNode.
     * 
     * @param graph
     */
    public OutputNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs a OutputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public OutputNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    public OutputNode(JsonObject nodeObject) throws GraphException {
        super(nodeObject);
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
            Edge edge = edges.get(0);
            WSPort fromPort = (WSPort) edge.getFromPort();
            setParameterType(fromPort.getType());
        }
        return parameterType;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new OutputComponent();
        }
        return component;
    }

    /**
     * Returns the port of this OutputNode.
     * 
     * Note that an OutputNode always has only one input port.
     * 
     * @return The port
     */
    @Override
    public SystemDataPort getPort() {
        return (SystemDataPort) getInputPorts().get(0);
    }

    /**
     * Returns the first port that this output node is connected from.
     * 
     * @return The first port that this output node is connected from
     */
    @Override
    public Port getConnectedPort() {
        return getPort().getEdge(0).getFromPort();
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
        // TODO organize
        Port fromPort = edge.getFromPort();

        if (edge instanceof DataEdge) {
            DataPort fromDataPort = (DataPort) fromPort;
            DataType fromType = fromDataPort.getType();

            List<DataEdge> edges = getEdges();
            if (edges.size() == 1) {
                setParameterType(fromType);

                if (!isConfigured() && fromDataPort instanceof WSPort) {
                    setName(fromDataPort.getName());
                    WSComponentPort componentPort = ((WSPort) fromDataPort).getComponentPort();
                    setDescription(componentPort.getDescription());
                    setMetadata(componentPort.getAppinfo());
                }
            } else {
                throw new GraphException("Cannot connect more than one output ports to the output parameter.");
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
        // TODO organize
        List<DataEdge> edges = getEdges();
        if (edges.size() == 0) {
            setParameterType(null);

            if (!isConfigured()) {
                // Reset
                setName(OutputComponent.NAME);
                setDescription("");
                setMetadata(null);
            }

        } else if (edges.size() == 1) {
            // This happens when the second edges was wrongly added and removed.
        } else {
            // Should not happen
            throw new WorkflowRuntimeException("edges.size(): " + edges.size());
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
        setComponent(new OutputComponent());
    }

    /**
     * @return the node xml
     */
    @Override
    public XmlElement toXML() {

        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_OUTPUT);
        return nodeElement;
    }

    @Override
    protected JsonObject toJSON() {
        JsonObject nodeObject = super.toJSON();
        nodeObject.addProperty(GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_OUTPUT);
        return nodeObject;
    }
}