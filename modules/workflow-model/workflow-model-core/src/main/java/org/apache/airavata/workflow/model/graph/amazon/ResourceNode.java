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

import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.xmlpull.infoset.XmlElement;

/**
 * The placeholder for Instance Nodes type. Using for easy checking
 * 
 */
public abstract class ResourceNode extends NodeImpl {

    /**
     * 
     * Constructs a ResourceNode.
     * 
     * @param graph
     */
    public ResourceNode(Graph graph) {
        super(graph);
    }

    /**
     * Constructs an InstanceNode.
     * 
     * @param nodeElement
     * @throws GraphException
     */
    public ResourceNode(XmlElement nodeElement) throws GraphException {
        super(nodeElement);
    }

    /**
     * 
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#addConfigurationElement(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected XmlElement addConfigurationElement(XmlElement nodeElement) {
        XmlElement configElement = nodeElement.addElement(GraphSchema.NS, GraphSchema.NODE_CONFIG_TAG);
        return configElement;
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        super.edgeWasAdded(edge);

        if (edge instanceof DataEdge) {
            Port toPort = edge.getToPort();
            Node toNode = toPort.getNode();
            Port fromPort = edge.getFromPort();
            Node fromNode = fromPort.getNode();

            if (!(toNode instanceof ResourceNode && fromNode instanceof ResourceNode)) {
                throw new GraphException("Cannot connect Resource Node to other type of nodes");
            }
        }
    }
}