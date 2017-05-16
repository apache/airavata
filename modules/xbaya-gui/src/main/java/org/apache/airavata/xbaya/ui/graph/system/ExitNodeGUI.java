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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.system.ExitNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.graph.Paintable;

public class ExitNodeGUI extends NodeGUI {

    public final static Color c_LIGHTER = new Color(255, 255, 255);
    public final static Color c_DARKER = new Color(251, 103, 87);

    /**
     * Constructs a BPELExitNodeGUI.
     * 
     * @param node
     */
    public ExitNodeGUI(ExitNode node) {
        super(node);
        node.setName(" Exit");
    }

    @Override
    protected void paint(Graphics2D g2) {

        Point position = this.node.getPosition();

        Graphics2D g = (Graphics2D) g2.create();
        GradientPaint gp = new GradientPaint(position.x, position.y, c_LIGHTER, (position.x + this.dimension.height),
                (position.y + this.dimension.height), c_DARKER);
        // Draws the body.

        if (this.dragged) {
            g.setColor(DRAGGED_BODY_COLOR);
        } else {
            g.setColor(this.bodyColor);
        }
        Ellipse2D.Double bodyShape = new Ellipse2D.Double(position.x + 2, position.y, this.dimension.height, this.dimension.height);
        drawHeader(g, bodyShape, node.getName(), c_DARKER, false);
//        g.setPaint(gp);
//        g.fillOval(position.x + 2, position.y, this.dimension.height, this.dimension.height);
//        g.setColor(Color.black);
////        g.setStroke(new BasicStroke(1.2f));
//        g.drawOval(position.x + 2, position.y, this.dimension.height, this.dimension.height);
        // Text
//        g.setColor(TEXT_COLOR);

        // XXX it's debatable if we should show the ID or the name.
//        String name = this.node.getName(); // + this.node.getID();
//        g.drawString(name, position.x + TEXT_GAP_X, position.y + this.headHeight - TEXT_GAP_Y + 2);

        // Edge
        drawEdge(g, bodyShape.getBounds2D(),EDGE_COLOR.brighter());
        drawEdge(g, bodyShape, EDGE_COLOR);
        
//        g.setColor(EDGE_COLOR);
//        // Comment of dont want circle in rectangle
//        g.drawRect(position.x, position.y, this.dimension.height + 2, this.dimension.height);

        // Paint all ports
        drawPorts(g, node);

        // Paint extras
        for (Paintable paintable : this.paintables) {
            paintable.paint(g, this.node.getPosition());
        }
    }

}