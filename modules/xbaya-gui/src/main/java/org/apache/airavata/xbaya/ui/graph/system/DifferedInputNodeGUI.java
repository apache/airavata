/*
 * Copyright (c) 2012 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.ui.graph.system;

import java.awt.Color;

import org.apache.airavata.workflow.model.graph.system.DifferedInputNode;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.ErrorMessages;

/**
 * @author Chathura Herath
 */
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
	public void showConfigurationDialog(XBayaEngine engine) {
		if (testAndSetConfigDisplay()) {
			if (this.inputNode.isConnected()) {
				if (this.configurationWindow == null) {
					this.configurationWindow = new DifferedInputConfigurationDialog(
							this.inputNode, engine);
				}
				this.configurationWindow.show();

			} else {
				engine.getErrorWindow().info(
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

	protected synchronized void closingDisplay() {
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2012 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme!
 * Lab. For further information please visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall
 * not be used to endorse or promote products derived from this software without
 * prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name
 * nor may "Indiana University" appear in their name, without prior written
 * permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of any
 * other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual
 * property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 * WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 * NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 * INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 * INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 * "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 * LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 * ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 * GENERATED USING SOFTWARE.
 */
