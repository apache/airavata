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

package org.apache.airavata.xbaya.ui.dialogs.amazon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.core.amazon.AmazonCredential;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

public class ChangeCredentialWindow {
    private XBayaEngine engine;
    private XBayaDialog dialog;
    private XBayaTextField accessKeyIDTextField;
    private XBayaTextField secretAccessKeyTextField;

    private JDialog owner;

    /**
     * Constructs a ChangeCredentialWindow.
     * 
     * @param owner
     */
    public ChangeCredentialWindow(JDialog owner) {
        this.owner = owner;
        initGUI();
    }

    /**
     * 
     * Constructs a ChangeCredentialWindow.
     * 
     * @param engine
     */
    public ChangeCredentialWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    protected void initGUI() {

        this.accessKeyIDTextField = new XBayaTextField();
        XBayaLabel accessKeyIDLabel = new XBayaLabel("Access Key", this.accessKeyIDTextField);

        this.secretAccessKeyTextField = new XBayaTextField();
        XBayaLabel secretAccessKeyLabel = new XBayaLabel("Secret Access Key", this.secretAccessKeyTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(accessKeyIDLabel);
        infoPanel.add(this.accessKeyIDTextField);
        infoPanel.add(secretAccessKeyLabel);
        infoPanel.add(this.secretAccessKeyTextField);

        infoPanel.layout(2, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(infoPanel);
        mainPanel.layout(1, 1, GridPanel.WEIGHT_EQUALLY, GridPanel.WEIGHT_EQUALLY);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String accessID = ChangeCredentialWindow.this.accessKeyIDTextField.getText();
                if (accessID != "") {
                    AmazonCredential.getInstance().setAwsAccessKeyId(accessID);
                }
                String secretID = ChangeCredentialWindow.this.secretAccessKeyTextField.getText();
                if (secretID != "") {
                    AmazonCredential.getInstance().setAwsSecretAccessKey(secretID);
                }
                hide();
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

        if (this.owner == null) {
            this.dialog = new XBayaDialog(this.engine.getGUI(), "S3 Toolkits", mainPanel, buttonPanel);
        } else {
            this.dialog = new XBayaDialog(this.owner, "S3 Toolkits", mainPanel, buttonPanel);
        }
    }

    /**
     * hide the dialog (when user clicked on cancel)
     */
    public void hide() {
        this.dialog.hide();
    }

    /**
     * show the dialog
     */
    public void show() {
        if (AmazonCredential.getInstance().getAwsAccessKeyId() != "") {
            ChangeCredentialWindow.this.accessKeyIDTextField
                    .setText(AmazonCredential.getInstance().getAwsAccessKeyId());
        }
        if (AmazonCredential.getInstance().getAwsSecretAccessKey() != "") {
            ChangeCredentialWindow.this.secretAccessKeyTextField.setText(AmazonCredential.getInstance()
                    .getAwsSecretAccessKey());
        }
        this.dialog.show();
    }
}