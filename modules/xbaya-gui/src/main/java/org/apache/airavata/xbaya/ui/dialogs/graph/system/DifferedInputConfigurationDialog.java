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
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.workflow.model.graph.Node.NodeExecutionState;
import org.apache.airavata.workflow.model.graph.system.DifferedInputNode;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.graph.system.DifferedInputNodeGUI;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextComponent;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.xmlpull.infoset.XmlElement;

public class DifferedInputConfigurationDialog {
    private XBayaGUI xbayaGUI;

    private DifferedInputNode node;

    private XBayaDialog dialog;

    private GridPanel gridPanel;

    private XBayaTextField nameTextField;

    private XBayaTextArea descriptionTextArea;

    private XBayaLabel valueLabel;

    private XBayaTextField valueTextField;

    private XBayaTextArea valueTextArea;

    private XBayaTextArea metadataTextArea;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public DifferedInputConfigurationDialog(DifferedInputNode node, XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {
        DataType type = this.node.getParameterType();
        XBayaTextComponent textComponent;
//        boolean knownType = LEADTypes.isKnownType(type);
        textComponent = this.valueTextField;
        this.valueLabel.setText("Default value");
        this.valueLabel.setLabelFor(textComponent);
        final int index = 5;
        this.gridPanel.remove(index);
        this.gridPanel.add(textComponent, index);
        this.gridPanel.layout(new double[] { 0, 1.0 / 2, 0, 1.0 / 2 },
                new double[] { 0, 1 });
        // String name = this.node.getConfiguredName();
        // if (name == null) {
        // name = this.node.getName();
        // }
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
        textComponent.setText(valueString);
        XmlElement metadata = this.node.getMetadata();
        String metadataText;
        if (metadata == null) {
            metadataText = WSConstants.EMPTY_APPINFO;
        } else {
            metadataText = XMLUtil.xmlElementToString(metadata);
        }
        this.metadataTextArea.setText(metadataText);

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
    	
        this.dialog.hide();
        ((DifferedInputNodeGUI)NodeController.getGUI(this.node)).closingDisplay();
    }

    private void setInput() {
        DataType type = this.node.getParameterType();
        XBayaTextComponent textComponent;
        textComponent = this.valueTextField;

        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();
        String valueString = textComponent.getText();
        String metadataText = this.metadataTextArea.getText();

        if (name.length() == 0) {
            String warning = "The name cannot be empty.";
            this.xbayaGUI.getErrorWindow().error(warning);
            return;
        }
        Object value = null;
        if (valueString.length() > 0) {
            if (!this.node.isInputValid(valueString)) {
                String warning = "The defalut value is not valid for "
                        + this.node.getParameterType() + ".";
                this.xbayaGUI.getErrorWindow().error(warning);
            }
            value = valueString;
        }
        XmlElement metadata;
        if (metadataText.length() == 0) {
            metadata = null;
        } else {
            try {
                metadata = XMLUtil.stringToXmlElement(metadataText);
            } catch (RuntimeException e) {
                String warning = "The metadata is ill-formed.";
                this.xbayaGUI.getErrorWindow().error(warning, e);
                return;
            }
        }

        this.node.setConfigured(true);
        this.node.setConfiguredName(name);
        this.node.setDescription(description);
        this.node.setDefaultValue(value);
        this.node.setMetadata(metadata);
        this.node.setState(NodeExecutionState.FINISHED);
        
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
        XBayaLabel descriptionLabel = new XBayaLabel("Description",
                this.descriptionTextArea);

        this.valueTextField = new XBayaTextField(); // for string
        this.valueTextArea = new XBayaTextArea(); // for XML
        // temporaly set text field.
        this.valueLabel = new XBayaLabel("", this.valueTextField);

        this.metadataTextArea = new XBayaTextArea();
        XBayaLabel metadataLabel = new XBayaLabel("Metadata",
                this.metadataTextArea);

        this.gridPanel = new GridPanel();
        this.gridPanel.add(nameLabel);
        this.gridPanel.add(this.nameTextField);
        this.gridPanel.add(descriptionLabel);
        this.gridPanel.add(this.descriptionTextArea);
        this.gridPanel.add(this.valueLabel);
        this.gridPanel.add(this.valueTextField);
        this.gridPanel.add(metadataLabel);
        this.gridPanel.add(this.metadataTextArea);
        this.gridPanel.layout(4, 2, 3, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            @Override
			public void actionPerformed(ActionEvent e) {
                setInput();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            @Override
			public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.xbayaGUI,
                "Input Parameter Configuration", this.gridPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

}