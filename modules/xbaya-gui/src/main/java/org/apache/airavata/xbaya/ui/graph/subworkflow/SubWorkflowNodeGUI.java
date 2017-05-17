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
package org.apache.airavata.xbaya.ui.graph.subworkflow;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.subworkflow.SubWorkflowNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
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
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        logger.debug(event.toString());
        if (event.getClickCount() >= 2) {
            openWorkflowTab(engine.getGUI());
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

    public void openWorkflowTab(XBayaGUI xbayaGUI) {
        try {
            Workflow workflow = this.node.getComponent().getWorkflow();
            xbayaGUI.selectOrCreateGraphCanvas(workflow);
        } catch (GraphException e) {
        	xbayaGUI.getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
        } catch (ComponentException e) {
        	xbayaGUI.getErrorWindow().error(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
        }
    }

}