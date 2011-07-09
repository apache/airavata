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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.dynamic.CepComponent;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.graph.DataEdge;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.GraphFactory;
import org.apache.airavata.xbaya.graph.GraphSchema;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.dynamic.CepNode;
import org.apache.airavata.xbaya.graph.dynamic.CombineMultipleStreamNode;
import org.apache.airavata.xbaya.graph.impl.EdgeImpl;
import org.apache.airavata.xbaya.graph.impl.GraphImpl;
import org.apache.airavata.xbaya.graph.impl.NodeCloneFactory;
import org.apache.airavata.xbaya.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.gui.StreamSourceNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlElement;

public class WSGraph extends GraphImpl {

    private XmlElement metadata;

    private XmlElement inputMetadata;

    private XmlElement outputMetadata;

    /**
     * Constructs a WSGraph.
     * 
     * @param factory
     */
    public WSGraph(GraphFactory factory) {
        super(factory);
    }

    /**
     * Returns the metadata.
     * 
     * @return The metadata
     */
    public XmlElement getMetadata() {
        return this.metadata;
    }

    /**
     * Sets metadata.
     * 
     * @param metadata
     *            The metadata to set.
     */
    public void setMetadata(XmlElement metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the inputMetadata.
     * 
     * @return The inputMetadata
     */
    public XmlElement getInputMetadata() {
        return this.inputMetadata;
    }

    /**
     * Sets inputMetadata.
     * 
     * @param inputMetadata
     *            The inputMetadata to set.
     */
    public void setInputMetadata(XmlElement inputMetadata) {
        this.inputMetadata = inputMetadata;
    }

    /**
     * Returns the outputMetadata.
     * 
     * @return The outputMetadata
     */
    public XmlElement getOutputMetadata() {
        return this.outputMetadata;
    }

    /**
     * Sets outputMetadata.
     * 
     * @param outputMetadata
     *            The outputMetadata to set.
     */
    public void setOutputMetadata(XmlElement outputMetadata) {
        this.outputMetadata = outputMetadata;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.GraphImpl#toXML(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void toXML(XmlElement graphElement) {
        super.toXML(graphElement);

        graphElement.setAttributeValue(GraphSchema.NS, GraphSchema.GRAPH_TYPE_ATTRIBUTE, GraphSchema.GRAPH_TYPE_WS);

        if (this.metadata != null) {
            XmlElement metadataElement = graphElement.addElement(GraphSchema.NS, GraphSchema.GRAPH_METADATA_TAG);
            // Clone the metadata to avoid parent problem because this can be
            // called multiple times.
            metadataElement.addChild(XMLUtil.deepClone(this.metadata));
        }

        if (this.inputMetadata != null) {
            XmlElement metadataElement = graphElement.addElement(GraphSchema.NS, GraphSchema.GRAPH_INPUT_METADATA_TAG);
            // Clone the metadata to avoid parent problem because this can be
            // called multiple times.
            metadataElement.addChild(XMLUtil.deepClone(this.inputMetadata));
        }

        if (this.outputMetadata != null) {
            XmlElement metadataElement = graphElement.addElement(GraphSchema.NS, GraphSchema.GRAPH_OUTPUT_METADATA_TAG);
            // Clone the metadata to avoid parent problem because this can be
            // called multiple times.
            metadataElement.addChild(XMLUtil.deepClone(this.outputMetadata));
        }
    }

    /**
     * @see org.apache.airavata.xbaya.graph.impl.GraphImpl#parse(org.xmlpull.infoset.XmlElement)
     */
    @Override
    protected void parse(XmlElement graphElement) throws GraphException {
        super.parse(graphElement);

        XmlElement metadataElement = graphElement.element(GraphSchema.GRAPH_METADATA_TAG);
        if (metadataElement != null) {
            for (XmlElement appinfo : metadataElement.requiredElementContent()) {
                this.metadata = appinfo;
                // It should have only one element.
                break;
            }
        }

        XmlElement inputMetadataElement = graphElement.element(GraphSchema.GRAPH_INPUT_METADATA_TAG);
        if (inputMetadataElement != null) {
            for (XmlElement appinfo : inputMetadataElement.requiredElementContent()) {
                this.inputMetadata = appinfo;
                // It should have only one element.
                break;
            }
        }

        XmlElement outputMetadataElement = graphElement.element(GraphSchema.GRAPH_OUTPUT_METADATA_TAG);
        if (outputMetadataElement != null) {
            for (XmlElement appinfo : outputMetadataElement.requiredElementContent()) {
                this.outputMetadata = appinfo;
                // It should have only one element.
                break;
            }
        }
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
     * @param name
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
     * @param subworkflowNodes
     * @throws GraphException
     */
    public void configureWithd(Workflow oldWorkflow, List<Node> subworkflowNodes) throws GraphException {
        HashMap<Node, Node> nodeMap = new HashMap<Node, Node>();
        for (Node node : subworkflowNodes) {
            nodeMap.put(node, node);
        }

        Workflow clone = oldWorkflow.clone();
        clone.setName(clone.getName() + "_subWorkflow");
        List<NodeImpl> allOldNodes = clone.getGraph().getNodes();
        for (NodeImpl nodeImpl : allOldNodes) {
            if (null == nodeMap.get(nodeImpl)) {
                clone.removeNode(nodeImpl);
            }
        }

        // clone nodes and set it to this graph
        for (Node node : subworkflowNodes) {

            nodeMap.put(node, NodeCloneFactory.clone(node, this));

        }

        for (Node node : subworkflowNodes) {
            Node newNode = nodeMap.get(node);

            List<DataPort> inputPorts = node.getInputPorts();
            for (DataPort dataPort : inputPorts) {
                DataPort newPort = newNode.getInputPort(dataPort.getID());
                Node fromNode = dataPort.getFromNode();
                Node newFromNode = nodeMap.get(fromNode);
                if (null == newFromNode) {
                    // input to this subworkflow
                    InputNode inputNode = new InputComponent().createNode(this);
                    addEdge(inputNode.getOutputPort(0), newPort);
                } else {
                    String fromPortID = dataPort.getFromPort().getID();
                    addEdge(newFromNode.getOutputPort(fromPortID), newPort);
                }
            }

            List<DataPort> outputPorts = node.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                DataPort newPort = newNode.getOutputPort(dataPort.getID());

                List<Port> toNodes = dataPort.getToPorts();
                for (Port toPort : toNodes) {
                    Node newToNode = nodeMap.get(toPort.getNode());
                    if (null == newToNode) {
                        // outputnode
                        Node outputNode = new OutputComponent().createNode(this);
                        addEdge(newPort, outputNode.getInputPort(0));
                    } else {

                        addEdge(newPort, newToNode.getInputPort(toPort.getID()));
                    }
                }
            }

        }

    }

    public HashMap<String, LinkedList<Node>> labelIntroduceJoinsAndGetSubSets() throws XBayaException {

        parseGraph();

        labelGraph();
        HashMap<String, LinkedList<Node>> graphPartitionSets = getGraphPartitionSets();
        List<Node> joinRequiredNodes = GraphUtil.getJoinRequiredNodes(this);
        for (Node node : joinRequiredNodes) {
            CepNode newCEPJoinNode = (CepNode) new CepComponent().createNode(this);
            String newLabel = GraphUtil.introduceDynamicNode(node, newCEPJoinNode, this);
            LinkedList<Node> list = new LinkedList<Node>();
            list.add(newCEPJoinNode);
            graphPartitionSets.put(newLabel, list);
        }

        return graphPartitionSets;
    }

    /**
     * @throws XBayaException
     * 
     */
    private void parseGraph() throws XBayaException {
        topologicalSort();
        LinkedList<CepNode> cepNodes = GraphUtil.getCEPNodes(this);
        for (CepNode node : cepNodes) {
            if (node instanceof CombineMultipleStreamNode) {
                if (((CombineMultipleStreamNode) node).getStreamName() == null) {
                    throw new XBayaException("CombineMultipleStreamNode need to configure the queries:"
                            + node.getName());
                }
            } else if (null == node.getQuery()) {
                throw new XBayaException("CepNode need to configure the queries:" + node.getName());
            }
        }

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

    /**
	 * 
	 */
    private void labelGraph() {
        LinkedList<Node> workQueue = new LinkedList<Node>();

        List<InputNode> inputNodes = GraphUtil.getInputNodes(this);
        for (InputNode inputNode : inputNodes) {
            inputNode.setLabel(XBayaConstants.STATIC_LABEL);
            List<DataPort> outputPorts = inputNode.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                workQueue.addAll(dataPort.getToNodes());
                List<DataEdge> edges = dataPort.getEdges();
                for (DataEdge dataEdge : edges) {
                    dataEdge.setLabel(XBayaConstants.STATIC_LABEL);
                }
            }
        }

        LinkedList<StreamSourceNode> streamSourceNodes = GraphUtil.getStreamSourceNodes(this);
        for (StreamSourceNode input : streamSourceNodes) {
            String label = input.getlabel();
            List<DataPort> outputPorts = input.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                workQueue.addAll(dataPort.getToNodes());
                List<DataEdge> edges = dataPort.getEdges();
                for (DataEdge dataEdge : edges) {
                    dataEdge.setLabel(label);
                }
            }
        }

        while (!workQueue.isEmpty()) {
            Node processingNode = workQueue.remove();
            if (!GraphUtil.isAllInputsConnected(processingNode)) {
                throw new XBayaRuntimeException("Disconnedted node inputs during labeling");
            }
            if (GraphUtil.isAllInputsLabeled(processingNode)) {
                String sameLabel = GraphUtil.isSameLabeledInput(processingNode);
                if (null != sameLabel) {
                    // same label so its a regular node

                    if (GraphUtil.isRegulerNode(processingNode)) {
                        processingNode.setLabel(sameLabel);
                        String nodeLabel = processingNode.getLabel();

                        List<DataPort> outputPorts = processingNode.getOutputPorts();
                        for (DataPort dataPort : outputPorts) {
                            List<DataEdge> edges = dataPort.getEdges();
                            for (DataEdge dataEdge : edges) {
                                dataEdge.setLabel(nodeLabel);
                            }
                            workQueue.addAll(dataPort.getToNodes());
                        }
                    } else {
                        processingNode.inventLabel(GraphUtil.getEncodedInputLabels(processingNode));
                        String nodeOutLabel = processingNode.getLabel() + "_out";

                        List<DataPort> outputPorts = processingNode.getOutputPorts();
                        for (DataPort dataPort : outputPorts) {
                            List<DataEdge> edges = dataPort.getEdges();
                            for (DataEdge dataEdge : edges) {
                                dataEdge.setLabel(nodeOutLabel);
                            }
                            workQueue.addAll(dataPort.getToNodes());
                        }
                    }

                } else {
                    // may need to introduce a join
                    // if its CEP node we assume it doesnt require
                    // joins
                    // Its hard to determine if the CEP node would
                    // resolve the joins
                    if (!GraphUtil.isCEPNode(processingNode)) {
                        processingNode.setRequireJoin(true);

                    }
                    processingNode.inventLabel(GraphUtil.getEncodedInputLabels(processingNode));
                    String nodeOutLabel = processingNode.getLabel() + "_out";

                    List<DataPort> outputPorts = processingNode.getOutputPorts();
                    for (DataPort dataPort : outputPorts) {
                        List<DataEdge> edges = dataPort.getEdges();
                        for (DataEdge dataEdge : edges) {
                            dataEdge.setLabel(nodeOutLabel);
                        }
                        workQueue.addAll(dataPort.getToNodes());
                    }

                }
            } else {
                workQueue.add(processingNode);
            }
        }

        // if work queue is empty yet all are not labeled graph is not connected
        // !!
        List<EdgeImpl> edges = this.getEdges();
        for (EdgeImpl edge : edges) {
            if (null == edge.getLabel()) {
                throw new XBayaRuntimeException(
                        "Some edges were not labeled after labeling algorithm. Possibly partitioned graph");
            }
            System.out.println(edge.getLabel());
            System.out.println(edge);
        }

    }

}