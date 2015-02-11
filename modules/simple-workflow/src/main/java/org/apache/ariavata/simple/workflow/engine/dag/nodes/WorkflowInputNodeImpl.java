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

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutputPort;

import java.util.List;

public class WorkflowInputNodeImpl implements WorkflowInputNode {

    private NodeState myState = NodeState.READY;
    private final String nodeId;

    public WorkflowInputNodeImpl(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getNodeId() {
        return null;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.WORKFLOW_INPUT;
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
    public boolean isSatisfy() {
        return false; // TODO: Auto generated method body.
    }

    @Override
    public InputDataObjectType getInputObject() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public void addOutputPort(OutputPort outputPort) {
        // TODO: Auto generated method body.
    }

    @Override
    public List<OutputPort> getOutputPorts() {
        return null; // TODO: Auto generated method body.
    }

}
*/
