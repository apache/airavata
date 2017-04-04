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
package org.apache.airavata.xbaya.ui.graph.amazon;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.amazon.TerminateInstanceNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.graph.PortGUI;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class TerminateInstanceNodeGUI extends NodeGUI {

    private TerminateInstanceNode node;

    private Polygon polygon;

    private GeneralPath generalPath;

    /**
     * Constructs a InstanceNodeGUI.
     * 
     * @param node
     */
    public TerminateInstanceNodeGUI(TerminateInstanceNode node) {
        super(node);
        this.node = node;
        this.polygon = new Polygon();
        generalPath = new GeneralPath();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.system.ConfigurableNodeGUI#calculatePositions(java.awt.Graphics)
     */
    @Override
    protected void calculatePositions(Graphics g) {
        super.calculatePositions(g);
        calculatePositions();
        setPortPositions();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#getBounds()
     */
    @Override
    protected Rectangle getBounds() {
        return this.polygon.getBounds();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#isIn(java.awt.Point)
     */
    @Override
    protected boolean isIn(Point point) {
        return this.polygon.contains(point);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.system.ConfigurableNodeGUI#paint(java.awt.Graphics2D)
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
        drawBody(g, generalPath, g.getColor());

        // Draws the head.
        Polygon head = createHeader(position);
        drawHeader(g, DrawUtils.getRoundedShape(head), node.getName(), headColor);

        // Edge
        drawEdge(g, generalPath, EDGE_COLOR);

        // Paint all ports
        drawPorts(g, node);
    }

	private Polygon createHeader(Point position) {
		Polygon head = new Polygon();
        head.addPoint(position.x, position.y);
        head.addPoint(position.x, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y + this.headHeight / 2);
		return head;
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

        // control in port
        Port controlInPort = this.node.getControlInPort();
        NodeController.getGUI(controlInPort).setOffset(new Point(0, 0));
    }

    private void calculatePositions() {
        // Avoid instantiating a new polygon each time.
        this.polygon.reset();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y);
        this.polygon.addPoint(position.x, position.y + this.dimension.height + this.headHeight / 2);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height + this.headHeight
                / 2);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.headHeight / 2);
        DrawUtils.setupRoundedGeneralPath(polygon, generalPath);
    }
}