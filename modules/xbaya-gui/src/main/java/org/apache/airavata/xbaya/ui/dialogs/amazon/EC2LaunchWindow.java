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
package org.apache.airavata.xbaya.ui.dialogs.amazon;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaComboBox;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.util.AmazonUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EC2LaunchWindow {
    private XBayaEngine engine;
    private XBayaDialog dialog;
    private XBayaTextField amiTextField;
    private JSpinner numberOfInstanceSpinner;
    private XBayaComboBox instanceTypeComboBox;
    private XBayaComboBox keyComboBox;
    private JRadioButton existKeyButton;
    private ComboBoxModel keyComboBoxModel;

    /**
     * Constructs a EC2LaunchWindow.
     * 
     * @param engine XBayaEngine
     */
    public EC2LaunchWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private void initGUI() {
        /* Main Panel */
        this.amiTextField = new XBayaTextField();
        XBayaLabel amiLabel = new XBayaLabel("AMI ID", this.amiTextField);

        this.numberOfInstanceSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        XBayaLabel nInstanceLabel = new XBayaLabel("Number Of Instances", this.numberOfInstanceSpinner);

        this.instanceTypeComboBox = new XBayaComboBox(new DefaultComboBoxModel(AmazonUtil.INSTANCE_TYPE));
        this.instanceTypeComboBox.setSelectedItem(AmazonUtil.INSTANCE_TYPE[1]);
        XBayaLabel instanceTypeLabel = new XBayaLabel("Instance Type", this.instanceTypeComboBox);

        JRadioButton noKeyButton = new JRadioButton("No Key Pair");
        noKeyButton.setSelected(true);
        noKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                EC2LaunchWindow.this.keyComboBox.getJComboBox().setEnabled(false);
            }
        });

        this.existKeyButton = new JRadioButton("Exist Key Pairs");
        this.existKeyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                if (EC2LaunchWindow.this.keyComboBoxModel == null) {
                    EC2LaunchWindow.this.keyComboBoxModel = new DefaultComboBoxModel(AmazonUtil.loadKeypairs()
                            .toArray());
                    EC2LaunchWindow.this.keyComboBox.setModel(EC2LaunchWindow.this.keyComboBoxModel);
                }
                EC2LaunchWindow.this.keyComboBox.getJComboBox().setEnabled(true);
            }
        });

        ButtonGroup serviceTypeButtonGroup = new ButtonGroup();
        serviceTypeButtonGroup.add(noKeyButton);
        serviceTypeButtonGroup.add(this.existKeyButton);

        this.keyComboBox = new XBayaComboBox(new DefaultComboBoxModel());
        this.keyComboBox.getJComboBox().setEnabled(false);

        GridPanel radioPanel = new GridPanel();
        radioPanel.add(noKeyButton);
        radioPanel.add(new JPanel());
        radioPanel.add(this.existKeyButton);
        radioPanel.add(this.keyComboBox);
        radioPanel.layout(2, 2, 0, 1);

        XBayaLabel keyLabel = new XBayaLabel("Key Pair", radioPanel);

        GridPanel mainPanel = new GridPanel(true);
        mainPanel.add(amiLabel);
        mainPanel.add(this.amiTextField);
        mainPanel.add(nInstanceLabel);
        mainPanel.add(this.numberOfInstanceSpinner);
        mainPanel.add(instanceTypeLabel);
        mainPanel.add(this.instanceTypeComboBox);
        mainPanel.add(keyLabel);
        mainPanel.add(radioPanel);
        mainPanel.layout(4, 2, 0, GridPanel.WEIGHT_EQUALLY);

        /* Button Panel */
        JButton lunchButton = new JButton("Launch");
        lunchButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // validation
                if (EC2LaunchWindow.this.amiTextField.getText() == null
                        || EC2LaunchWindow.this.amiTextField.getText().isEmpty()
                        || (Integer) EC2LaunchWindow.this.numberOfInstanceSpinner.getValue() <= 0) {
                    EC2LaunchWindow.this.engine.getGUI().getErrorWindow().info(EC2LaunchWindow.this.dialog.getDialog(),
                            "Warning", "Please input all fields");
                    return;
                }

                try {
                    // get all data
                    String ami = EC2LaunchWindow.this.amiTextField.getText();
                    String instanceType = EC2LaunchWindow.this.instanceTypeComboBox.getText();
                    Integer n = (Integer) EC2LaunchWindow.this.numberOfInstanceSpinner.getValue();

                    // use exist key pair
                    if (EC2LaunchWindow.this.existKeyButton.isSelected()) {
                        String keyname = EC2LaunchWindow.this.keyComboBox.getText();
                        AmazonUtil.launchInstance(ami, instanceType, n, keyname);
                    } else {
                        AmazonUtil.launchInstance(ami, instanceType, n);
                    }

                    EC2LaunchWindow.this.hide();

                } catch (NumberFormatException nfe) {
                    EC2LaunchWindow.this.engine.getGUI().getErrorWindow().info(EC2LaunchWindow.this.dialog.getDialog(),
                            "Warning", "Number of Instances is not numeric");
                } catch (Exception ex) {
                    EC2LaunchWindow.this.engine.getGUI().getErrorWindow().error(EC2LaunchWindow.this.dialog.getDialog(),
                            "Cannot start EC2 instances: " + ex.getMessage(), ex);
                }
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EC2LaunchWindow.this.hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(lunchButton);
        buttonPanel.add(closeButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Amazon EC2 Launcher", mainPanel, buttonPanel);
    }

    public void hide() {
        this.dialog.hide();
    }

    public void show() {
        this.dialog.show();
    }
}