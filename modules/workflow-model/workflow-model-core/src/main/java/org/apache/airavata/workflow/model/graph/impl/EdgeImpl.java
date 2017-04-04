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

import com.google.gson.JsonObject;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.xmlpull.infoset.XmlElement;

/**
 * The Edge represents an edge that connects a uses port to a provide port.
 * 
 */
public abstract class EdgeImpl implements Edge {

    private GraphImpl graph;

    private PortImpl fromPort;

    private PortImpl toPort;
    // The followings are used only during parsing the XML.

    private String fromPortID;

    private String toPortID;

    private String label;

    /**
     * Creates an Edge.
     */
    public EdgeImpl() {
        // Do nothing
    }

    /**
     * Constructs a EdgeImpl.
     * 
     * @param edgeXml
     */
    public EdgeImpl(XmlElement edgeXml) {
        parse(edgeXml);
    }

    public EdgeImpl(JsonObject edgeObject) {
        parse(edgeObject);
    }
    /**
     * @see org.apache.airavata.workflow.model.graph.Edge#getFromPort()
     */
    public PortImpl getFromPort() {
        return this.fromPort;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.Edge#getToPort()
     */
    public PortImpl getToPort() {
        return this.toPort;
    }

    /**
     * Sets a graph this edge belogs to.
     * 
     * @param graph
     *            The graph
     */
    protected void setGraph(GraphImpl graph) {
        this.graph = graph;
    }

    /**
     * @param fromPort
     */
    protected void setFromPort(PortImpl fromPort) {
        this.fromPort = fromPort;
    }

    /**
     * @param toPort
     */
    protected void setToPort(PortImpl toPort) {
        this.toPort = toPort;
    }

    protected void indexToPointer() throws GraphException {
        this.fromPort = this.graph.getPort(this.fromPortID);
        if (this.fromPort == null) {
            throw new GraphException("Cannot find a port with the ID, " + this.fromPortID + ".");
        }
        this.toPort = this.graph.getPort(this.toPortID);
        if (this.toPort == null) {
            throw new GraphException("Cannot find a port with the ID, " + this.toPortID + ".");
        }

        // Has to do the above first because they are used in the edgeWasAdded
        // method.
        this.fromPort.addEdge(this);
        this.toPort.addEdge(this);
    }

    /**
     * @param edgeElement
     */
    protected void parse(XmlElement edgeElement) {
        XmlElement fromPortElement = edgeElement.element(GraphSchema.EDGE_FROM_PORT_TAG);
        this.fromPortID = fromPortElement.requiredText();

        XmlElement toPortElement = edgeElement.element(GraphSchema.EDGE_TO_PORT_TAG);
        this.toPortID = toPortElement.requiredText();
    }

    protected void parse(JsonObject edgeObject) {
        this.fromPortID = edgeObject.getAsJsonPrimitive(GraphSchema.EDGE_FROM_PORT_TAG).getAsString();
        this.toPortID = edgeObject.getAsJsonPrimitive(GraphSchema.EDGE_TO_PORT_TAG).getAsString();
    }
    /**
     * @return the XmlElement
     */
    protected XmlElement toXML() {
        XmlElement edgeXml = XMLUtil.BUILDER.newFragment(GraphSchema.NS, GraphSchema.EDGE_TAG);

        XmlElement fromEle = edgeXml.addElement(GraphSchema.NS, GraphSchema.EDGE_FROM_PORT_TAG);
        fromEle.addChild(this.fromPort.getID());

        XmlElement toEle = edgeXml.addElement(GraphSchema.NS, GraphSchema.EDGE_TO_PORT_TAG);
        toEle.addChild(this.toPort.getID());

        return edgeXml;
    }

    protected JsonObject toJSON() {
        JsonObject edgeElement = new JsonObject();
        edgeElement.addProperty(GraphSchema.EDGE_FROM_PORT_TAG, this.fromPort.getID());
        edgeElement.addProperty(GraphSchema.EDGE_TO_PORT_TAG, this.toPort.getID());
        return edgeElement;
    }

    /**
     * Returns the label.
     * 
     * @return The label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets label.
     * 
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

}