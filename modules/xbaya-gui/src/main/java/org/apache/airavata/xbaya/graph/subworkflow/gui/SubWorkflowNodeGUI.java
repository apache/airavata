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

package org.apache.airavata.xbaya.graph.subworkflow.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.gui.NodeGUI;
import org.apache.airavata.xbaya.graph.subworkflow.SubWorkflowNode;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubWorkflowNodeGUI extends NodeGUI {

    private final static Logger logger = LoggerFactory.getLogger(SubWorkflowNodeGUI.class);

    private SubWorkflowNode node;

    private static final Color HEAD_COLOR = new Color(138, 43, 226);

    /**
     * Constructs a SubWorkflowNodeGUI.
     * 
     * @param node
     */
    public SubWorkflowNodeGUI(SubWorkflowNode node) {
        super(node);
        this.node = node;
        this.setHeadColor(HEAD_COLOR);
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        logger.info(event.toString());
        if (event.getClickCount() >= 2) {
            openWorkflowTab(engine);
        }
    }

    protected void setSelectedFlag(boolean flag) {
        this.selected = flag;
        if (this.selected) {
            this.headColor = SELECTED_HEAD_COLOR;
        } else {
            this.headColor = HEAD_COLOR;
        }
    }

    public void openWorkflowTab(XBayaEngine engine) {
        WorkflowClient workflowClient = engine.getWorkflowClient();
        try {
            Workflow workflow = this.node.getComponent().getWorkflow(workflowClient);
            engine.getGUI().selectOrCreateGraphCanvas(workflow);
        } catch (GraphException e) {
            engine.getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
        } catch (WorkflowEngineException e) {
            engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
        } catch (ComponentException e) {
            engine.getErrorWindow().error(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }
    }

}