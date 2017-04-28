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
import org.apache.airavata.workflow.core.dag.port.InPort;
import org.apache.airavata.workflow.core.dag.port.OutPort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApplicationNodeImpl implements ApplicationNode {

    private NodeModel nodeModel;
    private List<InPort> inPorts = new ArrayList<>();
    private List<OutPort> outPorts = new ArrayList<>();

    public ApplicationNodeImpl(NodeModel nodeModel) {
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
        return nodeModel.getNodeId();
    }

    @Override
    public String getName() {
        return getNodeModel().getName();
    }

    @Override
    public NodeType getType() {
        return NodeType.APPLICATION;
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
    public boolean isReady() {
        for (InPort inPort : getInputPorts()) {
            if (!inPort.isReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getApplicationId() {
        return getNodeModel().getApplicationId();
    }

    @Override
    public String getApplicationName() {
        return null;
    }

    @Override
    public void addInPort(InPort inPort) {
        this.inPorts.add(inPort);
    }

    @Override
    public List<InPort> getInputPorts() {
        return this.inPorts;
    }

    @Override
    public void addOutPort(OutPort outPort) {
        this.outPorts.add(outPort);
    }

    @Override
    public List<OutPort> getOutputPorts() {
        return this.outPorts;
    }

    @Override
    public void addInputPorts(Collection<? extends InPort> inPorts) {
        this.inPorts.addAll(inPorts);
    }

    @Override
    public void addOutPorts(Collection<? extends OutPort> outPorts) {
        this.outPorts.addAll(outPorts);
    }
}
