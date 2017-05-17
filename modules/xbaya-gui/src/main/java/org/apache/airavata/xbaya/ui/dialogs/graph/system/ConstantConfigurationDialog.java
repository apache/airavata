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
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.workflow.model.graph.system.ConstantNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextComponent;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.xmlpull.infoset.XmlElement;

public class ConstantConfigurationDialog {

    private XBayaGUI xbayaGUI;

    private ConstantNode node;

    private XBayaDialog dialog;

    private GridPanel gridPanel;

    private XBayaTextField nameTextField;

    private XBayaTextField idTextField;

    private XBayaTextField typeTextField;

    private XBayaLabel valueLabel;

    private XBayaTextField valueTextField;

    private XBayaTextArea valueTextArea;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public ConstantConfigurationDialog(ConstantNode node, XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        DataType type = this.node.getType();
        XBayaTextComponent textComponent;
        textComponent = this.valueTextField;
        this.valueLabel.setText("Default value");
        this.valueLabel.setLabelFor(textComponent);
        final int index = 7;
        this.gridPanel.remove(index);
        this.gridPanel.add(textComponent, index);
        this.gridPanel.layout(4, 2, 3, 1);

        String name = this.node.getName();
        this.nameTextField.setText(name);
        this.idTextField.setText(this.node.getID());
        this.typeTextField.setText(type.toString());
        Object value = this.node.getValue();
        String valueString;
        if (value == null) {
            valueString = "";
        } else if (value instanceof XmlElement) {
            valueString = XMLUtil.xmlElementToString((XmlElement) value);
        } else {
            valueString = value.toString();
        }
        textComponent.setText(valueString);

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
        DataType type = this.node.getType();
        String valueString;
        valueString = this.valueTextField.getText();

        if (name.length() == 0) {
            String warning = "The name cannot be empty.";
            this.xbayaGUI.getErrorWindow().error(warning);
            return;
        }
        Object value = null;
        if (valueString.length() > 0) {
            if (!this.node.isInputValid(valueString)) {
                String warning = "The defalut value is not valid for " + this.node.getType() + ".";
                this.xbayaGUI.getErrorWindow().error(warning);
            }
            value = valueString;
        }

        this.node.setName(name);
        this.node.setValue(value);
        hide();
        this.xbayaGUI.getGraphCanvas().repaint();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.idTextField = new XBayaTextField();
        this.idTextField.setEditable(false);
        XBayaLabel idLabel = new XBayaLabel("ID", this.idTextField);

        this.typeTextField = new XBayaTextField();
        this.typeTextField.setEditable(false);
        XBayaLabel typeLabel = new XBayaLabel("Type", this.typeTextField);

        this.valueTextField = new XBayaTextField(); // for string
        this.valueTextArea = new XBayaTextArea(); // for XML
        // temporaly set text field.
        this.valueLabel = new XBayaLabel("", this.valueTextField);

        this.gridPanel = new GridPanel();
        this.gridPanel.add(nameLabel);
        this.gridPanel.add(this.nameTextField);
        this.gridPanel.add(idLabel);
        this.gridPanel.add(this.idTextField);
        this.gridPanel.add(typeLabel);
        this.gridPanel.add(this.typeTextField);
        this.gridPanel.add(this.valueLabel);
        this.gridPanel.add(this.valueTextField);
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

        this.dialog = new XBayaDialog(this.xbayaGUI, "Constant Configuration", this.gridPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

}