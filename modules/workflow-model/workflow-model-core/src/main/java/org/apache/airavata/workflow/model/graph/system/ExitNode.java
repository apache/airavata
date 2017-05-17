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

import org.apache.airavata.workflow.model.component.system.ExitComponent;
import org.apache.airavata.workflow.model.graph.ControlEdge;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.xmlpull.infoset.XmlElement;

public class ExitNode extends SystemNode {

    /**
     * Constructs a BPELExitNode.
     * 
     * @param graph
     */
    public ExitNode(Graph graph) {
        super(graph);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void parseComponent(XmlElement componentElement) {
        // No need to parse the XML.
        setComponent(new ExitComponent());
    }

    /**
     * @return the node xml
     */
    @Override
    protected XmlElement toXML() {

        XmlElement nodeElement = super.toXML();
        nodeElement.setAttributeValue(GraphSchema.NS, GraphSchema.NODE_TYPE_ATTRIBUTE, GraphSchema.NODE_TYPE_EXIT);
        return nodeElement;
    }

    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        super.edgeWasAdded(edge);
        if (edge instanceof ControlEdge) {
            List<ControlEdge> edges = getEdges();
            if (edges.size() > 1) {
                throw new GraphException("Cannot connect more than one Control Ports to the Exit node.");
            }
        }
    }

    protected List<ControlEdge> getEdges() {
        PortImpl port = getControlInPort();
        List<ControlEdge> edges = (List<ControlEdge>) port.getEdges();
        return edges;
    }

}