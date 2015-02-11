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
 *//*


package org.apache.ariavata.simple.workflow.engine.dag.nodes;

import org.apache.ariavata.simple.workflow.engine.dag.links.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.port.InputPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutputPort;

import java.util.ArrayList;
import java.util.List;

public class ApplicationNodeImpl implements ApplicationNode {

    private final String nodeId;
    private NodeState myState = NodeState.WAITING;
    private List<Edge> inputLinks = new ArrayList<Edge>();
    private List<Edge> outputLinks = new ArrayList<Edge>();

    public ApplicationNodeImpl(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.APPLICATION;
    }

    @Override
    public NodeState getNodeState() {
        return myState;
    }

    @Override
    public void setNodeState(NodeState newNodeState) {
        // TODO: node state can't be reversed , correct order WAITING --> READY --> EXECUTING --> EXECUTED --> COMPLETE
        myState = newNodeState;
    }

    @Override
    public void addInputPort(InputPort inputPort) {

    }

    @Override
    public List<InputPort> getInputPorts() {
        return null;
    }

    @Override
    public void addOutputPort(OutputPort outputPort) {

    }

    @Override
    public List<OutputPort> getOutputPorts() {
        return null;
    }

    public List<Edge> getInputLinks() {
        return inputLinks;
    }

    public List<Edge> getOutputLinks() {
        return outputLinks;
    }

    public void setInputLinks(List<Edge> inputLinks) {
        this.inputLinks = inputLinks;
    }

    public void setOutputLinks(List<Edge> outputLinks) {
        this.outputLinks = outputLinks;
    }

    public void addInputLink(Edge inputLink) {
        inputLinks.add(inputLink);
    }

    public void addOutputLink(Edge outputLink) {
        outputLinks.add(outputLink);
    }
}
*/
