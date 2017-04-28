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
package org.apache.airavata.xbaya.ui.graph.ws;

import java.awt.event.MouseEvent;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.ws.WorkflowNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowNodeGUI extends NodeGUI {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowNodeGUI.class);

    private WorkflowNode node;

    /**
     * Creates a WsNodeGui
     * 
     * @param node
     */
    public WorkflowNodeGUI(WorkflowNode node) {
        super(node);
        this.node = node;
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        logger.debug(event.toString());
        if (event.getClickCount() >= 2) {
            openWorkflowTab(engine);
        }
    }

    public void openWorkflowTab(XBayaEngine engine) {
        try {
            Workflow workflow = this.node.getComponent().getWorkflow();
            engine.getGUI().selectOrCreateGraphCanvas(workflow);
        } catch (GraphException e) {
            engine.getGUI().getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
        } catch (ComponentException e) {
            engine.getGUI().getErrorWindow().error(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }
    }
}