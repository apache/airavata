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

import org.apache.airavata.workflow.model.component.system.IfComponent;
import org.apache.airavata.workflow.model.component.system.SystemComponentDataPort;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.xmlpull.infoset.XmlElement;

public class IfNode extends SystemNode {

    private static final String XPATH_TAG_NAME = "xpath";

    private String xpath;

    /**
     * Creates a InputNode.
     * 
     * @param graph
     */
    public IfNode(Graph graph) {
        super(graph);

        // Set the default to $0, which means that the input is boolean and
        // xpath uses it as it is.
        this.xpath = "$0";
    }

    /**
     * Constructs an IfNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public IfNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * Returns the xpath.
     * 
     * @return The xpath
     */
    public String getXPath() {
        return this.xpath;
    }

    /**
     * Sets xpath.
     * 
     * @param xpath
     *            The xpath to set.
     */
    public void setXPath(String xpath) {
        this.xpath = xpath;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public IfComponent getComponent() {
        IfComponent component = (IfComponent) super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new IfComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * Adds additional input port.
     */
    public void addInputPort() {
        IfComponent component = getComponent();
        SystemComponentDataPort input = component.getInputPort();
        SystemDataPort port = input.createPort();
        addInputPort(port);
    }

    /**
     * @throws GraphException
     */
    public void removeInputPort() throws GraphException {
        List<DataPort> inputPorts = getInputPorts();
        // Remove the last one.
        DataPort inputPort = inputPorts.get(inputPorts.size() - 1);
        removeInputPort(inputPort);
    }

    @Override
    protected void parseConfiguration(XmlElement configElement) {
        super.parseConfiguration(configElement);
        XmlElement element = configElement.element(null, XPATH_TAG_NAME);
        if (element != null) {
            this.xpath = element.requiredText();
        }
    }

    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_IF);
        return nodeElement;
    }

    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);
        if (this.xpath != null) {
            XmlElement element = configElement.addElement(GraphSchema.NS, XPATH_TAG_NAME);
            element.addChild(this.xpath.toString());
        }
        return configElement;
    }
}