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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.graph.NodeGUI;
import org.apache.airavata.xbaya.ui.utils.DrawUtils;

public abstract class ConfigurableNodeGUI extends NodeGUI {

	protected static final Color CONFIG_AREA_COLOR = new Color(220, 220, 220);

	protected static final String DEFAULT_CONFIG_AREA_TEXT = "Config";

	protected static final int CONFIG_AREA_GAP_X = 20;

	protected String configurationText;

	protected RoundRectangle2D configurationArea;

	/**
	 * @param node
	 */
	public ConfigurableNodeGUI(NodeImpl node) {
		super(node);
		this.configurationText = DEFAULT_CONFIG_AREA_TEXT;
//		this.configurationArea = new RoundRectangle2D();

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
			showConfigurationDialog(engine.getGUI());
		}
	}

	/**
	 * @param engine
	 */
	protected abstract void showConfigurationDialog(XBayaGUI xbayaGUI);

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
		
		int h = fm.getHeight() + TEXT_GAP_Y * 2+1;
		int w = this.dimension.width - CONFIG_AREA_GAP_X * 2;
		int x = position.x + CONFIG_AREA_GAP_X;
		int y = position.y
				+ this.headHeight
				+ (this.dimension.height - this.headHeight - h)
				/ 2;
		this.configurationArea=new RoundRectangle2D.Float(x,y,w,h,DrawUtils.ARC_SIZE, DrawUtils.ARC_SIZE);
	}

	/**
	 * Paints the config area
	 * 
	 * @param g
	 */
	@Override
	protected final void paint(Graphics2D g) {
		super.paint(g);
		drawComponentConfiguration(g);
	}

	/**
	 * @param g
	 */
	protected void drawComponentConfiguration(Graphics2D g) {
		String s = this.configurationText;
		g.setColor(CONFIG_AREA_COLOR);
		g.fill(this.configurationArea);
		g.setColor(TEXT_COLOR);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
		
		g.drawString(s, 
				(int)(this.configurationArea.getX() + (this.configurationArea.getWidth()-bounds.getWidth())/2), 
				(int)(this.configurationArea.getY() + (this.configurationArea.getHeight()+bounds.getHeight()/2)/2));
	}
}