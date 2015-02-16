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
package org.apache.ariavata.simple.workflow.engine.dag.port;

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.ariavata.simple.workflow.engine.dag.edge.Edge;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;

import java.util.List;

public class InputPortIml implements InPort {

    private InputDataObjectType inputDataObjectType;
    private boolean isSatisfy = false;
    private String portId;
    private Edge edge;
    private WorkflowNode node;

    public InputPortIml(String portId) {
        this.portId = portId;
    }

    @Override
    public void setInputObject(InputDataObjectType inputObject) {
        this.inputDataObjectType = inputObject;
    }

    @Override
    public InputDataObjectType getInputObject() {
        return this.inputDataObjectType;
    }

    @Override
    public Edge getEdge() {
        return this.edge;
    }

    @Override
    public void addEdge(Edge edge) {
        this.edge = edge;
    }

    @Override
    public boolean isSatisfy() {
        return inputDataObjectType.getValue() != null && !inputDataObjectType.getValue().equals("");
    }

    @Override
    public WorkflowNode getNode() {
        return this.node;
    }

    @Override
    public void setNode(WorkflowNode workflowNode) {
        this.node = workflowNode;
    }

    @Override
    public String getId() {
        return this.portId;
    }

}
