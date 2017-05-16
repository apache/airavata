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
package org.apache.airavata.workflow.model.component.system;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ws.WSComponent;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.subworkflow.SubWorkflowNode;
//import org.apache.airavata.workflow.model.ode.ODEClient;
import org.apache.airavata.workflow.model.wf.Workflow;

public class SubWorkflowComponent extends WSComponent {

    private Workflow workflow;

    private SubWorkflowComponent(Workflow workflow) throws ComponentException {
//        super(workflow.getWorkflowWSDL());
        this.workflow = workflow;

    }

    public static SubWorkflowComponent getInstance(Workflow workflow) throws ComponentException {
//        new ODEClient().getInputs(workflow);
        return new SubWorkflowComponent(workflow);
    }

    /**
     * @param workflowClient
     * @return The workflow
     * @throws ComponentException
     * @throws WorkflowEngineException
     * @throws GraphException
     */
    public Workflow getWorkflow() throws GraphException, ComponentException {
        return this.workflow;
    }

    /**
     * @see org.apache.airavata.workflow.model.component.ws.WSComponent#createNode(org.apache.airavata.workflow.model.graph.Graph)
     */
    @Override
    public SubWorkflowNode createNode(Graph graph) {
        SubWorkflowNode node = new SubWorkflowNode(graph);
        node.setWorkflow(workflow.clone());

        // Copy some infomation from the component

        node.setName(getName());
        node.setComponent(this);
        // node.setWSDLQName(this.wsdlQName);

        // Creates a unique ID for the node. This has to be after setName().
        node.createID();

        // Creat ports
        createPorts(node);

        return node;
    }

}