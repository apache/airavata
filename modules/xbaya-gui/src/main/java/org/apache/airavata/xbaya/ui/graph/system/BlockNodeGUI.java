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
package org.apache.airavata.xbaya.ui.graph.system;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.system.BlockNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;

public class BlockNodeGUI extends NodeGUI {

    private static final int HEIGHT = 100;

    private BlockNode node;

    /**
     * @param node
     */
    public BlockNodeGUI(BlockNode node) {
        super(node);
        this.node = node;
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#calculatePositions(java.awt.Graphics)
     */
    @Override
    protected void calculatePositions(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        this.headHeight = fm.getHeight() + TEXT_GAP_Y * 2;

        this.dimension.height = this.headHeight + PORT_INITIAL_GAP + HEIGHT;
        this.dimension.width = fm.stringWidth(this.node.getID() + TEXT_GAP_X * 2);

        /* Calculates the position of ports */
        setPortPositions();
    }

    /**
     * Sets up the position of ports
     */
    @Override
    protected void setPortPositions() {
        // No input ports

        Port controlInPort = this.node.getControlInPort();
        if (controlInPort != null) {
        	NodeController.getGUI(controlInPort).setOffset(new Point(0, 0));
        }

        // There are two controlOutPorts.
        List<? extends Port> controlOutPorts = this.node.getControlOutPorts();
        Port controlOutPort1 = controlOutPorts.get(0);
        Point offset = new Point(getBounds().width, +getBounds().height / 2);
        NodeController.getGUI(controlOutPort1).setOffset(offset);

        Port controlOutPort2 = controlOutPorts.get(1);
        offset = new Point(this.getBounds().width, getBounds().height);
        NodeController.getGUI(controlOutPort2).setOffset(offset);

        // No outputs
    }
}