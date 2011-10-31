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

package org.apache.airavata.xbaya.graph.interaction.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.monitor.gui.MonitorPanel;

public class ServiceInteractionWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private WaitDialog invokingDialog;

    private XBayaTextArea consoleTextArea;

    private XBayaTextField commandField;

    private String nodeID;

    public ServiceInteractionWindow(XBayaEngine engine, String nodeID) {
        this.engine = engine;
        this.nodeID = nodeID;
        initGui();
    }

    /**
	 * 
	 */
    private void initGui() {

        GridPanel mainPanel = new GridPanel();

        MonitorPanel monitorPanel = new MonitorPanel(this.engine, this.nodeID);
        this.consoleTextArea = new XBayaTextArea();
        XBayaLabel consoleLabel = new XBayaLabel("Console", this.consoleTextArea);

        this.commandField = new XBayaTextField();
        XBayaLabel commandLabel = new XBayaLabel("Command", this.commandField);

        mainPanel.add(monitorPanel);
        mainPanel.add(consoleLabel);
        mainPanel.add(this.consoleTextArea);
        mainPanel.add(commandLabel);
        mainPanel.add(this.commandField);

        mainPanel.layout(5, 1, GridPanel.WEIGHT_NONE, 1);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new AbstractAction() {
            /**
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                send();

            }

        });

        JButton cancelButton = new JButton("Done");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }

        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Deploy workflow to ODE and Registry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(sendButton);

    }

    private void hide() {
        this.dialog.hide();

    }

    /**
	 * 
	 */
    public void show() {
        this.dialog.show();
    }

    private void send() {
        String command = this.commandField.getText();
        this.commandField.setText("");
        this.consoleTextArea.setText(this.consoleTextArea.getText() + "\n>" + command + "\n");

    }
}