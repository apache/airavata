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
package org.apache.airavata.workflow.engine.datadriven;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.Pair;
import org.apache.airavata.workflow.model.component.system.InputComponent;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.workflow.model.wf.Workflow;

public class WorkflowHarvester {

    public WorkflowHarvester() {

    }

    public Workflow[] harvest(Workflow workflow, QName dataType) {
        LinkedList<Workflow> harvest = new LinkedList<Workflow>();
        LinkedList<Pair<String, String>> candidates = getCandidates(workflow, dataType);
        for (Pair<String, String> pair : candidates) {
            Workflow clone = workflow.clone();

            NodeImpl node = clone.getGraph().getNode(pair.getLeft());
            if (null == node) {
                throw new WorkflowRuntimeException("Specified node not found:" + pair.getLeft());
            }
            Port candidatePort = null;
            List<DataPort> inPorts = node.getInputPorts();
            for (DataPort dataPort : inPorts) {
                if (pair.getRight().equals(dataPort.getID())) {
                    candidatePort = dataPort;
                    break;
                }
            }
            if (null == candidatePort) {
                throw new WorkflowRuntimeException("Specifies Port was not found:" + pair.getRight());
            }
            if (!(candidatePort.getFromNode() instanceof InputNode)) {
                removeUnnecessaryNodes(node, candidatePort, clone);
                Node input = clone.addNode(new InputComponent());
                input.setPosition(new Point(Math.max(0, node.getPosition().x - 150), node.getPosition().y));

                // the returned workflows size should be less than that of the
                // original
                if (clone.getGraph().getNodes().size() < workflow.getGraph().getNodes().size()
                // if the sizes the different its a candidate, but need
                // to make sure
                // its not the same as one already harvested
                        && !isWorkflowAlreadyHarvested(harvest, clone)) {
                    try {
                        clone.getGraph().addEdge(input.getOutputPort(0), candidatePort);
                        cleanLeftOverInputNodes(clone);
                    } catch (GraphException e) {
                        throw new RuntimeException(e);
                    }

                    harvest.add(clone);
                }

            }
        }
        return harvest.toArray(new Workflow[0]);
    }

    /**
     * @param clone
     */
    private void cleanLeftOverInputNodes(Workflow clone) {

        List<NodeImpl> nodes = clone.getGraph().getNodes();
        LinkedList<Node> removeList = new LinkedList<Node>();
        for (Node nodeImpl : nodes) {
            if (nodeImpl instanceof InputNode) {
                if (nodeImpl.getOutputPort(0).getToNodes().size() == 0) {
                    removeList.add(nodeImpl);
                }
            }
        }
        for (Node node : removeList) {
            try {
                clone.removeNode(node);
            } catch (GraphException e) {
                throw new WorkflowRuntimeException(e);
            }
        }
    }

    /**
     * @param harvest
     * @param clone
     * @return
     */
    private boolean isWorkflowAlreadyHarvested(LinkedList<Workflow> harvest, Workflow clone) {
        for (Workflow workflow : harvest) {
            if (workflow.equals(clone)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param pair
     * @param clone
     */
    private void removeUnnecessaryNodes(Node node, Port candidatePort, Workflow workflow) {
        if (candidatePort.getFromPort().getEdges().size() == 1) {
            Node fromNode = candidatePort.getFromNode();
            try {
                List<DataPort> inputPorts = fromNode.getInputPorts();
                for (DataPort dataPort : inputPorts) {
                    removeUnnecessaryNodes(fromNode, dataPort, workflow);
                }
                workflow.removeNode(fromNode);
            } catch (GraphException e) {
                throw new WorkflowRuntimeException(e);
            }
        }
    }

    /**
     * @param pair
     * @return
     */
    private List<DataPort> getRemainderPorts(Pair<WSNode, DataPort> pair) {
        LinkedList<DataPort> ret = new LinkedList<DataPort>();
        List<DataPort> inputPorts = pair.getLeft().getInputPorts();
        for (DataPort dataPort : inputPorts) {
            if (pair.getRight() != dataPort) {
                ret.add(dataPort);
            }
        }
        return ret;
    }

    /**
     * @param workflow
     * @param dataType
     * @return pair of nodeid and portid
     */
    private LinkedList<Pair<String, String>> getCandidates(Workflow workflow, QName dataType) {
        LinkedList<Pair<String, String>> candidates = new LinkedList<Pair<String, String>>();
        List<NodeImpl> nodes = workflow.getGraph().getNodes();
        for (NodeImpl node : nodes) {
            if (node instanceof WSNode) {
                List<DataPort> inputPorts = ((WSNode) node).getInputPorts();
                for (DataPort dataPort : inputPorts) {

                    if (dataType.equals(dataPort.getType())) {
                        candidates.add(new Pair<String, String>(node.getID(), dataPort.getID()));
                    }

                }
            }
        }

        return candidates;
    }

}