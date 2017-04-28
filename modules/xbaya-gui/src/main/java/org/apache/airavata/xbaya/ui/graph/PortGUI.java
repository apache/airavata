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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.Port.Kind;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class PortGUI implements GraphPieceGUI {

    /**
     * The size of the port (diameter of the triangle)
     */
    public static final int DATA_PORT_SIZE = 10;

    /**
     * CONTROL_PORT_SIZE
     */
    public static final int CONTROL_PORT_SIZE = 6;

    private static final Color DATA_IN_COLOR = Color.BLUE;

    private static final Color DATA_OUT_COLOR = Color.GREEN;

    private static final Color CONTROL_IN_COLOR = Color.RED;

    private static final Color CONTROL_OUT_COLOR = Color.RED;

    private static final Color EPR_COLOR = Color.GREEN;

    private static final Color SELECTED_COLOR = Color.PINK;

    protected static final Color TEXT_COLOR = Color.black;

    private static final int TOKEN_SIZE = 22;

    private static final Color TOKEN_COLOR = Color.GREEN;

    private List<String> tokens = new LinkedList<String>();

    private Port port;
    
    private String portText=null;

    /**
     * The position of this port relative to the node this port belongs to.
     */
    private Point offset;

    private boolean selected = false;

    /**
     * @param port
     */
    public PortGUI(Port port) {
        this.port = port;
        this.offset = new Point(); // To avoid null check.
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        // Nothing
    }

    /**
     * @param g
     */
    public void paint(Graphics2D g) {

        Kind kind = this.port.getKind();
        Color color = null;
        switch (kind) {
        case DATA_IN:
            color = this.selected ? SELECTED_COLOR : DATA_IN_COLOR;
            break;
        case DATA_OUT:
            color = this.selected ? SELECTED_COLOR : DATA_OUT_COLOR;
            break;
        case CONTROL_IN:
            color = this.selected ? SELECTED_COLOR : CONTROL_IN_COLOR;
            break;
        case CONTROL_OUT:
            color = this.selected ? SELECTED_COLOR : CONTROL_OUT_COLOR;
            break;
        case EPR:
            color = this.selected ? SELECTED_COLOR : EPR_COLOR;
            break;
        }

        Point point = getPosition();
        Shape shape = null;
        switch (kind) {
        case DATA_IN:

            shape = drawPortArrow(point);
            int count = 0;
            String[] tokenArray = new String[this.tokens.size()];
            this.tokens.toArray(tokenArray);
            for (String token : tokenArray) {
                g.setColor(TOKEN_COLOR);
                g.fill(new Ellipse2D.Double(point.x + TOKEN_SIZE /* +count*5 */, point.y + TOKEN_SIZE * count,
                        TOKEN_SIZE, TOKEN_SIZE / 2));
                g.setColor(TEXT_COLOR);
                g.drawString(token, point.x + TOKEN_SIZE * 3 /* +count*5 */, point.y + TOKEN_SIZE * count);

                ++count;
            }

            break;
        case DATA_OUT:
            shape = drawPortArrow(point);
            count = 0;
            tokenArray = new String[this.tokens.size()];
            this.tokens.toArray(tokenArray);
            for (String token : tokenArray) {
                g.setColor(TOKEN_COLOR);
                g.fill(new Ellipse2D.Double(point.x + 5 /* +count*5 */, point.y + TOKEN_SIZE * count, TOKEN_SIZE,
                        TOKEN_SIZE / 2));
                g.setColor(TEXT_COLOR);
                g.drawString(token, point.x + TOKEN_SIZE + 10 /* +count*0 */, point.y + TOKEN_SIZE * count);

                ++count;
            }
            break;
        case CONTROL_IN:
        case CONTROL_OUT:
            shape = new Ellipse2D.Double(point.x - CONTROL_PORT_SIZE / 2, point.y - CONTROL_PORT_SIZE / 2,
                    CONTROL_PORT_SIZE, CONTROL_PORT_SIZE);
            break;
        case EPR:
            shape = new Ellipse2D.Double(point.x - CONTROL_PORT_SIZE / 2, point.y - CONTROL_PORT_SIZE / 2,
                    CONTROL_PORT_SIZE, CONTROL_PORT_SIZE);
            break;
        }
        DrawUtils.gradientFillShape(g, color.brighter().brighter().brighter().brighter(), color.darker(), shape);
        if (getPortText()!=null){
        	g.setColor(Color.WHITE);
            Font oldFont = g.getFont();
    		g.setFont(new Font(oldFont.getFontName(),Font.BOLD,7));
            Rectangle2D bounds = g.getFontMetrics().getStringBounds(getPortText(), g);
            g.drawString(getPortText(), (int)(shape.getBounds().getX() + (shape.getBounds().getWidth()-bounds.getWidth())*2/4), 
    		(int)(shape.getBounds().getY() + (shape.getBounds().getHeight()+bounds.getHeight())*4/8));
            g.setFont(oldFont);
        }
    }

    /**
     * @param point
     * @return
     */
    private Shape drawPortArrow(Point point) {
        Shape shape;
        Polygon triangle = new Polygon();
        triangle.addPoint(point.x - DATA_PORT_SIZE / 2, point.y - DATA_PORT_SIZE / 2);
        triangle.addPoint(point.x + DATA_PORT_SIZE / 2, point.y);
        triangle.addPoint(point.x - DATA_PORT_SIZE / 2, point.y + DATA_PORT_SIZE / 2);
//        shape = DrawUtils.getRoundedShape(triangle);
        shape = triangle;
        return shape;
    }

    /**
     * @param offset
     */
    public void setOffset(Point offset) {
        this.offset = offset;
    }

    /**
     * @return the absolute position of the port
     */
    public Point getPosition() {
        Point nodePosition = this.port.getNode().getPosition();
        int offsetX=this.offset.x;
//        if ((PortGUI.DATA_PORT_SIZE / 2) + 1 < this.offset.x){
//        	offsetX=this.offset.x+(PortGUI.DATA_PORT_SIZE / 2);
//        }else{
//        	offsetX=0;
//        }
        return new Point(nodePosition.x + offsetX, nodePosition.y + this.offset.y);
    }

    /**
     * @param bool
     */
    protected void setSelectedFlag(boolean bool) {
        this.selected = bool;
    }

    /**
     * @param workflowName
     */
    public void removeToken(String workflowName) {
        int count = -1;
        for (String key : this.tokens) {
            count++;
            if (workflowName.equals(key)) {
                break;
            }
        }
        if (count != -1) {
            this.tokens.remove(count);
        }

    }

    /**
     * @param workflowName
     */
    public void addToken(String workflowName) {
        boolean found = false;
        for (String key : this.tokens) {
            if (workflowName.equals(key)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.tokens.add(workflowName);
        }

    }

    /**
	 * 
	 */
    public void reset() {
        this.tokens.clear();

    }

	public String getPortText() {
		return portText;
	}

	public void setPortText(String portText) {
		this.portText = portText;
	}
}