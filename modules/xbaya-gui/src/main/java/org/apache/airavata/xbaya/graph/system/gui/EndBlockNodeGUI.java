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

package org.apache.airavata.xbaya.graph.system.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.List;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.gui.PortGUI;
import org.apache.airavata.xbaya.graph.system.EndBlockNode;

public class EndBlockNodeGUI extends ConfigurableNodeGUI {

    // private static final MLogger logger = MLogger.getLogger();

    private EndBlockConfigurationDialog configurationWindow;

    private EndBlockNode node;

    private Polygon polygon;

    /**
     * @param node
     */
    public EndBlockNodeGUI(EndBlockNode node) {
        super(node);
        this.node = node;
        this.polygon = new Polygon();
    }

    /**
     * Shows a configuration window when a user click the configuration area.
     * 
     * @param engine
     */
    @Override
    protected void showConfigurationDialog(XBayaEngine engine) {
        if (this.configurationWindow == null) {
            this.configurationWindow = new EndBlockConfigurationDialog(this.node, engine);
        }
        this.configurationWindow.show();
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.NodeGUI#calculatePositions(java.awt.Graphics)
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
            port.getGUI().paint(g);
        }

        paintConfiguration(g);
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.NodeGUI#setPortPositions()
     */
    @Override
    protected void setPortPositions() {
        // inputs
        List<? extends Port> inputPorts = this.node.getInputPorts();
        for (int i = 0; i < inputPorts.size(); i++) {
            Port port = inputPorts.get(i);
            Point offset;
            if (i < inputPorts.size() / 2) {
                offset = new Point(PortGUI.DATA_PORT_SIZE / 2, this.headHeight + PORT_INITIAL_GAP + PORT_GAP * i);
            } else {
                offset = new Point(PortGUI.DATA_PORT_SIZE / 2, this.headHeight + PORT_INITIAL_GAP + PORT_GAP * (i + 1));
            }
            port.getGUI().setOffset(offset);
        }

        // outputs
        List<? extends Port> outputPorts = this.node.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            Point offset = new Point(this.getBounds().width - PortGUI.DATA_PORT_SIZE / 2, (int) (this.headHeight
                    + PORT_INITIAL_GAP + PORT_GAP * (outputPorts.size() / 2.0 + i)));
            port.getGUI().setOffset(offset);
        }

        // control-in
        Port controlInPort = this.node.getControlInPort();
        if (controlInPort != null) {
            controlInPort.getGUI().setOffset(new Point(0, 0));
        }

        // control-out
        for (Port controlOutPort : this.node.getControlOutPorts()) {
            controlOutPort.getGUI().setOffset(new Point(getBounds().width, getBounds().height - this.headHeight / 2));
            break; // Has only one
        }

    }

    private void calculatePositions() {
        this.polygon.reset();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y);
        this.polygon.addPoint(position.x, position.y + this.dimension.height + this.headHeight / 2);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.headHeight / 2);
    }
}