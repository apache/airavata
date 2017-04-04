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
package org.apache.airavata.workflow.core.dag.port;

import org.apache.airavata.model.PortModel;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;

public class InputPortIml implements InPort {

    private InputDataObjectType inputDataObjectType;
    private boolean ready = false;
    private Edge edge;
    private WorkflowNode node;
    private PortModel portModel;
    private DataType dataType;

    public InputPortIml(PortModel portModel) {
        this.portModel = portModel;
    }

    @Override
    public void setInputObject(InputDataObjectType inputObject) {
        this.inputDataObjectType = inputObject;
        ready = (inputDataObjectType.getValue() != null && !inputDataObjectType.getValue().equals(""))
                || !inputDataObjectType.isIsRequired();
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
    public String getDefaultValue() {
        return getPortModel().getValue();
    }

    public void setDefaultValue(String defaultValue) {
        getPortModel().setValue(defaultValue);
    }

    @Override
    public void setPortModel(PortModel portModel) {
        this.portModel = portModel;
    }

    @Override
    public PortModel getPortModel() {
        return portModel;
    }

    @Override
    public boolean isReady() {
        return getInputObject() != null && (!inputDataObjectType.isIsRequired() ||
                (inputDataObjectType.getValue() != null && !inputDataObjectType.getValue().equals("")));
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
        return getPortModel().getPortId();
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getNodeId() {
        return getNode().getId();
    }

}
