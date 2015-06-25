/*
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

package org.apache.airavata.workflow.core.dag.nodes;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.workflow.core.dag.port.OutPort;

public class WorkflowInputNodeImpl implements WorkflowInputNode {

    private NodeState myState = NodeState.READY;
    private final String nodeId;
    private String nodeName;
    private OutPort outPort;
    private InputDataObjectType inputDataObjectType;
    private String name;

    public WorkflowInputNodeImpl(String nodeId) {
        this(nodeId, null);
    }

    public WorkflowInputNodeImpl(String nodeId, String nodeName) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    @Override
    public String getId() {
        return this.nodeId;
    }

    @Override
    public String getName() {
        return this.nodeName;
    }

    @Override
    public NodeType getType() {
        return NodeType.WORKFLOW_INPUT;
    }

    @Override
    public NodeState getState() {
        return myState;
    }

    @Override
    public void setState(NodeState newState) {
        if (newState.getLevel() > myState.getLevel()) {
            myState = newState;
        } else {
            throw new IllegalStateException("Node state can't be reversed. currentState : " + myState.toString() + " , newState " + newState.toString());
        }
    }

    @Override
    public boolean isReady() {
        return (inputDataObjectType.getValue() != null && !inputDataObjectType.getValue().equals(""))
                || !inputDataObjectType.isIsRequired();
    }

    @Override
    public InputDataObjectType getInputObject() {
        return this.inputDataObjectType;
    }

    @Override
    public void setInputObject(InputDataObjectType inputObject) {
        this.inputDataObjectType = inputObject;
    }

    @Override
    public OutPort getOutPort() {
        return this.outPort;
    }

    @Override
    public void setOutPort(OutPort outPort) {
        this.outPort = outPort;
    }


}
