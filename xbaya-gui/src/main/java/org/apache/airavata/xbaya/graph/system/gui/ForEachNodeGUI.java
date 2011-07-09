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

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.graph.impl.PortImpl;
import org.apache.airavata.xbaya.graph.system.ForEachNode;

public class ForEachNodeGUI extends ConfigurableNodeGUI {

    // private static final MLogger logger = MLogger.getLogger();

    private static final String CONFIG_AREA_STRING = "Config";

    private ForEachNode node;

    private ForEachConfigurationDialog configurationWindow;

    private Polygon polygon;

    /**
     * @param node
     */
    public ForEachNodeGUI(ForEachNode node) {
        super(node);
        this.node = node;
        setConfigurationText(CONFIG_AREA_STRING);
        this.polygon = new Polygon(); // To avoid null check.
    }

    /**
     * Shows a configuration window when a user click the configuration area.
     * 
     * @param engine
     */
    @Override
    protected void showConfigurationDialog(XBayaEngine engine) {
        if (this.configurationWindow == null) {
            this.configurationWindow = new ForEachConfigurationDialog(this.node, engine);
        }
        this.configurationWindow.show();
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
        Polygon head = new Polygon();
        head.addPoint(position.x, position.y + this.headHeight / 2);
        head.addPoint(position.x, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y);
        g.fill(head);

        g.setColor(TEXT_COLOR);
        g.setColor(TEXT_COLOR);
        String name = this.node.getName();
        g.drawString(name, position.x + this.dimension.width / 3 + TEXT_GAP_X, position.y + this.headHeight
                - TEXT_GAP_Y);

        // Edge
        g.setColor(EDGE_COLOR);
        g.drawPolygon(this.polygon);

        // Paint all ports
        for (Port port : this.node.getAllPorts()) {
            port.getGUI().paint(g);
        }

        paintConfiguration(g);
    }

    private void calculatePositions() {
        // XXX Avoid instantiating a new polygon each time.
        this.polygon = new Polygon();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y + this.headHeight / 2);
        this.polygon.addPoint(position.x, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height + this.headHeight
                - this.headHeight / 2);
        this.polygon.addPoint(position.x + this.dimension.width, position.y);
    }

    /**
     * Sets up the position of ports
     */
    @Override
    protected void setPortPositions() {
        super.setPortPositions();

        PortImpl controlInPort = this.node.getControlInPort();
        if (controlInPort != null) {
            Point off = new Point(0, this.headHeight / 2);
            controlInPort.getGUI().setOffset(off);
        }
    }
}