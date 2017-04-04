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
package org.apache.airavata.workflow.core.dag.nodes;

import org.apache.airavata.model.ComponentState;
import org.apache.airavata.model.ComponentStatus;
import org.apache.airavata.model.NodeModel;
import org.apache.airavata.model.PortModel;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.workflow.core.dag.edge.Edge;
import org.apache.airavata.workflow.core.dag.port.InPort;

public class OutputNodeImpl implements OutputNode {

    private NodeModel nodeModel;
    private OutputDataObjectType outputDataObjectType;
    private InputDataObjectType inputDataObjectType;
    private PortModel portModel;
    private String value;
    private DataType dataType;

    public OutputNodeImpl(NodeModel nodeModel) {
        this.nodeModel = nodeModel;
    }

    @Override
    public void setNodeModel(NodeModel nodeModel) {
        this.nodeModel = nodeModel;
    }

    @Override
    public NodeModel getNodeModel() {
        return nodeModel;
    }

    @Override
    public String getId() {
        return getNodeModel().getNodeId();
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

    @Override
    public String getName() {
        return getNodeModel().getName();
    }

    @Override
    public NodeType getType() {
        return NodeType.WORKFLOW_OUTPUT;
    }

    @Override
    public ComponentState getState() {
        return getStatus().getState();
    }

    @Override
    public ComponentStatus getStatus() {
        return getNodeModel().getStatus();
    }

    @Override
    public void setStatus(ComponentStatus newStatus) {
        getNodeModel().setStatus(newStatus);
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
        return !(getInputObject() == null || getInputObject().getValue() == null
                || getInputObject().getValue().equals(""));
    }

    @Override
    public WorkflowNode getNode() {
        return this;
    }

    @Override
    public void setNode(WorkflowNode workflowNode) {
        // OutputNode is a workflow Node.
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
        return this;
    }

    @Override
    public void setInPort(InPort inPort) {
        // outputNode is an inPort.
    }

    @Override
    public void setInputObject(InputDataObjectType inputObject) {
        this.inputDataObjectType = inputObject;
        setOutputObject(convert(inputObject));
    }

    private OutputDataObjectType convert(InputDataObjectType inputObject) {
        if (inputObject != null) {
            OutputDataObjectType output = new OutputDataObjectType(getName());
            output.setValue(inputObject.getValue());
            output.setType(inputObject.getType());
            return output;

        }
        return null;
    }

    @Override
    public InputDataObjectType getInputObject() {
        return inputDataObjectType;
    }

    @Override
    public Edge getEdge() {
        return null;
    }

    @Override
    public void addEdge(Edge edge) {

    }

    @Override
    public String getDefaultValue() {
        return value;
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        value = defaultValue;
    }
}

