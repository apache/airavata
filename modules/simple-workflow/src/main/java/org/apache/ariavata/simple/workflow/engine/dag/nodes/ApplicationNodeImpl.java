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


package org.apache.ariavata.simple.workflow.engine.dag.nodes;


import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;

import java.util.List;

public class ApplicationNodeImpl implements ApplicationNode {

    private final String nodeId;
    private NodeState myState = NodeState.WAITING;
    private String applicationId;

    public ApplicationNodeImpl(String nodeId) {
        this(nodeId, null);
    }

    public ApplicationNodeImpl(String nodeId, String applicationId) {
        this.nodeId = nodeId;
        this.applicationId = applicationId;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public String getNodeName() {
        return null; // TODO: Auto generated method body.
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
    public boolean isSatisfy() {
        return false; // TODO: Auto generated method body.
    }

    @Override
    public String getApplicationId() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public List<InPort> getInputPorts() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public List<OutPort> getOutputPorts() {
        return null; // TODO: Auto generated method body.
    }
}
