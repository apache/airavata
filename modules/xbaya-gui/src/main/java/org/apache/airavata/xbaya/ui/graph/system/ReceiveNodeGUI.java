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

package org.apache.airavata.xbaya.ui.graph.system;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.system.ReceiveNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.ReceiveConfigurationDialog;

public class ReceiveNodeGUI extends ConfigurableNodeGUI {

    private ReceiveNode node;

    private Polygon polygon;

    private ReceiveConfigurationDialog configurationDialog;

    /**
     * @param node
     */
    public ReceiveNodeGUI(ReceiveNode node) {
        super(node);
        this.node = node;
        this.polygon = new Polygon(); // To avoid null check.
    }

    /**
     * Shows a configuration window when a user click the configuration area.
     * 
     * @param engine
     */
    @Override
    protected void showConfigurationDialog(XBayaGUI xbayaGUI) {
        if (this.configurationDialog == null) {
            this.configurationDialog = new ReceiveConfigurationDialog(this.node, xbayaGUI);
        }
        this.configurationDialog.show();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#calculatePositions(java.awt.Graphics)
     */
    @Override
    protected void calculatePositions(Graphics g) {
        super.calculatePositions(g);
        calculatePositions();
        setPortPositions();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#getBounds()
     */
    @Override
    protected Rectangle getBounds() {
        return this.polygon.getBounds();
    }

    @Override
    protected boolean isIn(Point point) {
        return this.polygon.contains(point);
    }

    @Override
    protected void paint(Graphics2D g) {
        Point position = getPosition();

        // Draws the body.
        if (this.dragged) {
            g.setColor(DRAGGED_BODY_COLOR);
        } else {
            g.setColor(this.bodyColor);
        }
        g.fillPolygon(this.polygon);

        // Draws the head.
        g.setColor(this.headColor);
        Polygon head = new Polygon();
        head.addPoint(position.x, position.y);
        head.addPoint(position.x, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y + this.headHeight / 2);
        g.fill(head);

        // Text
        g.setColor(TEXT_COLOR);
        String name = this.node.getName();
        g.drawString(name, position.x + TEXT_GAP_X, position.y + this.headHeight - TEXT_GAP_Y);

        // Edge
        g.setColor(EDGE_COLOR);
        g.drawPolygon(this.polygon);

        // Paint all ports
        for (Port port : this.node.getAllPorts()) {
            NodeController.getGUI(port).paint(g);
        }

        paintConfiguration(g);
    }

    private void calculatePositions() {
        this.polygon = new Polygon();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y);
        this.polygon.addPoint(position.x, position.y + this.dimension.height + this.headHeight / 2);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.headHeight / 2);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#setPortPositions()
     */
    @Override
    protected void setPortPositions() {
        super.setPortPositions();

        for (Port controlOutPort : this.node.getControlOutPorts()) {
        	NodeController.getGUI(controlOutPort).setOffset(new Point(getBounds().width, getBounds().height - this.headHeight / 2));
            break; // Has only one
        }

        Port port = this.node.getEPRPort();
        NodeController.getGUI(port).setOffset(new Point(getBounds().width / 2, this.headHeight / 4));
    }

}