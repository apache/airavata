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
package org.apache.airavata.workflow.model.graph.amazon;

import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.amazon.TerminateInstanceComponent;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.xmlpull.infoset.XmlElement;

public class TerminateInstanceNode extends ResourceNode {

    private boolean startNewInstance;

    /**
     * 
     * Constructs a InstanceNode.
     * 
     * @param graph
     */
    public TerminateInstanceNode(Graph graph) {
        super(graph);
        this.startNewInstance = true;
    }

    /**
     * Constructs an InstanceNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public TerminateInstanceNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#getComponent()
     */
    @Override
    public Component getComponent() {
        Component component = super.getComponent();
        if (component == null) {
            // The component is null when read from the graph XML.
            component = new TerminateInstanceComponent();
            setComponent(component);
        }
        return component;
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#toXML()
     */
    @Override
    protected XmlElement toXML() {
        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_TERMINATE);
        return nodeElement;
    }

    public boolean isStartNewInstance() {
        return this.startNewInstance;
    }

    public void setStartNewInstance(boolean startNewInstance) {
        this.startNewInstance = startNewInstance;
    }
}