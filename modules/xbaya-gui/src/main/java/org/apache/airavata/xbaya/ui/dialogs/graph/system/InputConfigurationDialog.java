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

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextComponent;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.xmlpull.infoset.XmlElement;

public class InputConfigurationDialog {

    private XBayaGUI xbayaGUI;

    private InputNode node;

    private XBayaDialog dialog;

    private GridPanel gridPanel;

    private XBayaTextField nameTextField;

    private XBayaTextArea descriptionTextArea;

    private XBayaLabel valueLabel;

    private XBayaTextField valueTextField;

    private XBayaTextField dataTypeTextField;

    private XBayaLabel dataTypeLabel;


    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param xbayaGUI
     */
    public InputConfigurationDialog(InputNode node, XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        String name = this.node.getID(); // Show ID.
        this.nameTextField.setText(name);
        this.descriptionTextArea.setText(this.node.getDescription());
        Object value = this.node.getDefaultValue();
        String valueString;
        if (value == null) {
            valueString = "";
        } else if (value instanceof XmlElement) {
            valueString = XMLUtil.xmlElementToString((XmlElement) value);
        } else {
            valueString = value.toString();
        }
        this.dataTypeTextField.setText(this.node.getParameterType().toString());
        this.valueTextField.setText(valueString);
        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    private void setInput() {
        XBayaTextComponent textComponent;
        textComponent = this.valueTextField;
        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();
        String valueString = textComponent.getText();
        if (name.length() == 0) {
            String warning = "The name cannot be empty.";
            this.xbayaGUI.getErrorWindow().error(warning);
            return;
        }
        Object value = null;
        if (valueString.length() > 0) {
            if (!this.node.isInputValid(valueString)) {
                String warning = "The defalut value is not valid for " + this.node.getParameterType() + ".";
                this.xbayaGUI.getErrorWindow().error(warning);
            }
            value = valueString;
        }

        this.node.setConfigured(true);
        this.node.setConfiguredName(name);
        this.node.setDescription(description);
        this.node.setDefaultValue(value);
        hide();
        this.xbayaGUI.getGraphCanvas().repaint();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        this.valueTextField = new XBayaTextField(); // for string
        this.valueLabel = new XBayaLabel("Value", this.valueTextField);

        this.dataTypeTextField = new XBayaTextField();
        this.dataTypeTextField.setEditable(false);
        this.dataTypeLabel = new XBayaLabel("Type", this.dataTypeTextField);

        this.gridPanel = new GridPanel();
        this.gridPanel.add(nameLabel);
        this.gridPanel.add(this.nameTextField);
        this.gridPanel.add(this.valueLabel);
        this.gridPanel.add(this.valueTextField);
        this.gridPanel.add(this.dataTypeLabel);
        this.gridPanel.add(this.dataTypeTextField);
        this.gridPanel.add(descriptionLabel);
        this.gridPanel.add(this.descriptionTextArea);
        this.gridPanel.layout(4, 2, 3, 1);

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

        this.dialog = new XBayaDialog(this.xbayaGUI, "Input Parameter Configuration", this.gridPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

}