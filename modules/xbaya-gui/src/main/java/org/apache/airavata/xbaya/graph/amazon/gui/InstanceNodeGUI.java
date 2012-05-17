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

package org.apache.airavata.xbaya.graph.amazon.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.List;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.amazon.InstanceNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.graph.gui.PortGUI;
import org.apache.airavata.xbaya.graph.system.gui.ConfigurableNodeGUI;

public class InstanceNodeGUI extends ConfigurableNodeGUI {

    private InstanceNode node;

    private Polygon polygon;

    private InstanceConfigurationDialog configDialog;

    /**
     * Constructs a InstanceNodeGUI.
     * 
     * @param node
     */
    public InstanceNodeGUI(InstanceNode node) {
        super(node);
        this.node = node;
        this.polygon = new Polygon();
    }

    /**
     * @see org.apache.airavata.xbaya.graph.system.gui.ConfigurableNodeGUI#showConfigurationDialog(org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    protected void showConfigurationDialog(XBayaEngine engine) {
        if (this.configDialog == null) {
            this.configDialog = new InstanceConfigurationDialog(this.node, engine);
        }
        this.configDialog.show();
    }

    /**
     * @see org.apache.airavata.xbaya.graph.system.gui.ConfigurableNodeGUI#calculatePositions(java.awt.Graphics)
     */
    @Override
    protected void calculatePositions(Graphics g) {
        super.calculatePositions(g);
        calculatePositions();
        setPortPositions();
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.NodeGUI#getBounds()
     */
    @Override
    protected Rectangle getBounds() {
        return this.polygon.getBounds();
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.NodeGUI#isIn(java.awt.Point)
     */
    @Override
    protected boolean isIn(Point point) {
        return this.polygon.contains(point);
    }

    /**
     * @see org.apache.airavata.xbaya.graph.system.gui.ConfigurableNodeGUI#paint(java.awt.Graphics2D)
     */
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
        g.fillRect(position.x, position.y, this.dimension.width, this.headHeight);

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

    /**
     * Sets up the position of ports
     */
    @Override
    protected void setPortPositions() {
        // inputs
        List<? extends Port> inputPorts = this.node.getInputPorts();
        for (int i = 0; i < inputPorts.size(); i++) {
            Port port = inputPorts.get(i);
            Point offset = new Point(PortGUI.DATA_PORT_SIZE / 2, this.headHeight + PORT_INITIAL_GAP + PORT_GAP * i);
            NodeController.getGUI(port).setOffset(offset);
        }

        // outputs
        List<? extends Port> outputPorts = this.node.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            // Use getBounds() instead of this.dimension because subclass might
            // overwrite getBounds() to have different shape.
            Point offset = new Point(this.getBounds().width - PortGUI.DATA_PORT_SIZE / 2, this.headHeight
                    + PORT_INITIAL_GAP + PORT_GAP * i);
            NodeController.getGUI(port).setOffset(offset);
        }

        // control out port
        List<? extends Port> controlOutPorts = this.node.getControlOutPorts();
        Port controlOutPort1 = controlOutPorts.get(0);
        Point offset = new Point(getBounds().width / 2, getBounds().height);
        NodeController.getGUI(controlOutPort1).setOffset(offset);
    }

    private void calculatePositions() {
        // Avoid instantiating a new polygon each time.
        this.polygon.reset();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y);
        this.polygon.addPoint(position.x, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width / 2, position.y + this.dimension.height
                + this.headHeight);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width, position.y);
    }
}