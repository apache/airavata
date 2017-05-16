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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.Port;
import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.amazon.InstanceConfigurationDialog;
import org.apache.airavata.xbaya.ui.graph.PortGUI;
import org.apache.airavata.xbaya.ui.graph.system.ConfigurableNodeGUI;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class InstanceNodeGUI extends ConfigurableNodeGUI {

    private InstanceNode node;

    private Polygon polygon;
    
    private GeneralPath generalPath;

    private InstanceConfigurationDialog configDialog;

    /**
     * Constructs a InstanceNodeGUI.
     * 
     * @param node
     */
    public InstanceNodeGUI(InstanceNode node) {
        super(node);
        this.node = node;
        this.polygon = new Polygon();
        generalPath = new GeneralPath();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.system.ConfigurableNodeGUI#showConfigurationDialog(org.apache.airavata.xbaya.XBayaEngine)
     */
    @Override
    protected void showConfigurationDialog(XBayaGUI xbayaGUI) {
        if (this.configDialog == null) {
            this.configDialog = new InstanceConfigurationDialog(this.node, xbayaGUI);
        }
        this.configDialog.show();
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
        return this.getComponentShape().getBounds();
    }

    /**
     * @see org.apache.airavata.xbaya.ui.graph.NodeGUI#isIn(java.awt.Point)
     */
    @Override
    protected boolean isIn(Point point) {
        return this.polygon.contains(point);
    }

	protected Color getComponentHeaderColor() {
		return this.headColor;
	}

	protected String getComponentHeaderText() {
		return this.node.getName();
	}

	protected GeneralPath getComponentShape() {
		return generalPath;
	}

	protected RoundRectangle2D getComponentHeaderShape() {
		RoundRectangle2D componentHeaderBoundaryRect = new RoundRectangle2D.Double(getPosition().x, getPosition().y, this.dimension.width, this.headHeight, DrawUtils.ARC_SIZE,DrawUtils.ARC_SIZE);
		return componentHeaderBoundaryRect;
	}

	protected Node getNode() {
		return this.node;
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

        // control out port
        List<? extends Port> controlOutPorts = this.node.getControlOutPorts();
        Port controlOutPort1 = controlOutPorts.get(0);
        Point offset = new Point(getBounds().width / 2, getBounds().height);
        NodeController.getGUI(controlOutPort1).setOffset(offset);
    }
    
    private void calculatePositions() {
        // Avoid instantiating a new polygon each time.
        this.polygon.reset();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y);
        this.polygon.addPoint(position.x, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width / 2, position.y + this.dimension.height
                + this.headHeight);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width, position.y);
        DrawUtils.setupRoundedGeneralPath(polygon, getComponentShape());
    }
}