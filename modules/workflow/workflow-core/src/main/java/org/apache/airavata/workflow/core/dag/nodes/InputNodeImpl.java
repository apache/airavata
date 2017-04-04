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
import org.apache.airavata.workflow.core.dag.port.OutPort;

import java.util.ArrayList;
import java.util.List;

public class InputNodeImpl implements InputNode {

    private NodeModel nodeModel;
    private InputDataObjectType inputDataObjectType;
    private PortModel portModel;
    private List<Edge> edges = new ArrayList<>();
    private String value;
    private DataType dataType;


    public InputNodeImpl(NodeModel nodeModel) {
        this.nodeModel = nodeModel;
        setPortModel(convert(nodeModel));
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
    public String getName() {
        return getNodeModel().getName();
    }

    @Override
    public NodeType getType() {
        return NodeType.WORKFLOW_INPUT;
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
        return (inputDataObjectType.getValue() != null && !inputDataObjectType.getValue().equals(""))
                || !inputDataObjectType.isIsRequired();
    }

    @Override
    public WorkflowNode getNode() {
        return this;
    }

    @Override
    public void setNode(WorkflowNode workflowNode) {
        // InputNode itself a workflowNode
    }

    @Override
    public InputDataObjectType getInputObject() {
        if (inputDataObjectType == null) {
            inputDataObjectType = new InputDataObjectType(getName());
            inputDataObjectType.setValue(getValue());
            inputDataObjectType.setType(getDataType());
//            inputDataObjectType.setIsRequired(true);
//            inputDataObjectType.setDataStaged(true);
//            inputDataObjectType.setInputOrder(0);
        }
        return inputDataObjectType;
    }

    @Override
    public void setInputObject(InputDataObjectType inputObject) {
        this.inputDataObjectType = inputObject;
    }

    @Override
    public OutPort getOutPort() {
        return this;
    }

    @Override
    public void setOutPort(OutPort outPort) {
        // InputNode is a outPort
    }

    @Override
    public void setValue(String value) {
        this.value = value;
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
    public String getValue() {
        return value;
    }

    @Override
    public void setOutputObject(OutputDataObjectType outputObject) {

    }


    @Override
    public OutputDataObjectType getOutputObject() {
        return convert(getInputObject());
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    private PortModel convert(NodeModel nodeModel) {
        PortModel portModel = new PortModel(nodeModel.getNodeId());
        portModel.setName(nodeModel.getName());
        portModel.setDescription(nodeModel.getDescription());
        return portModel;
    }

    private OutputDataObjectType convert(InputDataObjectType inputObject) {
        OutputDataObjectType output = new OutputDataObjectType(inputObject.getName());
        output.setType(inputObject.getType());
        output.setValue(inputObject.getValue());
        output.setIsRequired(inputObject.isIsRequired());
        output.setApplicationArgument(inputObject.getApplicationArgument());
        output.setOutputStreaming(false);
//        output.setDataMovement(true);
        return output;
    }
}
