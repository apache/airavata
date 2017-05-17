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
package org.apache.airavata.xbaya.ui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;

import org.apache.airavata.workflow.model.graph.ControlEdge;
import org.apache.airavata.workflow.model.graph.Edge;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class EdgeGUI implements GraphPieceGUI {

    /**
     * CONTROL_EDGE_STROKE
     */
    public static final Stroke CONTROL_EDGE_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f);

    public static final Stroke STREAM_EDGE_STROKE = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f);

    private static final Color lineColor = Color.black;

    private static final Color pointColor = Color.pink;

    private static final Color selectedPointColor = Color.red;

    private static final int POINT_SIZE = 8;

    private Edge edge;

    private boolean selected = false;

    private static Color STREAM_EDGE_COLOR = new Color(51, 255, 204);

    /**
     * @param edge
     * 
     */
    public EdgeGUI(Edge edge) {
        this.edge = edge;
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        // Nothing
    }

    /**
     * @param bool
     */
    protected void setSelectedFlag(boolean bool) {
        this.selected = bool;
    }

    /**
     * @return the middle point of the edge
     */
    protected Point getMiddlePosition() {
        Point point1 = getFromPosition();
        Point point2 = getToPosition();

        Point midPoint = new Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2);
        return midPoint;
    }

    /**
     * @param g
     */
    protected void paint(Graphics2D g) {
    	DrawUtils.initializeGraphics2D(g);
        Point point1 = getFromPosition();
        Point point2 = getToPosition();

            g.setColor(lineColor);

        Stroke originalStroke = g.getStroke();
        if (this.edge instanceof ControlEdge) {
            g.setStroke(EdgeGUI.CONTROL_EDGE_STROKE);
        } 
        paintLine(point1, point2, g);
        g.setStroke(originalStroke);

        g.setColor(this.selected ? pointColor : selectedPointColor);

        Point midPoint = getMiddlePosition();
        g.fillArc(midPoint.x - POINT_SIZE / 2, midPoint.y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE, 0, 360);
    }


    protected static void paintLine(Point point1, Point point2, Graphics2D g) {
        int d = 100;
        int dist = (int) point1.distance(point2);
        if (dist < d) {
            d = dist;
        }
        CubicCurve2D line = new CubicCurve2D.Double(point1.x, point1.y, point1.x + d, point1.y, point2.x - d, point2.y,
                point2.x, point2.y);
        g.draw(line);
    }

    private Point getFromPosition() {
        Port port = this.edge.getFromPort();
        return NodeController.getGUI(port).getPosition();
    }

    private Point getToPosition() {
        Port port = this.edge.getToPort();
        return NodeController.getGUI(port).getPosition();
    }
}