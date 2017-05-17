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

import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.PortImpl;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.ForEachConfigurationDialog;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public class ForEachNodeGUI extends ConfigurableNodeGUI {

    private static final String CONFIG_AREA_STRING = "Config";

    private ForEachNode node;

    private ForEachConfigurationDialog configurationWindow;

    private Polygon polygon;

    private GeneralPath generalPath;

    /**
     * @param node
     */
    public ForEachNodeGUI(ForEachNode node) {
        super(node);
        this.node = node;
        setConfigurationText(CONFIG_AREA_STRING);
        this.polygon = new Polygon(); // To avoid null check.
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
            this.configurationWindow = new ForEachConfigurationDialog(this.node, xbayaGUI);
        }
        this.configurationWindow.show();
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

	protected Color getComponentHeaderColor() {
		return headColor;
	}

	protected String getComponentHeaderText() {
		return node.getName();
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
        head.addPoint(position.x, position.y + this.headHeight / 2);
        head.addPoint(position.x, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y + this.headHeight);
        head.addPoint(position.x + this.dimension.width, position.y);
		return head;
	}

    private void calculatePositions() {
        // XXX Avoid instantiating a new polygon each time.
        this.polygon = new Polygon();
        Point position = getPosition();
        this.polygon.addPoint(position.x, position.y + this.headHeight / 2);
        this.polygon.addPoint(position.x, position.y + this.dimension.height);
        this.polygon.addPoint(position.x + this.dimension.width, position.y + this.dimension.height + this.headHeight
                - this.headHeight / 2);
        this.polygon.addPoint(position.x + this.dimension.width, position.y);
        DrawUtils.setupRoundedGeneralPath(polygon, getComponentShape());
    }

    /**
     * Sets up the position of ports
     */
    @Override
    protected void setPortPositions() {
        super.setPortPositions();

        PortImpl controlInPort = this.node.getControlInPort();
        if (controlInPort != null) {
            Point off = new Point(0, this.headHeight / 2);
            NodeController.getGUI(controlInPort).setOffset(off);
        }
    }
}