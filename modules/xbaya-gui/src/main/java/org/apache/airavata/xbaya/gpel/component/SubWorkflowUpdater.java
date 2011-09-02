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

package org.apache.airavata.xbaya.gpel.component;

import java.awt.Point;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WorkflowComponent;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.graph.ws.WorkflowNode;
import org.apache.airavata.xbaya.wf.Workflow;

public class SubWorkflowUpdater {

    private XBayaEngine engine;

    /**
     * Constructs a SubWorkflowUpdater.
     * 
     * @param engine
     */
    public SubWorkflowUpdater(XBayaEngine engine) {
        this.engine = engine;
    }

    /**
     * @param workflow
     * @throws ComponentException
     * @throws GraphException
     */
    public void update(Workflow workflow) throws ComponentException, GraphException {

        WorkflowComponent newComponent = new WorkflowComponent(workflow);
        URI newTemplateID = newComponent.getTemplateID();

        List<GraphCanvas> graphCanvases = this.engine.getGUI().getGraphCanvases();
        for (GraphCanvas graphCanvas : graphCanvases) {
            Graph graph = graphCanvas.getGraph();
            List<WorkflowNode> updatingNodes = new LinkedList<WorkflowNode>();
            for (Node node : graph.getNodes()) {
                if (node instanceof WorkflowNode) {
                    WorkflowNode workflowNode = (WorkflowNode) node;
                    WorkflowComponent workflowComponent = workflowNode.getComponent();
                    URI templateID = workflowComponent.getTemplateID();
                    if (templateID.equals(newTemplateID)) {
                        updatingNodes.add(workflowNode);
                    }
                }
            }
            for (WorkflowNode node : updatingNodes) {
                Point position = node.getPosition();
                graph.removeNode(node);

                WorkflowNode newNode = newComponent.createNode(graph);
                newNode.setPosition(position);
            }
        }
    }

}