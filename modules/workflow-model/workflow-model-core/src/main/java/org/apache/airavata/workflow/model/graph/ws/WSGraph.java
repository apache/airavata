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
import org.apache.airavata.workflow.model.graph.DataEdge;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.GraphFactory;
import org.apache.airavata.workflow.model.graph.GraphSchema;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.EdgeImpl;
import org.apache.airavata.workflow.model.graph.impl.GraphImpl;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.xmlpull.infoset.XmlElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class WSGraph extends GraphImpl {



    private boolean editable=true;
    /**
     * Constructs a WSGraph.
     * 
     * @param factory
     */
    public WSGraph(GraphFactory factory) {
        super(factory);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.GraphImpl#toXML(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void toXML(XmlElement graphElement) {
        super.toXML(graphElement);
    }


    protected void parse(JsonObject graphObject) throws GraphException {
        super.parse(graphObject);
    }

    /**
     * @see org.apache.airavata.workflow.model.graph.impl.GraphImpl#parse(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void parse(XmlElement graphElement) throws GraphException {
        super.parse(graphElement);
    }

    public boolean equals(WSGraph graph) {
        return !notEquals(graph);
    }

    /***
     * Finding Not equal is much efficient
     * 
     * @param graph
     * @return
     */
    public boolean notEquals(WSGraph graph) {
        if (graph.getNodes().size() != this.getNodes().size()) {
            return true;
        }
        // sizes are the same
        // try the names
        List<NodeImpl> thisNodes = this.getNodes();
        for (NodeImpl thisNode : thisNodes) {
            Node matchingNode = find(thisNode.getID(), graph.getNodes());
            if (null == matchingNode) {
                // not found so this is not equal
                return true;
            } else {
                // ok found, now check whether the connections match
                if (!inputEdgesMatch(thisNode, matchingNode)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param thisNode
     * @param matchingNode
     * @return
     */
    private boolean inputEdgesMatch(NodeImpl thisNode, Node matchingNode) {
        List<DataPort> thisInputPorts = thisNode.getInputPorts();
        // see whether the inputs are connected to component with same id
        for (int i = 0; i < thisInputPorts.size(); i++) {
            if (null != thisInputPorts.get(i).getFromNode()
                    && !thisInputPorts.get(i).getFromNode().getID()
                            .equals(matchingNode.getInputPort(i).getFromNode().getID())) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param id
     * @param nodes
     * @return
     */
    private Node find(String id, List<NodeImpl> nodes) {
        for (Node node : nodes) {
            if (node.getID().equals(id)) {
                return node;
            }
        }
        return null;
    }



 


    /**
     * @return
     * @throws GraphException
     * 
     */
    private LinkedList<Node> topologicalSort() throws GraphException {
        List<EdgeImpl> alledges = this.getEdges();
        HashSet<EdgeImpl> edgeSet = new HashSet<EdgeImpl>(alledges);
        List<Node> workQueue = new LinkedList<Node>(GraphUtil.getInputNodes(this));
        workQueue.addAll(GraphUtil.getStreamSourceNodes(this));
        LinkedList<Node> sortedOrder = new LinkedList<Node>();
        while (!workQueue.isEmpty()) {
            Node currentNode = workQueue.remove(0);
            sortedOrder.add(currentNode);
            List<DataPort> outputPorts = currentNode.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                List<DataEdge> curentEdges = dataPort.getEdges();
                for (DataEdge dataEdge : curentEdges) {
                    edgeSet.remove(dataEdge);
                    if (isAllEdgesRemoved(edgeSet, dataEdge.getToPort().getNode())) {
                        workQueue.add(dataEdge.getToPort().getNode());
                    }
                }
            }
        }
        if (edgeSet.isEmpty()) {
            return sortedOrder;
        } else {
            throw new GraphException("Graph Topological sorting failed, Graph has at least one cycle");
        }
    }

    /**
     * @param edgeSet
     * @param node
     * @return
     */
    private boolean isAllEdgesRemoved(HashSet<EdgeImpl> edgeSet, NodeImpl node) {

        List<DataPort> inputPorts = node.getInputPorts();
        for (DataPort dataPort : inputPorts) {
            List<DataEdge> edgesToCheck = dataPort.getEdges();
            for (DataEdge dataEdge : edgesToCheck) {
                if (edgeSet.contains(dataEdge)) {
                    return false;
                }
            }

            //
        }
        return true;
    }

    /**
     * @return
     * 
     * 
     */
    private HashMap<String, LinkedList<Node>> getGraphPartitionSets() {
        HashMap<String, LinkedList<Node>> partiotionGraph = GraphUtil.partitionGraphOnLabel(this);
        return partiotionGraph;
    }

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setEditable(boolean editable) {
		this.editable=editable;
	}


}