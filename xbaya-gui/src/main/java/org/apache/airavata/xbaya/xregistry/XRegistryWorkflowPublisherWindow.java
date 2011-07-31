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

package org.apache.airavata.xbaya.xregistry;

import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;

public class XRegistryWorkflowPublisherWindow {

    private XBayaEngine engine;

    private XBayaDialog dialog;

    private JButton okButton;

    private boolean makePublic = false;

    private JCheckBox chkMakePublic;

    private JTextField messageField;

    public XRegistryWorkflowPublisherWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private void ok() {
        if (Pattern.matches("[\\s | \\w]+", this.messageField.getText())) {
            this.okButton.setEnabled(false);
            this.hide();
        } else {
            this.engine.getErrorWindow().warning("Invalid name");
        }
    }

    public void show() {
        this.dialog.show();
    }

    public void hide() {
        this.dialog.hide();
    }

    /**
     * Intialize UI
     */
    private void initGUI() {

        JPanel buttonPanel = new JPanel();
        this.okButton = new JButton("OK");
        this.okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }

        });
        buttonPanel.add(this.okButton);

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "Select a workflow to load");
        mainPanel.getSwingComponent().setBorder(border);
        chkMakePublic = new JCheckBox("Make public");
        mainPanel.add(chkMakePublic);

        messageField = new JTextField(engine.getWorkflow().getName());
        messageField.setEditable(true);
        mainPanel.add(messageField);
        mainPanel.layout(2, 1, 0, 0);

        this.dialog = new XBayaDialog(this.engine, "Export to XRegistry", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(this.okButton);
    }

    public boolean isMakePublic() {
        return this.chkMakePublic.isSelected();
    }

    public void setLabel(String val) {
        this.messageField.setText(val);
    }

    public String getLabel() {
        return this.messageField.getText();
    }
}