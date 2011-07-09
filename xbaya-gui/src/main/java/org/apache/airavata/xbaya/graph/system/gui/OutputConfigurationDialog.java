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

package org.apache.airavata.xbaya.graph.system.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.util.WSConstants;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.infoset.XmlElement;

public class OutputConfigurationDialog {

    private XBayaEngine engine;

    private OutputNode node;

    private XBayaDialog dialog;

    private XBayaTextField nameTextField;

    private XBayaTextArea descriptionTextArea;

    private XBayaTextArea metadataTextArea;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public OutputConfigurationDialog(OutputNode node, XBayaEngine engine) {
        this.engine = engine;
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
    }

    private void setInput() {
        String name = this.nameTextField.getText();
        String description = this.descriptionTextArea.getText();
        String metadataText = this.metadataTextArea.getText();

        if (name.length() == 0) {
            String warning = "The name cannot be empty.";
            this.engine.getErrorWindow().error(warning);
            return;
        }

        XmlElement metadata;
        if (metadataText.length() == 0) {
            metadata = null;
        } else {
            try {
                metadata = XMLUtil.stringToXmlElement(metadataText);
            } catch (RuntimeException e) {
                String warning = "The metadata is ill-formed.";
                this.engine.getErrorWindow().error(warning, e);
                return;
            }
        }

        this.node.setConfigured(true);
        this.node.setConfiguredName(name);
        this.node.setDescription(description);
        this.node.setMetadata(metadata);
        hide();
        this.engine.getGUI().getGraphCanvas().repaint();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.descriptionTextArea = new XBayaTextArea();
        XBayaLabel descriptionLabel = new XBayaLabel("Description", this.descriptionTextArea);

        this.metadataTextArea = new XBayaTextArea();
        XBayaLabel metadataLabel = new XBayaLabel("Metadata", this.metadataTextArea);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(nameLabel);
        mainPanel.add(this.nameTextField);
        mainPanel.add(descriptionLabel);
        mainPanel.add(this.descriptionTextArea);
        mainPanel.add(metadataLabel);
        mainPanel.add(this.metadataTextArea);
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

        this.dialog = new XBayaDialog(this.engine, "Input Parameter Configuration", mainPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}