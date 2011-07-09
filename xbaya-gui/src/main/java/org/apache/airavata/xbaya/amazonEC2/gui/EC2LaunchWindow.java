/*
 * Copyright (c) 2011 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.amazonEC2.gui;

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComboBox;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.util.AmazonUtil;

/**
 * @author Patanachai Tangchaisin
 */
public class EC2LaunchWindow {

    private XBayaEngine engine;
    private XBayaDialog dialog;

    private XBayaTextField amiTextField;
    private JSpinner numberOfInstanceSpinner;
    private XBayaComboBox instanceTypeComboBox;
    private XBayaComboBox keyComboBox;
    private JRadioButton existKeyButton;
    private JRadioButton noKeyButton;

    private ComboBoxModel keyComboBoxModel;

    /**
     * 
     * Constructs a EC2LaunchWindow.
     * 
     * @param engine
     * @param ec2
     */
    public EC2LaunchWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    private void initGUI() {
        /*
         * Main Panel
         */
        this.amiTextField = new XBayaTextField();
        XBayaLabel amiLabel = new XBayaLabel("AMI ID", this.amiTextField);

        this.numberOfInstanceSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        XBayaLabel nInstanceLabel = new XBayaLabel("Number Of Instances", this.numberOfInstanceSpinner);

        this.instanceTypeComboBox = new XBayaComboBox(new DefaultComboBoxModel(AmazonUtil.INSTANCE_TYPE));
        this.instanceTypeComboBox.setSelectedItem(AmazonUtil.INSTANCE_TYPE[1]);
        XBayaLabel instanceTypeLabel = new XBayaLabel("Instance Type", this.instanceTypeComboBox);

        this.noKeyButton = new JRadioButton("No Key Pair");
        this.noKeyButton.setSelected(true);
        this.noKeyButton.addActionListener(new ActionListener() {
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
        serviceTypeButtonGroup.add(this.noKeyButton);
        serviceTypeButtonGroup.add(this.existKeyButton);

        this.keyComboBox = new XBayaComboBox(new DefaultComboBoxModel());
        this.keyComboBox.getJComboBox().setEnabled(false);

        GridPanel radioPanel = new GridPanel();
        radioPanel.add(this.noKeyButton);
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

        /*
         * Button Panel
         */
        JButton lunchButton = new JButton("Launch");
        lunchButton.addActionListener(new AbstractAction() {
            private Object numberOfInstanceSpinner;

            @Override
            public void actionPerformed(ActionEvent e) {
                // validation
                if (EC2LaunchWindow.this.amiTextField.getText() == null
                        || EC2LaunchWindow.this.amiTextField.getText().isEmpty()
                        || ((Integer) EC2LaunchWindow.this.numberOfInstanceSpinner.getValue()).intValue() <= 0) {
                    EC2LaunchWindow.this.engine.getErrorWindow().info(EC2LaunchWindow.this.dialog.getDialog(),
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
                    EC2LaunchWindow.this.engine.getErrorWindow().info(EC2LaunchWindow.this.dialog.getDialog(),
                            "Warning", "Number of Instances is not numeric");
                } catch (Exception ex) {
                    EC2LaunchWindow.this.engine.getErrorWindow().error(EC2LaunchWindow.this.dialog.getDialog(),
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

        this.dialog = new XBayaDialog(this.engine, "Amazon EC2 Launcher", mainPanel, buttonPanel);
    }

    /**
	 * 
	 */
    public void hide() {
        this.dialog.hide();
    }

    /**
	 * 
	 */
    public void show() {
        this.dialog.show();
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2011 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
