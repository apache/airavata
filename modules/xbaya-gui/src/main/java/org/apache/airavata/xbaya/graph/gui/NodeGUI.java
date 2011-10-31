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

package org.apache.airavata.xbaya.graph.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.Port;
import org.apache.airavata.xbaya.monitor.gui.MonitorEventHandler.NodeState;

public abstract class NodeGUI implements GraphPieceGUI {

    /**
     * BREAK_POINT_BORDER_COLOR
     */
    protected static final Color BREAK_POINT_BORDER_COLOR = new Color(53, 103, 157);

    /**
     * The minimum width of the node.
     */
    protected static final int MINIMUM_WIDTH = 100;

    /**
     * The minimum height of the node
     */
    protected static final int MINIMUM_HEIGHT = 37;

    protected static final int TEXT_GAP_X = 5;

    protected static final int TEXT_GAP_Y = 2;

    protected static final Color TEXT_COLOR = Color.black;

    protected static final int PORT_GAP = 13;

    protected static final int PORT_INITIAL_GAP = 10;

    protected static final Color EDGE_COLOR = Color.black;

    protected static final Color DEFAULT_HEAD_COLOR = Color.white;

    protected static final Color SELECTED_HEAD_COLOR = Color.pink;

    /**
     * The default body color.
     */
    public static final Color DEFAULT_BODY_COLOR = new Color(250, 220, 100);

    protected static final Color DRAGGED_BODY_COLOR = Color.lightGray;

    protected static final Color BREAK_POINT_COLOR = new Color(174, 197, 221);

    protected Node node;

    protected Dimension dimension;

    protected int headHeight;

    protected boolean selected = false;

    protected boolean dragged = false;

    protected Color headColor;

    protected Color bodyColor;

    protected List<Paintable> paintables;

    /**
     * @param node
     */
    public NodeGUI(Node node) {
        this.node = node;
        this.bodyColor = DEFAULT_BODY_COLOR;
        this.headColor = DEFAULT_HEAD_COLOR;
        // The followings are just to make sure that it has some size.
        this.dimension = new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        this.paintables = new LinkedList<Paintable>();
    }

    /**
     * Sets the color of the body.
     * 
     * @param color
     *            The color
     */
    public void setBodyColor(Color color) {
        this.bodyColor = color;
    }

    /**
     * @return The color of the body.
     */
    public Color getBodyColor() {
        return this.bodyColor;
    }

    /**
     * Sets the color of the head.
     * 
     * @param color
     *            The color to set
     */
    public void setHeadColor(Color color) {
        this.headColor = color;
    }

    /**
     * @see org.apache.airavata.xbaya.graph.gui.GraphPieceGUI#mouseClicked(java.awt.event.MouseEvent,
     *      org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    public void mouseClicked(MouseEvent event, XBayaEngine engine) {
        // Nothing by default
    }

    /**
     * @param paintable
     */
    public void addPaintable(Paintable paintable) {
        this.paintables.add(paintable);
    }

    /**
     * @param paintable
     */
    public void removePaintable(Paintable paintable) {
        this.paintables.remove(paintable);
    }

    /**
     * @param flag
     */
    protected void setSelectedFlag(boolean flag) {
        this.selected = flag;
        if (this.selected) {
            this.headColor = SELECTED_HEAD_COLOR;
        } else {
            this.headColor = DEFAULT_HEAD_COLOR;
        }
    }

    /**
     * @param flag
     */
    protected void setDraggedFlag(boolean flag) {
        this.dragged = flag;
    }

    /**
     * Returns the position of the node.
     * 
     * @return the position of the node
     */
    protected Point getPosition() {
        return this.node.getPosition();
    }

    /**
     * Gets the bounding Rectangle of this Node.
     * 
     * @return A rectangle indicating this component's bounds
     */
    protected Rectangle getBounds() {
        return new Rectangle(this.node.getPosition(), this.dimension);
    }

    /**
     * Checks if a user's click is to select the node.
     * 
     * @param point
     *            The location.
     * @return true if the user's click is to select the node; false otherwise
     */
    protected boolean isIn(Point point) {
        Rectangle bounds = getBounds();
        return bounds.contains(point);
    }

    /**
     * Checks if a user's click is to select the configuration
     * 
     * @param point
     * @return true if the user's click is to select the node, false otherwise
     */
    @SuppressWarnings("unused")
    protected boolean isInConfig(Point point) {
        return false;
    }

    /**
     * Calculates the width of the node and x-coordinate of the ports. This method has to be called before painting any
     * parts of the graph.
     * 
     * @param g
     */
    protected void calculatePositions(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        this.headHeight = fm.getHeight() + TEXT_GAP_Y * 2;

        int maxNumPort = Math.max(this.node.getOutputPorts().size(), this.node.getInputPorts().size());
        this.dimension.height = Math.max(this.headHeight + PORT_INITIAL_GAP + PORT_GAP * maxNumPort, MINIMUM_HEIGHT);
        this.dimension.width = Math.max(MINIMUM_WIDTH, fm.stringWidth(this.node.getID()) + TEXT_GAP_X * 2);

        /* Calculates the position of ports */
        setPortPositions();
    }

    /**
     * @param g
     */
    protected void paint(Graphics2D g) {

        Point position = this.node.getPosition();

        // Draws the body.
        if (this.dragged) {
            g.setColor(DRAGGED_BODY_COLOR);
        } else {
            g.setColor(this.bodyColor);
        }
        g.fillRect(position.x, position.y, this.dimension.width, this.dimension.height);

        // Draws the head.
        g.setColor(this.headColor);
        g.fillRect(position.x, position.y, this.dimension.width, this.headHeight);
        // Draw a small circle to indicate the break
        if (node.isBreak()) {
            g.setColor(BREAK_POINT_COLOR);
            int r = this.headHeight / 4;
            g.fillOval(position.x + this.dimension.width - 3 * r, position.y + r, 2 * r, 2 * r);
            g.setColor(BREAK_POINT_BORDER_COLOR);
            g.drawOval(position.x + this.dimension.width - 3 * r, position.y + r, 2 * r, 2 * r);
        }

        // Text
        g.setColor(TEXT_COLOR);

        // XXX it's debatable if we should show the ID or the name.
        // String name = this.node.getName();
        String name = this.node.getID();
        g.drawString(name, position.x + TEXT_GAP_X, position.y + this.headHeight - TEXT_GAP_Y);

        // Edge
        g.setColor(EDGE_COLOR);
        g.drawRect(position.x, position.y, this.dimension.width, this.dimension.height);

        // Paint all ports
        for (Port port : this.node.getAllPorts()) {
            port.getGUI().paint(g);
        }

        // Paint extras
        for (Paintable paintable : this.paintables) {
            paintable.paint(g, this.node.getPosition());
        }
    }

    /**
     * Sets up the position of ports
     */
    protected void setPortPositions() {
        // inputs
        List<? extends Port> inputPorts = this.node.getInputPorts();
        for (int i = 0; i < inputPorts.size(); i++) {
            Port port = inputPorts.get(i);
            Point offset = new Point(PortGUI.DATA_PORT_SIZE / 2, this.headHeight + PORT_INITIAL_GAP + PORT_GAP * i);
            port.getGUI().setOffset(offset);
        }

        // outputs
        List<? extends Port> outputPorts = this.node.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            // Use getBounds() instead of this.dimension because subclass might
            // overwrite getBounds() to have different shape.
            Point offset = new Point(this.getBounds().width - PortGUI.DATA_PORT_SIZE / 2, this.headHeight
                    + PORT_INITIAL_GAP + PORT_GAP * i);
            port.getGUI().setOffset(offset);
        }

        // control-in
        Port controlInPort = this.node.getControlInPort();
        if (controlInPort != null) {
            controlInPort.getGUI().setOffset(new Point(0, 0));
        }

        // control-outs
        for (Port controlOutPort : this.node.getControlOutPorts()) {
            // By default, all ports will be drawn at the same place. Subclass
            // should rearrange them if there are more than one control-out
            // ports.
            controlOutPort.getGUI().setOffset(new Point(getBounds().width, getBounds().height));
        }
    }

    /**
     * @param workflowName
     * @param failed
     */
    public void setToken(String workflowName, NodeState state) {
        List<DataPort> inputPorts = this.node.getInputPorts();
        switch (state) {
        case EXECUTING:

            for (DataPort dataPort : inputPorts) {
                ((DataPort) dataPort.getFromPort()).getGUI().removeToken(workflowName);
                dataPort.getGUI().addToken(workflowName);
            }
            break;
        case FINISHED:
            for (DataPort dataPort : inputPorts) {
                dataPort.getGUI().removeToken(workflowName);
            }

            List<DataPort> outputPorts = this.node.getOutputPorts();
            for (DataPort dataPort : outputPorts) {
                dataPort.getGUI().addToken(workflowName);
            }
            break;
        case FAILED:

            break;

        }

    }

    /**
	 * 
	 */
    public void resetTokens() {

        List<DataPort> inputPorts = this.node.getInputPorts();
        for (DataPort dataPort : inputPorts) {
            dataPort.getGUI().reset();
        }
        List<DataPort> outputPorts = this.node.getOutputPorts();
        for (DataPort dataPort : outputPorts) {
            dataPort.getGUI().reset();
        }
    }
}