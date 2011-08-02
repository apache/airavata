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

package org.apache.airavata.xbaya.component.dynamic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.wf.Workflow;

public class GenericSubWorkflowComponent extends CepComponent {

    private Workflow workflow;

    public GenericSubWorkflowComponent(Workflow workflow) {
        super();
        this.setName(workflow.getName());
        this.workflow = workflow;
        ODEClient odeClient = new ODEClient();
        List<InputNode> inputNodes = odeClient.getInputNodes(workflow);
        this.inputs = new LinkedList<CepComponentPort>();
        this.outputs = new LinkedList<CepComponentPort>();
        for (InputNode inputNode : inputNodes) {
            this.inputs.add(new GenericWubWorkflowComponentPort(inputNode.getOutputPort(0).getToPorts().get(0).getID(),
                    inputNode.getParameterType(), this));
        }
        LinkedList<OutputNode> outputNodes = odeClient.getoutNodes(workflow);
        for (OutputNode outputNode : outputNodes) {
            this.outputs.add(new GenericWubWorkflowComponentPort(outputNode.getInputPort(0).getFromPort().getID(),
                    outputNode.getParameterType(), this));
        }

    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#getInputPorts()
     */
    @Override
    public List<CepComponentPort> getInputPorts() {
        // TODO Auto-generated method stub
        return this.inputs;
    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#getOutputPorts()
     */
    @Override
    public List<CepComponentPort> getOutputPorts() {
        // TODO Auto-generated method stub
        return this.outputs;
    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#createNode(org.apache.airavata.xbaya.graph.Graph)
     */
    @Override
    public Node createNode(Graph graph) {
        Node node = super.createNode(graph);

        node.setName(workflow.getName());
        Collection<? extends Port> allPorts = node.getAllPorts();
        for (Port port : allPorts) {
            ((GenericSubworkflowPort) port).setID(port.getComponentPort().getName());
            port.setName(port.getComponentPort().getName());
        }
        return node;
    }

    /**
     * @see org.apache.airavata.xbaya.component.Component#toHTML()
     */
    @Override
    public String toHTML() {
        // TODO Auto-generated method stub
        return "";
    }

}