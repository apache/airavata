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
import org.apache.airavata.workflow.model.component.system.EndBlockComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.xmlpull.infoset.XmlElement;

public class EndBlockNode extends SystemNode {

    /**
     * Creates a InputNode.
     * 
     * @param graph
     */
    public EndBlockNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs a InputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public EndBlockNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public EndBlockComponent getComponent() {
        EndBlockComponent component = (EndBlockComponent) super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new EndBlockComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * Adds additional input port.
     */
    public void addInputPort() {
        EndBlockComponent component = getComponent();
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
        EndBlockComponent component = getComponent();
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
     * @see org.apache.airavata.workflow.model.graph.system.SystemNode#portTypeChanged(org.apache.airavata.workflow.model.graph.system.SystemDataPort)
     */
    @Override
    protected void portTypeChanged(SystemDataPort port) throws GraphException {
        super.portTypeChanged(port);

        List<DataPort> inputPorts = getInputPorts();
        List<DataPort> outputPorts = getOutputPorts();
        int size = outputPorts.size();

        Kind kind = port.getKind();
        int index;
        if (kind == Kind.DATA_IN) {
            index = inputPorts.indexOf(port) % size;
        } else if (kind == Kind.DATA_OUT) {
            index = outputPorts.indexOf(port);
        } else {
            throw new WorkflowRuntimeException();
        }

        DataPort inputPort1 = inputPorts.get(index);
        DataPort inputPort2 = inputPorts.get(size + index);
        DataPort outputPort = outputPorts.get(index);

        DataType inputType1 = inputPort1.getType();
        DataType inputType2 = inputPort2.getType();
        DataType outputType = outputPort.getType();

        DataType portType = port.getType();
        if (portType == null || portType.equals(WSConstants.XSD_ANY_TYPE)) {
            // Do nothing
            return;
        }

        if (port == inputPort1) {
            // input1 -> input2
            if (inputType2.equals(WSConstants.XSD_ANY_TYPE)) {
                inputPort2.copyType(port);
            } else if (inputType2.equals(portType)) {
                // Do nothing.
            } else {
                String message = "The type of input " + index + " (" + inputType1 + ") of " + getID()
                        + " must be same as the type of input " + (index + size) + " (" + inputType2 + ").";
                throw new GraphException(message);
            }
            // input1 -> output
            if (outputType.equals(WSConstants.XSD_ANY_TYPE)) {
                outputPort.copyType(port);
            } else if (outputType.equals(portType)) {
                // Do nothing.
            } else {
                String message = "The type of input " + index + " (" + inputType1 + ") of " + getID()
                        + " must be same as the type of output " + index + " (" + outputType + ").";
                throw new GraphException(message);
            }

        } else if (port == inputPort2) {
            // input2 -> input1
            if (inputType1.equals(WSConstants.XSD_ANY_TYPE)) {
                inputPort1.copyType(port);
            } else if (inputType1.equals(portType)) {
                // Do nothing.
            } else {
                String message = "The type of input " + index + " (" + inputType1 + ") of " + getID()
                        + " must be same as the type of input " + (index + size) + " (" + inputType2 + ").";
                throw new GraphException(message);
            }
            // input2 -> output
            if (outputType.equals(WSConstants.XSD_ANY_TYPE)) {
                outputPort.copyType(port);
            } else if (outputType.equals(portType)) {
                // Do nothing.
            } else {
                String message = "The type of input " + (index + size) + " (" + inputType2 + ") of " + getID()
                        + " must be same as the type of output " + index + " (" + outputType + ").";
                throw new GraphException(message);
            }
        } else if (port == outputPort) {
            // output -> input1
            if (inputType1.equals(WSConstants.XSD_ANY_TYPE)) {
                inputPort1.copyType(port);
            } else if (inputType1.equals(portType)) {
                // Do nothing.
            } else {
                String message = "The type of input " + index + " (" + inputType1 + ") of " + getID()
                        + " must be same as the type of output " + index + " (" + outputType + ").";
                throw new GraphException(message);
            }
            // output -> input2
            if (inputType2.equals(WSConstants.XSD_ANY_TYPE)) {
                inputPort2.copyType(port);
            } else if (inputType2.equals(portType)) {
                // Do nothing.
            } else {
                String message = "The type of input " + (index + size) + " (" + inputType2 + ") of " + getID()
                        + " must be same as the type of input " + index + " (" + outputType + ").";
                throw new GraphException(message);
            }
        } else {
            throw new WorkflowRuntimeException();
        }
    }

    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_ENDBLOCK);
        return nodeElement;
    }
}