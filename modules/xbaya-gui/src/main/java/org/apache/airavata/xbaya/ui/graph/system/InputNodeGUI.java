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

import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.InputConfigurationDialog;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;

public class InputNodeGUI extends ConfigurableNodeGUI {

    private static final String CONFIG_AREA_STRING = "Config";

    private static final Color HEAD_COLOR = new Color(153, 204, 255);

    private InputNode inputNode;

    private InputConfigurationDialog configurationWindow;

    /**
     * @param node
     */
    public InputNodeGUI(InputNode node) {
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
    protected void showConfigurationDialog(XBayaGUI xbayaGUI) {
        if (this.inputNode.isConnected()) {
            if (this.configurationWindow == null) {
                this.configurationWindow = new InputConfigurationDialog(this.inputNode, xbayaGUI);
            }
            this.configurationWindow.show();

        } else {
        	xbayaGUI.getErrorWindow().info(ErrorMessages.INPUT_NOT_CONNECTED_WARNING);
        }
    }

    public InputNode getInputNode() {
        return this.inputNode;
    }

    protected void setSelectedFlag(boolean flag) {
        this.selected = flag;
        if (this.selected) {
            this.headColor = SELECTED_HEAD_COLOR;
        } else {
            this.headColor = HEAD_COLOR;
        }
    }
}