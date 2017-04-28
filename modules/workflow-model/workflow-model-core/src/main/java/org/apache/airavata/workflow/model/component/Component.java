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
package org.apache.airavata.workflow.model.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.workflow.model.graph.ControlPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.EPRPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;

public abstract class Component {

    protected String name;

    protected String description;

    protected ComponentControlPort controlInPort;

    protected List<ComponentControlPort> controlOutPorts;

    protected ComponentEPRPort eprPort;

    /**
     * Creates a Component.
     */
    public Component() {
        this.controlOutPorts = new ArrayList<ComponentControlPort>();
    }

    /**
     * Sets the name of the component.
     * 
     * @param name
     *            The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the component.
     * 
     * @return The name of the component
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the description of the component.
     * 
     * @param description
     *            The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the component.
     * 
     * @return The description of the component
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return The list of input ComponentDataPort
     */
    public abstract List<? extends ComponentDataPort> getInputPorts();

    /**
     * @return The list of output ComponentDataPort
     */
    public abstract List<? extends ComponentDataPort> getOutputPorts();

    /**
     * Returns the index'th input component port.
     * 
     * @param index
     *            The index
     * @return The component port
     */
    public ComponentPort getInputPort(int index) {
        return getInputPorts().get(index);
    }

    /**
     * Returns the index'th output component port.
     * 
     * @param index
     *            The index
     * @return The component port
     */
    public ComponentPort getOutputPort(int index) {
        List<? extends ComponentPort> outs = getOutputPorts();
        if (index >= outs.size()) {
            throw new IllegalArgumentException("index, " + index + ", must be less than " + outs.size() + ".");
        }
        return outs.get(index);
    }

    /**
     * @return The controlInPort.
     */
    public ComponentPort getControlInPort() {
        return this.controlInPort;
    }

    /**
     * @return The list of controlOutPorts.
     */
    public List<ComponentControlPort> getControlOutPorts() {
        return this.controlOutPorts;
    }

    /**
     * @param index
     * @return The controlOutPort at the specified index.
     */
    public ComponentControlPort getControlOutPort(int index) {
        return getControlOutPorts().get(index);
    }

    /**
     * @return The EPR Port
     */
    public ComponentEPRPort getEPRPort() {
        return this.eprPort;
    }

    /**
     * Creates a new node from this component in a specified graph.
     * 
     * @param graph
     *            The specified graph
     * 
     * @return The Node created
     */
    abstract public Node createNode(Graph graph);

    protected void createPorts(NodeImpl node) {
        for (ComponentDataPort input : getInputPorts()) {
            DataPort port = input.createPort();
            node.addInputPort(port);
        }

        for (ComponentDataPort output : getOutputPorts()) {
            DataPort port = output.createPort();
            node.addOutputPort(port);
        }

        if (this.controlInPort != null) {
            ControlPort port = this.controlInPort.createPort();
            node.setControlInPort(port);
        }

        for (ComponentControlPort componentPort : this.controlOutPorts) {
            ControlPort port = componentPort.createPort();
            node.addControlOutPort(port);
        }

        if (this.eprPort != null) {
            EPRPort port = this.eprPort.createPort();
            node.setEPRPort(port);
        }
    }

    /**
     * Returns the HTML to show the information of this Component
     * 
     * @return The string representation of the HTML
     */
    abstract public String toHTML();
}