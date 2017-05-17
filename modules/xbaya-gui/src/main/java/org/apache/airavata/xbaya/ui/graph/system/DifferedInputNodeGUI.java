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

import org.apache.airavata.workflow.model.graph.system.DifferedInputNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.DifferedInputConfigurationDialog;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;

public class DifferedInputNodeGUI extends ConfigurableNodeGUI {

	// private static final MLogger logger = MLogger.getLogger();

	private static final String CONFIG_AREA_STRING = "Config";

	private static final Color HEAD_COLOR = new Color(153, 204, 255);

	private DifferedInputNode inputNode;

	private DifferedInputConfigurationDialog configurationWindow;

	private volatile boolean configCanBeDisplayed = true;

	/**
	 * @param node
	 */
	public DifferedInputNodeGUI(DifferedInputNode node) {
		super(node);
		this.inputNode = node;
		setConfigurationText(CONFIG_AREA_STRING);
		this.headColor = HEAD_COLOR;
	}

	/**
	 * Shows a configuration window when a user click the configuration area.
	 * 
	 * @param engine
	 */
	@Override
	public void showConfigurationDialog(XBayaGUI xbayaGUI) {
		if (testAndSetConfigDisplay()) {
			if (this.inputNode.isConnected()) {
				if (this.configurationWindow == null) {
					this.configurationWindow = new DifferedInputConfigurationDialog(
							this.inputNode, xbayaGUI);
				}
				this.configurationWindow.show();

			} else {
				xbayaGUI.getErrorWindow().info(
						ErrorMessages.INPUT_NOT_CONNECTED_WARNING);
			}
		}
	}

	protected synchronized boolean testAndSetConfigDisplay() {
		if (this.configCanBeDisplayed) {
			this.configCanBeDisplayed = false;
			return true;
		}
		return false;
	}

	public synchronized void closingDisplay() {
		this.configCanBeDisplayed = true;
	}

	public DifferedInputNode getInputNode() {
		return this.inputNode;
	}

	@Override
	protected void setSelectedFlag(boolean flag) {
		this.selected = flag;
		if (this.selected) {
			this.headColor = SELECTED_HEAD_COLOR;
		} else {
			this.headColor = HEAD_COLOR;
		}
	}
}