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
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.airavata.workflow.model.graph.system.S3InputNode;
import org.apache.airavata.xbaya.core.amazon.AmazonCredential;
import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.XBayaDialog;
import org.apache.airavata.xbaya.ui.dialogs.amazon.BucketsLoader;
import org.apache.airavata.xbaya.ui.dialogs.amazon.ChangeCredentialWindow;
import org.apache.airavata.xbaya.ui.widgets.amazon.S3Tree;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

public class S3FileChooser implements TreeSelectionListener {

    private XBayaDialog dialog;
    private XBayaGUI xbayaGUI;
    protected S3InputNode inputNode;
    private String chosenFile;

    private S3Tree s3Tree;

    /**
     * 
     * Constructs a S3FileChooser.
     * 
     * @param engine
     * @param inputNode
     */
    public S3FileChooser(XBayaGUI xbayaGUI, S3InputNode inputNode) {
        this.xbayaGUI=xbayaGUI;
        this.inputNode = inputNode;
        initGUI();
    }

    private void initGUI() {

        /*
         * ScrollPane for S3 Tree
         */
        // add tree listener to this
        this.s3Tree = new S3Tree();
        this.s3Tree.addTreeSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(this.s3Tree);

        /*
         * Button Panel
         */
        JButton refreshButton = new JButton("Connect/Refresh");
        refreshButton.addActionListener(new AbstractAction() {

            private ChangeCredentialWindow credentialWindow;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (AmazonCredential.getInstance().getAwsAccessKeyId().isEmpty()
                        || AmazonCredential.getInstance().getAwsSecretAccessKey().isEmpty()) {
                    S3FileChooser.this.xbayaGUI.getErrorWindow().warning(S3FileChooser.this.dialog.getDialog(), "Error",
                            "Aws Access Key not set!");

                    if (this.credentialWindow == null) {
                        this.credentialWindow = new ChangeCredentialWindow(S3FileChooser.this.dialog.getDialog());
                    }
                    try {
                        this.credentialWindow.show();
                    } catch (Exception e1) {
                        S3FileChooser.this.xbayaGUI.getErrorWindow().error(e1);
                    }

                    return;
                }
                S3FileChooser.this.s3Tree.clean();

                try {

                    // create S3Service
                    S3Service s3Service = new RestS3Service(new AWSCredentials(AmazonCredential.getInstance()
                            .getAwsAccessKeyId(), AmazonCredential.getInstance().getAwsSecretAccessKey()));

                    BucketsLoader bucketsLoader = new BucketsLoader(S3FileChooser.this.xbayaGUI,
                            S3FileChooser.this.dialog.getDialog());
                    bucketsLoader.load(s3Service, S3FileChooser.this.s3Tree);

                } catch (S3ServiceException s3ex) {
                    S3FileChooser.this.xbayaGUI.getErrorWindow().error(s3ex);
                }
            }
        });

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (S3FileChooser.this.chosenFile != null) {
                    S3FileChooser.this.inputNode.setDefaultValue(new String(S3FileChooser.this.chosenFile));
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
        buttonPanel.add(refreshButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.xbayaGUI, "Amazon S3 Input Chooser", scrollPane, buttonPanel);
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

    /**
     * 
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = s3Tree.getSelectedNode();

        if (node == null) {
            this.chosenFile = null;
            return;
        }

        Object nodeInfo = node.getUserObject();
        String bucketName = "";
        if (node.isLeaf()) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
            bucketName = (String) parentNode.getUserObject();
            String keyName = (String) nodeInfo;
            this.chosenFile = "s3n://" + bucketName + "/" + keyName;

        } else {
            bucketName = (String) nodeInfo;
            this.chosenFile = "s3n://" + bucketName;
        }

    }
}