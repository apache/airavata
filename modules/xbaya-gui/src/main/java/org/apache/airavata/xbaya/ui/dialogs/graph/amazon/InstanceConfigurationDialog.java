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
package org.apache.airavata.xbaya.ui.dialogs.graph.amazon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaComboBox;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.AmazonUtil;

public class InstanceConfigurationDialog {
    private XBayaGUI xbayaGUI;

    private InstanceNode node;

    private XBayaDialog dialog;

    private XBayaTextField nameTextField;
    private XBayaTextField idTextField;
    private XBayaTextField usernameTextField;
    private XBayaComboBox instanceTypeComboBox;

    private JRadioButton amiButton;

    private JRadioButton idButton;

    /**
     * Constructs an InputConfigurationWindow.
     * 
     * @param node
     * @param engine
     */
    public InstanceConfigurationDialog(InstanceNode node, XBayaGUI xbayaGUI) {
        this.xbayaGUI=xbayaGUI;
        this.node = node;
        initGui();
    }

    /**
     * Shows the dialog.
     */
    public void show() {

        // set name
        String name = this.node.getName();
        this.nameTextField.setText(name);

        // set Selected
        this.amiButton.setSelected(this.node.isStartNewInstance());
        this.idButton.setSelected(!this.node.isStartNewInstance());

        // instance type
        if (this.node.isStartNewInstance())
            this.instanceTypeComboBox.getJComboBox().setEnabled(true);
        else
            this.instanceTypeComboBox.getJComboBox().setEnabled(false);

        // set Value
        this.idTextField.setText(this.node.getIdAsValue());
        this.usernameTextField.setText(this.node.getUsername());
        if (this.node.getInstanceType() != null)
            this.instanceTypeComboBox.setSelectedItem(this.node.getInstanceType());
        else
            this.instanceTypeComboBox.setSelectedItem(AmazonUtil.INSTANCE_TYPE[1]);

        // show
        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    private void hide() {
        this.dialog.hide();
    }

    /**
     * Initializes the GUI.
     */
    private void initGui() {

        /*
         * Name
         */
        this.nameTextField = new XBayaTextField();
        XBayaLabel nameLabel = new XBayaLabel("Name", this.nameTextField);
        this.nameTextField.setEditable(false);

        /*
         * Radio button
         */
        this.amiButton = new JRadioButton("Start new instance");
        this.amiButton.setSelected(true);
        this.amiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                InstanceConfigurationDialog.this.instanceTypeComboBox.getJComboBox().setEnabled(true);
            }
        });
        this.instanceTypeComboBox = new XBayaComboBox(new DefaultComboBoxModel(AmazonUtil.INSTANCE_TYPE));
        this.instanceTypeComboBox.setSelectedItem(AmazonUtil.INSTANCE_TYPE[1]);
        GridPanel amiPanel = new GridPanel();
        amiPanel.add(this.amiButton);
        amiPanel.add(this.instanceTypeComboBox);
        amiPanel.layout(1, 2, GridPanel.WEIGHT_NONE, 1);

        this.idButton = new JRadioButton("Use existing instance");
        this.idButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                InstanceConfigurationDialog.this.instanceTypeComboBox.getJComboBox().setEnabled(false);
            }
        });
        ButtonGroup serviceTypeButtonGroup = new ButtonGroup();
        serviceTypeButtonGroup.add(this.amiButton);
        serviceTypeButtonGroup.add(this.idButton);
        XBayaLabel radioLabel = new XBayaLabel("Options", this.amiButton);

        /*
         * AMI/Instance ID
         */
        this.idTextField = new XBayaTextField();
        XBayaLabel idLabel = new XBayaLabel("AMI/Instance ID", this.amiButton);

        /*
         * Username
         */
        this.usernameTextField = new XBayaTextField();
        XBayaLabel usernameLabel = new XBayaLabel("Username", this.amiButton);

        GridPanel gridPanel = new GridPanel();
        gridPanel.add(nameLabel);
        gridPanel.add(this.nameTextField);
        gridPanel.add(radioLabel);
        gridPanel.add(amiPanel);
        gridPanel.add(new JPanel());
        gridPanel.add(this.idButton);
        gridPanel.add(idLabel);
        gridPanel.add(this.idTextField);
        gridPanel.add(usernameLabel);
        gridPanel.add(this.usernameTextField);
        gridPanel.layout(5, 2, GridPanel.WEIGHT_NONE, 1);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (InstanceConfigurationDialog.this.idTextField.getText() == null
                        || InstanceConfigurationDialog.this.idTextField.getText().isEmpty()) {
                    InstanceConfigurationDialog.this.xbayaGUI.getErrorWindow()
                            .error("Please input AMI ID or Instance ID");
                    return;
                }
                if (InstanceConfigurationDialog.this.usernameTextField.getText() == null
                        || InstanceConfigurationDialog.this.usernameTextField.getText().isEmpty()) {
                    InstanceConfigurationDialog.this.xbayaGUI.getErrorWindow().error(
                            "Please input username to access instance");
                    return;
                }

                InstanceConfigurationDialog.this.node.setStartNewInstance(InstanceConfigurationDialog.this.amiButton
                        .isSelected());
                if (InstanceConfigurationDialog.this.amiButton.isSelected()) {
                    InstanceConfigurationDialog.this.node.setAmiId(InstanceConfigurationDialog.this.idTextField
                            .getText());
                    InstanceConfigurationDialog.this.node
                            .setInstanceType(InstanceConfigurationDialog.this.instanceTypeComboBox.getText());
                } else {
                    InstanceConfigurationDialog.this.node.setInstanceId(InstanceConfigurationDialog.this.idTextField
                            .getText());
                    InstanceConfigurationDialog.this.node.setInstanceType(null);
                }
                InstanceConfigurationDialog.this.node.setUsername(InstanceConfigurationDialog.this.usernameTextField
                        .getText());
                hide();
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

        this.dialog = new XBayaDialog(this.xbayaGUI, "Instance Configuration", gridPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }
}