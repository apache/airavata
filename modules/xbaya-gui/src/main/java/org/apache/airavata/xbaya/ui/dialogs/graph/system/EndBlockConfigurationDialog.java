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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.system.EndBlockNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class EndBlockConfigurationDialog {

    private XBayaGUI xbayaGUI;

    private EndBlockNode node;

    private XBayaDialog dialog;

    private XBayaTextField nameTextField;

    private XBayaTextField idTextField;

    private JSpinner numPorts;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public EndBlockConfigurationDialog(EndBlockNode node, XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    @SuppressWarnings("boxing")
    public void show() {
        String name = this.node.getName();
        this.nameTextField.setText(name);
        this.idTextField.setText(this.node.getID());
        int number = this.node.getOutputPorts().size();
        this.numPorts.setValue(number);

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    private void setInput() {
        Integer value = (Integer) this.numPorts.getValue();
        int number = value.intValue();
        int current = this.node.getOutputPorts().size();
        try {
            if (number > current) {
                // Add ports
                for (int i = 0; i < number - current; i++) {
                    this.node.addOutputPort();
                    this.node.addInputPort();
                    this.node.addInputPort();
                }
            } else if (number < current) {
                for (int i = 0; i < current - number; i++) {
                    this.node.removeOutputPort();
                    this.node.removeInputPort();
                    this.node.removeInputPort();
                }
            } else {
                // Do nothing.
            }
        } catch (GraphException e) {
            this.xbayaGUI.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
        }

        hide();
        this.xbayaGUI.getGraphCanvas().repaint();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {
        this.nameTextField = new XBayaTextField();
        this.nameTextField.setEditable(false);
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);

        this.idTextField = new XBayaTextField();
        this.idTextField.setEditable(false);
        XBayaLabel idLabel = new XBayaLabel("ID", this.idTextField);

        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        this.numPorts = new JSpinner(model);
        XBayaLabel numPortLabel = new XBayaLabel("Number of Parameters", this.numPorts);

        GridPanel gridPanel = new GridPanel();
        gridPanel.add(nameLabel);
        gridPanel.add(this.nameTextField);
        gridPanel.add(idLabel);
        gridPanel.add(this.idTextField);
        gridPanel.add(numPortLabel);
        gridPanel.add(this.numPorts);
        gridPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);

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

        this.dialog = new XBayaDialog(this.xbayaGUI, "Endif Configuration", gridPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}