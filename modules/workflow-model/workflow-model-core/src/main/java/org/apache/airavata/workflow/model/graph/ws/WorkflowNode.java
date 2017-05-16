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
package org.apache.airavata.workflow.model.graph.ws;

import com.google.gson.JsonObject;
import org.apache.airavata.workflow.model.component.ws.WorkflowComponent;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.xmlpull.infoset.XmlElement;

public class WorkflowNode extends WSNode {

    /**
     * Constructs a WorkflowNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public WorkflowNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    public WorkflowNode(JsonObject nodeObject) throws GraphException{
        super(nodeObject);
    }
    /**
     * Constructs a WorkflowNode.
     * 
     * @param graph
     */
    public WorkflowNode(Graph graph) {
        super(graph);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.ws.WSNode#getComponent()
     */
    @Override
    public WorkflowComponent getComponent() {
        return (WorkflowComponent) super.getComponent();
    }

    @Override
    public XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_WORKFLOW);
        return nodeElement;
    }

    @Override
    protected JsonObject toJSON() {
        JsonObject nodeObject = (JsonObject) super.toJSON();
        nodeObject.addProperty(GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_WORKFLOW);
        return nodeObject;
    }
}