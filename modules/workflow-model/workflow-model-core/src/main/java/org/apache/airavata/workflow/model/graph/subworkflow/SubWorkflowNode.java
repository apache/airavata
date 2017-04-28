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
package org.apache.airavata.workflow.model.graph.subworkflow;

import java.util.List;

import org.apache.airavata.workflow.model.component.system.SubWorkflowComponent;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.util.GraphUtil;
import org.apache.airavata.workflow.model.wf.Workflow;

public class SubWorkflowNode extends NodeImpl {

    private Workflow workflow;

    /**
     * Constructs a SubWorkflowNode.
     * 
     * @param graph
     */
    public SubWorkflowNode(Graph graph) {
        super(graph);
    }


    /**
     * @see org.apache.airavata.workflow.model.graph.ws.WSNode#getComponent()
     */
    @Override
    public SubWorkflowComponent getComponent() {
        return (SubWorkflowComponent) super.getComponent();
    }

    /**
     * @throws GraphException
     * @see org.apache.airavata.workflow.model.graph.impl.NodeImpl#edgeWasAdded(org.apache.airavata.workflow.model.graph.Edge)
     */
    @Override
    protected void edgeWasAdded(Edge edge) throws GraphException {
        GraphUtil.validateConnection(edge);
    }

    /**
     * @param workflow
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public Workflow getWorkflow() {
        return this.workflow;
    }

     public DataPort assignInputPortID(String id, int index) {
        List<DataPort> ports = this.getInputPorts();
        for (DataPort d : ports) {
            if (null == d.getID()) {
                d.setID(id);
            }
        }

        return ports.get(index);
    }

    public DataPort assignOutputPortID(String id, int index) {
        List<DataPort> ports = this.getOutputPorts();
        for (DataPort d : ports) {
            if (null == d.getID()) {
                d.setID(id);
            }
        }

        return ports.get(index);
    }
    
}