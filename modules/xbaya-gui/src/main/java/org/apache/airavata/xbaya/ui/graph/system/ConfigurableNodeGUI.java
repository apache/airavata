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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;

public abstract class ConfigurableNodeGUI extends NodeGUI {

    protected static final Color CONFIG_AREA_COLOR = new Color(220, 220, 220);

    protected static final String DEFAULT_CONFIG_AREA_TEXT = "Config";

    protected static final int CONFIG_AREA_GAP_X = 20;

    protected String configurationText;

    protected Rectangle configurationArea;

    /**
     * @param node
     */
    public ConfigurableNodeGUI(NodeImpl node) {
        super(node);
        this.configurationText = DEFAULT_CONFIG_AREA_TEXT;
        this.configurationArea = new Rectangle();

    }

    /**
     * Sets the text shown on the configuration area.
     * 
     * @param text
     *            The text to set
     */
    public void setConfigurationText(String text) {
        this.configurationText = text;
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        if (isInConfig(event.getPoint())) {
            showConfigurationDialog(engine);
        }
    }

    /**
     * @param engine
     */
    protected abstract void showConfigurationDialog(XBayaEngine engine);

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

    @Override
    protected void calculatePositions(Graphics g) {
        super.calculatePositions(g);

        Point position = this.node.getPosition();
        FontMetrics fm = g.getFontMetrics();

        this.configurationArea.height = fm.getHeight() + TEXT_GAP_Y * 2;
        this.configurationArea.width = this.dimension.width - CONFIG_AREA_GAP_X * 2;
        this.configurationArea.x = position.x + CONFIG_AREA_GAP_X;
        this.configurationArea.y = position.y + this.headHeight
                + (this.dimension.height - this.headHeight - this.configurationArea.height) / 2;

    }

    /**
     * Paints the config area
     * 
     * @param g
     */
    @Override
    protected void paint(Graphics2D g) {
        super.paint(g);
        paintConfiguration(g);
    }

    /**
     * @param g
     */
    protected void paintConfiguration(Graphics2D g) {
        g.setColor(CONFIG_AREA_COLOR);
        g.fill(this.configurationArea);
        g.setColor(TEXT_COLOR);
        g.drawString(this.configurationText, this.configurationArea.x + TEXT_GAP_X, this.configurationArea.y
                + this.configurationArea.height - TEXT_GAP_Y);
    }
}