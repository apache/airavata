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

import org.apache.airavata.workflow.model.graph.system.S3InputNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.S3FileChooser;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;

public class S3InputNodeGUI extends ConfigurableNodeGUI {

    private static final String CONFIG_AREA_STRING = "Config";

    private static final Color HEAD_COLOR = new Color(153, 204, 255);

    private S3InputNode inputNode;

    private S3FileChooser s3FileChooser;

    /**
     * @param node
     */
    public S3InputNodeGUI(S3InputNode node) {
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

        if (!this.inputNode.isConnected()) {
        	xbayaGUI.getErrorWindow().info(ErrorMessages.INPUT_NOT_CONNECTED_WARNING);
        } else {
            if (this.s3FileChooser == null) {
                this.s3FileChooser = new S3FileChooser(xbayaGUI, this.inputNode);
            }
            this.s3FileChooser.show();
        }
    }

    public S3InputNode getInputNode() {
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