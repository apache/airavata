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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.system.EndifNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.EndifConfigurationDialog;
import org.apache.airavata.xbaya.ui.graph.PortGUI;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class EndifNodeGUI extends ConfigurableNodeGUI {

    private EndifConfigurationDialog configurationWindow;

    private EndifNode node;

    private Polygon polygon;
    
    private GeneralPath generalPath;

    /**
     * @param node
     */
    public EndifNodeGUI(EndifNode node) {
        super(node);
        this.node = node;
        this.polygon = new Polygon();
        generalPath = new GeneralPath();
    }

    /**
     * Shows a configuration window when a user click the configuration area.
     * 
     * @param engine
     */
    @Override
    protected void showConfigurationDialog(XBayaGUI xbayaGUI) {
        if (this.configurationWindow == null) {
            this.configurationWindow = new EndifConfigurationDialog(this.node, xbayaGUI);
        }
        this.configurationWindow.show();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#calculatePositions(java.awt.Graphics)
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

    @Override
    protected boolean isIn(Point point) {
        return this.polygon.contains(point);
    }

	protected Color getComponentHeaderColor() {
		return headColor;
	}

	protected String getComponentHeaderText() {
		return this.node.getName();
	}

	protected GeneralPath getComponentHeaderShape() {
		return DrawUtils.getRoundedShape(createHeader(getPosition()));
	}

	protected GeneralPath getComponentShape() {
		return generalPath;
	}

	protected Node getNode() {
		return this.node;
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
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#setPortPositions()
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
            NodeController.getGUI(port).setOffset(offset);
        }

        // outputs
        List<? extends Port> outputPorts = this.node.getOutputPorts();
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            Point offset = new Point(this.getBounds().width - PortGUI.DATA_PORT_SIZE / 2, (int) (this.headHeight
                    + PORT_INITIAL_GAP + PORT_GAP * (outputPorts.size() / 2.0 + i)));
            NodeController.getGUI(port).setOffset(offset);
        }

        // control-in
        Port controlInPort = this.node.getControlInPort();
        if (controlInPort != null) {
        	NodeController.getGUI(controlInPort).setOffset(new Point(0, 0));
        }

        // control-out
        for (Port controlOutPort : this.node.getControlOutPorts()) {
        	NodeController.getGUI(controlOutPort).setOffset(new Point(getBounds().width, getBounds().height - this.headHeight / 2));
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
        DrawUtils.setupRoundedGeneralPath(polygon, getComponentShape());
    }
}