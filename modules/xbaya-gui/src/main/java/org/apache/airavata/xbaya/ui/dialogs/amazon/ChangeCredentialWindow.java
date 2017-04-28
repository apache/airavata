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
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;

public class ChangeCredentialWindow {
    private XBayaEngine engine;
    private XBayaDialog dialog;
    private XBayaTextField accessKeyIDTextField;
    private XBayaTextField secretAccessKeyTextField;

    private JDialog owner;

    /**
     * Constructs a ChangeCredentialWindow.
     *
     * @param engine XBayaEngine
     */
    public ChangeCredentialWindow(XBayaEngine engine) {
        this.engine = engine;
        initGUI();
    }

    public ChangeCredentialWindow(JDialog owner) {
        this.owner = owner;
        initGUI();
    }

    protected void initGUI() {
        this.accessKeyIDTextField = new XBayaTextField();
        XBayaLabel accessKeyIDLabel = new XBayaLabel("Access Key", this.accessKeyIDTextField);

        this.secretAccessKeyTextField = new XBayaTextField();
        XBayaLabel secretAccessKeyLabel = new XBayaLabel("Secret Key", this.secretAccessKeyTextField);

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
                if (!"".equals(accessID)) {
                    String secretID = ChangeCredentialWindow.this.secretAccessKeyTextField.getText();

                    if (!"".equals(secretID)) {
                        AmazonCredential.getInstance().setAwsAccessKeyId(accessID);
                        AmazonCredential.getInstance().setAwsSecretAccessKey(secretID);
                        hide();
                        return;
                    }
                }

                JOptionPane.showMessageDialog(dialog.getDialog(),"SecretKey and AccessKey can not be empty!");
            }

        });

        JButton generateButton = new JButton("Generate Key Pair");
        generateButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
               /* String accessID = ChangeCredentialWindow.this.accessKeyIDTextField.getText();
                if (!"".equals(accessID)) {
                    String secretID = ChangeCredentialWindow.this.secretAccessKeyTextField.getText();

                    if (!"".equals(secretID)) {
                        AmazonCredential.getInstance().setAwsAccessKeyId(accessID);
                        AmazonCredential.getInstance().setAwsSecretAccessKey(secretID);

                        File file = new File(System.getProperty("user.home") + "/.ssh/" + EC2Provider.KEY_PAIR_NAME);

                        if (file.exists()) {
                            ChangeCredentialWindow.this.engine.getGUI().getErrorWindow().
                                    info(ChangeCredentialWindow.this.dialog.getDialog(),
                                    "Warning", "The file " + file.getAbsolutePath() + " exists.");

                        } else {
                            AWSCredentials credential =
                                    new BasicAWSCredentials(accessID, secretID);
                            AmazonEC2Client ec2client = new AmazonEC2Client(credential);

                            try {
                                EC2ProviderUtil.buildKeyPair(ec2client, EC2Provider.KEY_PAIR_NAME);
                            } catch (NoSuchAlgorithmException e1) {
                                ChangeCredentialWindow.this.engine.getGUI().getErrorWindow().
                                        info(ChangeCredentialWindow.this.dialog.getDialog(),
                                        "Warning", e1.getMessage());
                            } catch (InvalidKeySpecException e1) {
                                ChangeCredentialWindow.this.engine.getGUI().getErrorWindow().
                                        info(ChangeCredentialWindow.this.dialog.getDialog(),
                                        "Warning", e1.getMessage());
                            } catch (IOException e1) {
                                ChangeCredentialWindow.this.engine.getGUI().getErrorWindow().
                                        info(ChangeCredentialWindow.this.dialog.getDialog(),
                                        "Warning", e1.getMessage());
                            }

                            JOptionPane.showMessageDialog(dialog.getDialog(),"The key " +
                                    file.getAbsolutePath() + " generated.");
                        }

                        hide();
                        return;
                    }
                }*/

                JOptionPane.showMessageDialog(dialog.getDialog(),"SecretKey and AccessKey can not be empty!");
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
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);

        if (this.owner == null) {
            this.dialog = new XBayaDialog(this.engine.getGUI(), "Security Credentials", mainPanel, buttonPanel);
        } else {
            this.dialog = new XBayaDialog(this.owner, "Security Credentials", mainPanel, buttonPanel);
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
        if (!"".equals(AmazonCredential.getInstance().getAwsAccessKeyId())) {
            ChangeCredentialWindow.this.accessKeyIDTextField
                    .setText(AmazonCredential.getInstance().getAwsAccessKeyId());
        }
        if (!"".equals(AmazonCredential.getInstance().getAwsSecretAccessKey())) {
            ChangeCredentialWindow.this.secretAccessKeyTextField.setText(AmazonCredential.getInstance()
                    .getAwsSecretAccessKey());
        }
        this.dialog.show();
    }
}