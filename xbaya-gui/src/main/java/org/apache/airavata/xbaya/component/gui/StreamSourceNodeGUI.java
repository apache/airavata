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

package org.apache.airavata.xbaya.component.gui;

import java.awt.Color;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.system.gui.ConfigurableNodeGUI;
import org.apache.airavata.xbaya.graph.system.gui.StreamSourceConfigurationDialog;
import org.apache.airavata.xbaya.graph.system.gui.StreamSourceNode;

public class StreamSourceNodeGUI extends ConfigurableNodeGUI {

    private static Color HEAD_COLOR = Color.BLUE;

    private static final String CONFIG_AREA_STRING = "Config";

    private StreamSourceNode inputNode;

    private StreamSourceConfigurationDialog configurationWindow;

    /**
     * @param node
     */
    public StreamSourceNodeGUI(StreamSourceNode node) {
        super(node);

        this.inputNode = node;
        setConfigurationText(CONFIG_AREA_STRING);
        headColor = HEAD_COLOR;
    }

    /**
     * Shows a configuration window when a user click the configuration area.
     * 
     * @param engine
     */
    @Override
    protected void showConfigurationDialog(XBayaEngine engine) {
        if (this.configurationWindow == null) {
            this.configurationWindow = new StreamSourceConfigurationDialog(this.inputNode, engine);
        }
        this.configurationWindow.show();
    }

    // protected void paint(Graphics2D g) {
    //
    // Point position = this.node.getPosition();
    //
    // // Draws the body.
    // if (this.dragged) {
    // g.setColor(DRAGGED_BODY_COLOR);
    // } else {
    // g.setColor(this.bodyColor);
    // }
    // g.fillRect(position.x, position.y, this.dimension.width,
    // this.dimension.height);
    //
    // // Draws the head.
    // g.setColor(this.headColor);
    // g.fillRect(position.x, position.y, this.dimension.width,
    // this.headHeight);
    // //Draw a small circle to indicate the break
    // if(node.isBreak() ){
    // g.setColor(BREAK_POINT_COLOR);
    // int r = this.headHeight/4;
    // g.fillOval( position.x+this.dimension.width - 3*r, position.y +r, 2*r, 2*r);
    // g.setColor(BREAK_POINT_BORDER_COLOR);
    // g.drawOval( position.x+this.dimension.width - 3*r, position.y +r, 2*r, 2*r);
    // }
    //
    // // Text
    // g.setColor(TEXT_COLOR);
    //
    // // XXX it's debatable if we should show the ID or the name.
    // // String name = this.node.getName();
    // String name = this.node.getID();
    // g.drawString(name, position.x + TEXT_GAP_X, position.y
    // + this.headHeight - TEXT_GAP_Y);
    //
    // // Edge
    // g.setColor(EDGE_COLOR);
    // g.drawRect(position.x, position.y, this.dimension.width,
    // this.dimension.height);
    //
    // // Paint all ports
    // for (Port port : this.node.getAllPorts()) {
    // port.getGUI().paint(g);
    // }
    //
    // // Paint extras
    // for (Paintable paintable : this.paintables) {
    // paintable.paint(g, this.node.getPosition());
    // }
    //
    //
    //
    // }

    protected void setSelectedFlag(boolean flag) {
        this.selected = flag;
        if (this.selected) {
            this.headColor = SELECTED_HEAD_COLOR;
        } else {
            this.headColor = HEAD_COLOR;
        }
    }

    // protected void calculatePositions(Graphics g) {
    // FontMetrics fm = g.getFontMetrics();
    // this.headHeight = fm.getHeight() + TEXT_GAP_Y * 2;
    //
    // this.dimension.height = MINIMUM_HEIGHT*3;
    // this.dimension.width = MINIMUM_WIDTH*2;
    //
    // }

}