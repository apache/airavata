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
import java.net.URL;

import org.apache.airavata.common.utils.BrowserLauncher;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.graph.system.OutputConfigurationDialog;

public class OutputNodeGUI extends ConfigurableNodeGUI {

    private static final String CONFIG_AREA_STRING = "View";

    private static final Color HEAD_COLOR = new Color(35, 107, 142);

    private OutputNode outputNode;

    private OutputConfigurationDialog configurationWindow;

    /**
     * @param node
     */
    public OutputNodeGUI(OutputNode node) {
        super(node);
        this.outputNode = node;
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

        if (this.node instanceof OutputNode) {

            String description = ((OutputNode) this.node).getDescription();
            if (null != description) {
                // try to parse it to a URL and if yes try to open the browser
                try {
                    description = description.trim();
                    URL url = new URL(description);
                    // no exception -> valid url lets try to open it
                    BrowserLauncher.openURL(url);
                } catch (Exception e) {
                    // do nothing since this is an optional attempt
                }

            }
        }
        if (this.configurationWindow == null) {
            this.configurationWindow = new OutputConfigurationDialog(this.outputNode, xbayaGUI);
        }
        this.configurationWindow.show();

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