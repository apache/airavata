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

import org.apache.airavata.workflow.model.component.system.ReceiveComponent;
import org.apache.airavata.workflow.model.component.system.SystemComponentDataPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.xmlpull.infoset.XmlElement;

public class ReceiveNode extends SystemNode {

    /**
     * Creates a InputNode.
     * 
     * @param graph
     */
    public ReceiveNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs a InputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public ReceiveNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public ReceiveComponent getComponent() {
        ReceiveComponent component = (ReceiveComponent) super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new ReceiveComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * Adds additional output port.
     */
    public void addOutputPort() {
        ReceiveComponent component = getComponent();
        SystemComponentDataPort outputPort = component.getOutputPort();
        SystemDataPort port = outputPort.createPort();
        addOutputPort(port);
    }

    /**
     * @throws GraphException
     */
    public void removeOutputPort() throws GraphException {
        List<DataPort> outputPorts = getOutputPorts();
        // Remove the last one.
        DataPort outputPort = outputPorts.get(outputPorts.size() - 1);
        removeOutputPort(outputPort);
    }

    @Override
    protected void edgeWasAdded(Edge edge) {
        // TODO
    }

    @Override
    protected void parseConfiguration(XmlElement configElement) {
        super.parseConfiguration(configElement);
    }

    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_RECEIVE);
        return nodeElement;
    }

    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);
        return configElement;
    }
}