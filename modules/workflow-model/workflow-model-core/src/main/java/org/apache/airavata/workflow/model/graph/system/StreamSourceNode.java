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

import java.awt.Point;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.system.StreamSourceComponent;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Port;
//import org.apache.axiom.om.util.UUIDGenerator;
import org.xmlpull.infoset.XmlElement;

public class StreamSourceNode extends ParameterNode {

    private String streamSourceURL;

    private ArrayList<InputNode> inputNodes = new ArrayList<InputNode>();

    private String label;

    /**
     * Creates an InputNode.
     * 
     * @param graph
     */
    public StreamSourceNode(Graph graph) {
        super(graph);
        this.label = UUID.randomUUID().toString();
    }

    /**
     * Constructs an InputNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public StreamSourceNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
        this.label = UUID.randomUUID().toString();
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new StreamSourceComponent();
            setComponent(component);
        }
        return component;
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
     * Returns the streamSourceURL.
     * 
     * @return The streamSourceURL
     */
    public String getStreamSourceURL() {
        return this.streamSourceURL;
    }

    /**
     * Sets streamSourceURL.
     * 
     * @param streamSourceURL
     *            The streamSourceURL to set.
     */
    public void setStreamSourceURL(String streamSourceURL) {
        this.streamSourceURL = streamSourceURL;
    }

    /**
     * @param inputNode
     */
    public void addInputNode(InputNode inputNode) {
        if (!this.inputNodes.contains(inputNode)) {
            this.inputNodes.add(inputNode);
        }
        setPosition(this.getPosition());

    }

    @Override
    public void setPosition(Point point) {
        super.setPosition(point);
        int count = 0;
        for (InputNode inputNode : this.inputNodes) {
            inputNode.setPosition(new Point(point.x + 5 + count * 5, point.y + 25 + count * 45));
            ++count;
        }
    }

    public ArrayList<InputNode> getInputNodes() {
        return this.inputNodes;
    }

    /**
	 * 
	 */
    public String getlabel() {
        return this.label;
    }

    public XmlElement toXML() {
        XmlElement xml = super.toXML();
        xml.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_STREAM_SOURCE);
        return xml;
    }

}