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

package org.apache.airavata.workflow.core.dag.nodes;

import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.workflow.core.dag.port.InPort;

public class WorkflowOutputNodeImpl implements WorkflowOutputNode {

    private NodeState myState = NodeState.WAITING;
    private final String nodeId;
    private String nodeName;
    private OutputDataObjectType outputDataObjectType;
    private InPort inPort;

    public WorkflowOutputNodeImpl(String nodeId) {
        this(nodeId, null);
    }

    public WorkflowOutputNodeImpl(String nodeId, String nodeName) {
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
        return NodeType.WORKFLOW_OUTPUT;
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
        return !(inPort.getInputObject() == null || inPort.getInputObject().getValue() == null
                || inPort.getInputObject().getValue().equals(""));
    }

    @Override
    public OutputDataObjectType getOutputObject() {
        return this.outputDataObjectType;
    }

    @Override
    public void setOutputObject(OutputDataObjectType outputObject) {
        this.outputDataObjectType = outputObject;
    }

    @Override
    public InPort getInPort() {
        return this.inPort;
    }

    @Override
    public void setInPort(InPort inPort) {
        this.inPort = inPort;
    }

}

