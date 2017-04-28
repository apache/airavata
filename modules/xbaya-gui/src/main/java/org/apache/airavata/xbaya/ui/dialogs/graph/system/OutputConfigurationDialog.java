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
package org.apache.airavata.xbaya.ui.dialogs.graph.system;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.common.utils.WSConstants;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.system.OutputNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class OutputConfigurationDialog {

    private XBayaGUI xbayaGUI;

    private OutputNode node;

    private XBayaDialog dialog;

    private XBayaTextField nameTextField;

    private XBayaTextArea descriptionTextArea;

    private XBayaTextField dataTypeField;

    private XBayaLabel dataTypeLabel;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public OutputConfigurationDialog(OutputNode node, XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        String name = this.node.getConfiguredName();
        if (name == null) {
            name = this.node.getName();
        }
        this.nameTextField.setText(name);
        this.descriptionTextArea.setText(this.node.getDescription());
        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    private void setInput() {
        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();
        if (name.length() == 0) {
            String warning = "The name cannot be empty.";
            this.xbayaGUI.getErrorWindow().error(warning);
            return;
        }
        this.node.setConfigured(true);
        this.node.setConfiguredName(name);
        this.node.setDescription(description);
        hide();
        this.xbayaGUI.getGraphCanvas().repaint();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.dataTypeField = new XBayaTextField(this.node.getParameterType().toString());
        this.dataTypeField.setEditable(false);
        this.dataTypeLabel = new XBayaLabel("Type", this.dataTypeField);
        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(nameLabel);
        mainPanel.add(this.nameTextField);
        mainPanel.add(this.dataTypeLabel);
        mainPanel.add(this.dataTypeField);
        mainPanel.add(descriptionLabel);
        mainPanel.add(this.descriptionTextArea);
        mainPanel.layout(3, 2, 2, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setInput();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.xbayaGUI, "Input Parameter Configuration", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}