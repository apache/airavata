/*
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
 *
 */

package org.apache.airavata.xbaya.gpel.gui;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;

public class GPELConfigurationWindow {

    private XBayaEngine engine;

    private WorkflowClient workflowClient;

    private XBayaDialog dialog;

    private JButton okButton;

    private XBayaTextField uriField;

    /**
     * Constructs a SaveWorkflowWindow.
     * 
     * @param engine
     */
    public GPELConfigurationWindow(XBayaEngine engine) {
        this.engine = engine;
        this.workflowClient = this.engine.getWorkflowClient();
        initGUI();
    }

    /**
     * Shows the window.
     */
    public void show() {

        URI uri = this.workflowClient.getEngineURL();
        String uriString;
        if (uri == null) {
            uriString = XBayaConstants.DEFAULT_GPEL_ENGINE_URL.toString();
        } else {
            uriString = uri.toString();
        }
        this.uriField.setText(uriString);

        this.dialog.show();
    }

    /**
     * Hides the window.
     */
    public void hide() {
        this.dialog.hide();
    }

    private void ok() {
        // Set the name and description to the graph.
        String uriString = this.uriField.getText();

        if (uriString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.GPEL_URL_EMPTY);
            return;
        }
        URI uri;
        try {
            uri = new URI(uriString).parseServerAuthority();
        } catch (URISyntaxException e) {
            this.engine.getErrorWindow().error(ErrorMessages.GPEL_WRONG_URL, e);
            return;
        }

        hide();

        try {
            this.workflowClient.setEngineURL(uri);
        } catch (WorkflowEngineException e) {
            this.engine.getErrorWindow().error(ErrorMessages.GPEL_CONNECTION_ERROR, e);
        } catch (RuntimeException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }
    }

    /**
     * Initializes the GUI
     */
    private void initGUI() {
        this.uriField = new XBayaTextField();
        XBayaLabel uriLabel = new XBayaLabel("URL", this.uriField);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(uriLabel);
        mainPanel.add(this.uriField);
        mainPanel.layout(1, 2, GridPanel.WEIGHT_NONE, 1);

        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Configure the BPEL Engine", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }
}