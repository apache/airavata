/*
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
 *
 */

package org.apache.airavata.xbaya.graph.ws;

import org.apache.airavata.xbaya.component.ws.WorkflowComponent;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.ws.gui.WorkflowNodeGUI;
import org.xmlpull.infoset.XmlElement;

public class WorkflowNode extends WSNode {

    private WorkflowNodeGUI gui;

    /**
     * Constructs a WorkflowNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public WorkflowNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
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
     * @see org.apache.airavata.xbaya.graph.ws.WSNode#getGUI()
     */
    @Override
    public NodeGUI getGUI() {
        if (this.gui == null) {
            this.gui = new WorkflowNodeGUI(this);
        }
        return this.gui;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.ws.WSNode#getComponent()
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

}