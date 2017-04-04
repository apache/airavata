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
import org.apache.airavata.workflow.model.component.ComponentDataPort;
import org.apache.airavata.workflow.model.component.system.EndForEachComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.xmlpull.infoset.XmlElement;

public class EndForEachNode extends SystemNode {

    /**
     * Creates a InputNode.
     * 
     * @param graph
     */
    public EndForEachNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs a InputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public EndForEachNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public EndForEachComponent getComponent() {
        EndForEachComponent component = (EndForEachComponent) super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new EndForEachComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * Adds additional input port.
     */
    public void addInputPort() {
        EndForEachComponent component = getComponent();
        ComponentDataPort input = component.getInputPort();
        DataPort port = input.createPort();
        addInputPort(port);
    }

    /**
     * Removes the last input port.
     * 
     * @throws GraphException
     */
    public void removeInputPort() throws GraphException {
        List<DataPort> inputPorts = getInputPorts();
        // Remove the last one.
        DataPort inputPort = inputPorts.get(inputPorts.size() - 1);
        removeInputPort(inputPort);
    }

    /**
     * Adds additional output port.
     */
    public void addOutputPort() {
        EndForEachComponent component = getComponent();
        ComponentDataPort outputPort = component.getOutputPort();
        DataPort port = outputPort.createPort();
        addOutputPort(port);
    }

    /**
     * Removes the last output port.
     * 
     * @throws GraphException
     */
    public void removeOutputPort() throws GraphException {
        List<DataPort> outputPorts = getOutputPorts();
        // Remove the last one.
        DataPort outputPort = outputPorts.get(outputPorts.size() - 1);
        removeOutputPort(outputPort);
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        // XXX cannot detect if the type is array or not from WSDL at this
        // point. so no check here.
        // super.edgeWasAdded(edge);

        Port fromPort = edge.getFromPort();
        Port toPort = edge.getToPort();
        if (edge instanceof DataEdge) {
            if (fromPort instanceof EPRPort) {
                // TODO
                return;
            }

            DataPort fromDataPort = (DataPort) fromPort;
            DataPort toDataPort = (DataPort) toPort;

            DataType fromType = fromDataPort.getType();
            DataType toType = toDataPort.getType();

            if (fromDataPort.getNode() == this) {
                if (!(toType == null || toType.equals(WSConstants.XSD_ANY_TYPE))) {
                    fromDataPort.copyType(toDataPort);
                }
            } else if (toDataPort.getNode() == this) {
                if (!(fromType == null || fromType.equals(WSConstants.XSD_ANY_TYPE))) {
                    toDataPort.copyType(fromDataPort);
                }
            } else {
                throw new WorkflowRuntimeException();
            }
        }
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.system.SystemNode#portTypeChanged(org.apache.airavata.workflow.model.graph.system.SystemDataPort)
     */
    @Override
    protected void portTypeChanged(SystemDataPort port) throws GraphException {
        super.portTypeChanged(port);

        List<DataPort> inputPorts = getInputPorts();
        List<DataPort> outputPorts = getOutputPorts();

        Kind kind = port.getKind();
        int index;
        if (kind == Kind.DATA_IN) {
            index = inputPorts.indexOf(port);
        } else if (kind == Kind.DATA_OUT) {
            index = outputPorts.indexOf(port);
        } else {
            throw new WorkflowRuntimeException();
        }

        SystemDataPort inputPort = (SystemDataPort) inputPorts.get(index);
        SystemDataPort outputPort = (SystemDataPort) outputPorts.get(index);

        DataType inputType = inputPort.getType();
        DataType outputType = outputPort.getType();

        DataType portType = port.getType();
        if (portType == null || portType.equals(WSConstants.XSD_ANY_TYPE)) {
            // Do nothing
            return;
        }

        if (port == inputPort) {
            // input -> output
            if (outputType.equals(WSConstants.XSD_ANY_TYPE)) {
                outputPort.copyType(port, 1);
            } else if (outputType.equals(portType)) {
                // Do nothing.
            } else {
                // XXX cannot parse array from WSDL.
                // String message = "The type of input " + index + " ("
                // + inputType + ") of " + getID()
                // + " must be same as the type of output " + index + " ("
                // + outputType + ").";
                // throw new GraphException(message);
            }

        } else if (port == outputPort) {
            // output -> input1
            if (inputType.equals(WSConstants.XSD_ANY_TYPE)) {
                inputPort.copyType(port, -1);
            } else if (inputType.equals(portType)) {
                // Do nothing.
            } else {
                // XXX cannot parse array from WSDL.
                // String message = "The type of input " + index + " ("
                // + inputType + ") of " + getID()
                // + " must be same as the type of output " + index + " ("
                // + outputType + ").";
                // throw new GraphException(message);
            }
        } else {
            throw new WorkflowRuntimeException();
        }
    }

    @Override
    protected void parseConfiguration(XmlElement configElement) {
        super.parseConfiguration(configElement);
    }

    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_MERGE);
        return nodeElement;
    }

    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);
        return configElement;
    }
}