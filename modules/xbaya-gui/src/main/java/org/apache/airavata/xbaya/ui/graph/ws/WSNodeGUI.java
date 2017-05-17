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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.apache.airavata.workflow.model.graph.ws.WSNode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.messaging.Monitor;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.ws.ServiceInteractionWindow;
import org.apache.airavata.xbaya.ui.dialogs.graph.ws.WSNodeWindow;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler.NodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSNodeGUI extends NodeGUI {

    private final static Logger logger = LoggerFactory.getLogger(WSNodeGUI.class);

    private WSNode node;

    private WSNodeWindow window;

    protected static final Color CONFIG_AREA_COLOR = new Color(220, 220, 220);

    protected static final String DEFAULT_CONFIG_AREA_TEXT = "Interact";

    protected static final int CONFIG_AREA_GAP_X = 20;

    protected String configurationText = DEFAULT_CONFIG_AREA_TEXT;

    protected Rectangle configurationArea;

    private boolean interactiveMode;

    /**
     * Creates a WsNodeGui
     * 
     * @param node
     */
    public WSNodeGUI(WSNode node) {
        super(node);
        this.node = node;
        this.configurationArea = new Rectangle();
    }

    private void showWindow(XBayaEngine engine) {
        if (this.window == null) {
            this.window = new WSNodeWindow(engine, this.node);
        }
        this.window.show();
    }

    protected void showConfigurationDialog(XBayaGUI xbayaGUI, Monitor monitor) {
        new ServiceInteractionWindow(xbayaGUI, this.node.getID(),monitor).show();

    }

    protected void calculatePositions(Graphics g) {
        super.calculatePositions(g);

        Point position = this.node.getPosition();
        FontMetrics fm = g.getFontMetrics();

        if (this.interactiveMode && isInteractable()) {
            this.configurationArea.height = fm.getHeight() + TEXT_GAP_Y * 2;
            // it only need to say interact and the rest of the are should be
            // available for double clicking
            this.configurationArea.width = 50;
            this.configurationArea.x = position.x + CONFIG_AREA_GAP_X;
            this.configurationArea.y = position.y + this.headHeight
                    + (this.dimension.height - this.headHeight - this.configurationArea.height) / 2;
        } else {
            this.configurationArea.height = 0;
            this.configurationArea.width = 0;
        }

    }

    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        if (event.getClickCount() >= 2) {
            showWindow(engine);
        } else if (this.interactiveMode && (isInteractable()) && isInConfig(event.getPoint())) {
            showConfigurationDialog(engine.getGUI(),engine.getMonitor());
        }
    }

    private boolean isInteractable() {
        return this.bodyColor == NodeState.EXECUTING.color || this.bodyColor == NodeState.FAILED.color
                || this.bodyColor == NodeState.FINISHED.color;
    }

    /**
     * Paints the config area
     * 
     * @param g
     */
    @Override
    protected void paint(Graphics2D g) {
        super.paint(g);
        if (isInteractable()) {
            paintConfiguration(g);
        }
    }

    protected void paintConfiguration(Graphics2D g) {
        g.setColor(CONFIG_AREA_COLOR);
        g.fill(this.configurationArea);
        g.setColor(TEXT_COLOR);
        g.drawString(this.configurationText, this.configurationArea.x + TEXT_GAP_X, this.configurationArea.y
                + this.configurationArea.height - TEXT_GAP_Y);
    }

    /**
     * Checks if a user's click is to select the configuration
     * 
     * @param point
     * @return true if the user's click is to select the node, false otherwise
     */
    @Override
    protected boolean isInConfig(Point point) {
        return this.configurationArea.contains(point);
    }

    public void setInteractiveMode(boolean mode) {
        this.interactiveMode = mode;
    }

}