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
import org.apache.airavata.xbaya.core.amazon.AmazonCredential;
import org.apache.airavata.xbaya.core.amazon.EC2InstanceResult;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XbayaEnhancedList;
import org.apache.airavata.xbaya.util.AmazonUtil;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class EC2InstancesManagementWindow {
    private XBayaEngine engine;
    private XBayaDialog dialog;
    private XbayaEnhancedList<EC2InstanceResult> list;
    private ChangeCredentialWindow credentialWindow;

    /**
     * Constructs a EC2InstancesManagementWindow.
     * 
     * @param engine XBayaEngine
     */
    public EC2InstancesManagementWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    public void show() {
        this.dialog.show();
    }

    public void hide() {
        this.dialog.hide();
    }

    private void initGUI() {
        this.list = new XbayaEnhancedList<EC2InstanceResult>();

        GridPanel mainPanel = new GridPanel();
        TitledBorder border = new TitledBorder(new EtchedBorder(), "My Instances");
        mainPanel.getSwingComponent().setBorder(border);
        mainPanel.add(this.list);
        mainPanel.layout(1, 1, 0, 0);

        /* Connect/Refresh Button */
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                /* Check if Credential is already set or not */
                if (credentialSet()) {
                    InstancesLoader instancesLoader = new InstancesLoader(EC2InstancesManagementWindow.this.engine,
                            EC2InstancesManagementWindow.this.dialog.getDialog());
                    instancesLoader.load(EC2InstancesManagementWindow.this.list);
                }
            }
        });

        /* Launch Instance Button */
        JButton launchButton = new JButton("Launch");
        launchButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (credentialSet()) {
                    EC2LaunchWindow ec2LaunchWindow = new EC2LaunchWindow(EC2InstancesManagementWindow.this.engine);
                    ec2LaunchWindow.show();
                }
            }
        });

        /* Terminate Instance */
        JButton terminateButton = new JButton("Terminate");
        terminateButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<EC2InstanceResult> selected = EC2InstancesManagementWindow.this.list.getSelectedValues();

                if (selected.size() == 0) {
                    EC2InstancesManagementWindow.this.engine.getGUI().getErrorWindow().info(
                            EC2InstancesManagementWindow.this.dialog.getDialog(), "Warning", "No instances selected");
                    return;
                }

                String text = "";
                List<String> requestIds = new ArrayList<String>();

                for (EC2InstanceResult ec2InstancesResult : selected) {
                    requestIds.add(ec2InstancesResult.getInstance().getInstanceId());
                    text += ec2InstancesResult.getInstance().getInstanceId() + ",";
                }

                // confirm from user
                int n = JOptionPane.showConfirmDialog(EC2InstancesManagementWindow.this.dialog.getDialog(),
                        "Are you want to terminate instances:\n" + text, "Terminate Instances",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (n == JOptionPane.YES_OPTION) {
                    // terminate
                    AmazonUtil.terminateInstances(requestIds);

                    // reload
                    InstancesLoader instancesLoader = new InstancesLoader(EC2InstancesManagementWindow.this.engine,
                            EC2InstancesManagementWindow.this.dialog.getDialog());
                    instancesLoader.load(EC2InstancesManagementWindow.this.list);
                }
            }
        });

        /* Close Button */
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EC2InstancesManagementWindow.this.hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(launchButton);
        buttonPanel.add(terminateButton);
        buttonPanel.add(closeButton);

        this.dialog = new XBayaDialog(this.engine.getGUI(), "Amazon EC2 Management Console", mainPanel, buttonPanel);
        int width = 800;
        int height = 500;
        this.dialog.getDialog().setPreferredSize(new Dimension(width, height));
        this.dialog.setDefaultButton(closeButton);

    }

    private boolean credentialSet() {
        if (AmazonCredential.getInstance().getAwsAccessKeyId().isEmpty()
                || AmazonCredential.getInstance().getAwsSecretAccessKey().isEmpty()) {
            EC2InstancesManagementWindow.this.engine.getGUI().getErrorWindow().warning(
                    EC2InstancesManagementWindow.this.dialog.getDialog(), "Error", "Aws Access Key not set!");

            if (this.credentialWindow == null) {
                this.credentialWindow = new ChangeCredentialWindow(EC2InstancesManagementWindow.this.dialog.getDialog());
            }
            try {
                this.credentialWindow.show();
                return false;
            } catch (Exception e1) {
                EC2InstancesManagementWindow.this.engine.getGUI().getErrorWindow().error(e1);
            }
        }
        return true;
    }
}