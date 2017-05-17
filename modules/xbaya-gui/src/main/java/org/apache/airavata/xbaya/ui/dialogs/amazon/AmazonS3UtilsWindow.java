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
import org.apache.airavata.xbaya.ui.widgets.amazon.S3Tree;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.io.File;

public class AmazonS3UtilsWindow {
    private XBayaDialog dialog;

    private static XBayaEngine xBayaEngine;

    private static AmazonS3UtilsWindow window;

    private XBayaTextField fileTextField;
    private XBayaTextField uploadBucketTextField;
    private XBayaTextField downloadBucketTextField;
    private XBayaTextField keyTextField;
    private XBayaTextField folderTextField;

    private S3Tree s3Tree;

    /**
     * Constructs a AmazonS3UtilsWindow.
     *
     * @param engine XBayaEngine
     */
    private AmazonS3UtilsWindow(XBayaEngine engine) {
        xBayaEngine = engine;
        initGUI();
    }

    /**
     * getErrorWindow
     *
     * @param engine XBayaEngine
     */
    public static AmazonS3UtilsWindow getInstance(XBayaEngine engine) {
        if (window == null) {
            window = new AmazonS3UtilsWindow(engine);
        } else if (xBayaEngine != engine) {
            window = new AmazonS3UtilsWindow(engine);
        }
        return window;
    }

    /**
     * Get S3 Service
     *
     * @return S3Service
     */
    private S3Service getS3Service() {
        S3Service s3Service = null;
        try {
            s3Service = new RestS3Service(new AWSCredentials(AmazonCredential.getInstance().getAwsAccessKeyId(),
                    AmazonCredential.getInstance().getAwsSecretAccessKey()));
        } catch (S3ServiceException s3ex) {
            xBayaEngine.getGUI().getErrorWindow().error(s3ex);
        }
        return s3Service;
    }

    @SuppressWarnings("serial")
    protected void initGUI() {

        /* Upload Panel */
        this.fileTextField = new XBayaTextField();
        XBayaLabel fileLabel = new XBayaLabel("Upload File Path", this.fileTextField);

        this.uploadBucketTextField = new XBayaTextField();
        XBayaLabel uploadBucketLabel = new XBayaLabel("Bucket Name", this.uploadBucketTextField);

        GridPanel uploadPanel = new GridPanel();
        uploadPanel.getSwingComponent().setBorder(BorderFactory.createTitledBorder("Upload"));
        uploadPanel.add(fileLabel);
        uploadPanel.add(this.fileTextField);
        uploadPanel.add(uploadBucketLabel);
        uploadPanel.add(this.uploadBucketTextField);
        uploadPanel.layout(2, 2, GridPanel.WEIGHT_NONE, 1);

        /* Download Panel */
        if (AmazonCredential.getInstance().getAwsAccessKeyId().equals("AKIAI3GNMQVYA5LSQNEQ")) {
            // Avoid to use default Aws Access Key
            JOptionPane.showMessageDialog(AmazonS3UtilsWindow.this.dialog.getDialog(), "Aws Access Key not set!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.downloadBucketTextField = new XBayaTextField();
        XBayaLabel downloadBucketLabel = new XBayaLabel("Bucket Name", this.downloadBucketTextField);

        this.keyTextField = new XBayaTextField();
        XBayaLabel keyLabel = new XBayaLabel("Key Name", this.keyTextField);

        this.folderTextField = new XBayaTextField();
        XBayaLabel folderLabel = new XBayaLabel("Download Location", this.folderTextField);

        GridPanel downloadPanel = new GridPanel();
        downloadPanel.getSwingComponent().setBorder(BorderFactory.createTitledBorder("Download"));
        downloadPanel.add(downloadBucketLabel);
        downloadPanel.add(this.downloadBucketTextField);
        downloadPanel.add(keyLabel);
        downloadPanel.add(this.keyTextField);
        downloadPanel.add(folderLabel);
        downloadPanel.add(this.folderTextField);
        downloadPanel.layout(3, 2, GridPanel.WEIGHT_NONE, 1);

        /* Button Panel */
        JButton refreshButton = new JButton("Connect/Refresh");
        refreshButton.addActionListener(new AbstractAction() {

            private ChangeCredentialWindow credentialWindow;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (AmazonCredential.getInstance().getAwsAccessKeyId().isEmpty()
                        || AmazonCredential.getInstance().getAwsSecretAccessKey().isEmpty()) {
                    JOptionPane.showMessageDialog(AmazonS3UtilsWindow.this.dialog.getDialog(),
                            "Aws Access Key not set!", "Error", JOptionPane.ERROR_MESSAGE);

                    if (this.credentialWindow == null) {
                        this.credentialWindow = new ChangeCredentialWindow(AmazonS3UtilsWindow.this.dialog.getDialog());
                    }
                    try {
                        this.credentialWindow.show();
                    } catch (Exception e1) {
                        xBayaEngine.getGUI().getErrorWindow().error(e1);
                    }

                    return;
                }
                AmazonS3UtilsWindow.this.s3Tree.clean();
                BucketsLoader bucketsLoader = new BucketsLoader(xBayaEngine.getGUI(), window.dialog.getDialog());
                bucketsLoader.load(getS3Service(), AmazonS3UtilsWindow.this.s3Tree);
            }
        });

        JButton uploadButton = new JButton("Upload");
        uploadButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((window.fileTextField.getText().length() != 0)
                        && (window.uploadBucketTextField.getText().length() != 0)) {
                    S3Uploader s3Uploader = new S3Uploader(xBayaEngine, window.dialog.getDialog());
                    s3Uploader.upload(getS3Service(), AmazonS3UtilsWindow.this.s3Tree,
                            window.uploadBucketTextField.getText(), window.fileTextField.getText());

                    window.fileTextField.setText("");
                    window.folderTextField.setText("");
                } else {
                    xBayaEngine.getGUI().getErrorWindow().error(window.dialog.getDialog(),
                            "Please give input to every upload fields");
                }
            }
        });

        JButton downloadButton = new JButton("Download");
        downloadButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((window.downloadBucketTextField.getText().length() != 0)
                        && (window.keyTextField.getText().length() != 0)
                        && (window.folderTextField.getText().length() != 0)) {
                    S3Downloader s3Downloader = new S3Downloader(xBayaEngine, window.dialog.getDialog());
                    s3Downloader.download(getS3Service(), window.downloadBucketTextField.getText(),
                            window.keyTextField.getText(), window.folderTextField.getText());

                    window.downloadBucketTextField.setText("");
                    window.keyTextField.setText("");
                    window.folderTextField.setText("");

                } else {
                    xBayaEngine.getGUI().getErrorWindow().error(window.dialog.getDialog(),
                            "Please give input to every download fields");
                }
            }
        });

        JButton fileButton = new JButton("Choose File & Flolder");
        fileButton.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int returnVal = fc.showOpenDialog(AmazonS3UtilsWindow.this.dialog.getDialog());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String filePath = fc.getSelectedFile().getAbsolutePath();
                    File file = fc.getSelectedFile();
                    if (file.isFile()) {
                        window.fileTextField.setText(filePath);
                        window.folderTextField.setText("");
                    } else if (file.isDirectory()) {
                        window.folderTextField.setText(filePath);
                        window.fileTextField.setText("");
                    }
                }
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
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(fileButton);
        buttonPanel.add(cancelButton);

        /* Main Panel */
        GridPanel mainPanel = new GridPanel(true);
        this.s3Tree = new S3Tree();
        mainPanel.add(new JScrollPane(this.s3Tree));
        mainPanel.add(uploadPanel);
        mainPanel.add(downloadPanel);
        mainPanel.layout(3, 1, 0, GridPanel.WEIGHT_EQUALLY);

        this.s3Tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = AmazonS3UtilsWindow.this.s3Tree.getSelectedNode();

                if (node == null)
                    return;

                Object nodeInfo = node.getUserObject();
                String bucketName;
                String downloadPanelBucketName = "";
                if (node.isLeaf() && node.getParent() != null) { // Node is probably a key
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
                    bucketName = (String) parentNode.getUserObject();
                    if (!bucketName.equals("S3 Contents")) { // Node is indeed a key
                        downloadPanelBucketName = (String) parentNode.getUserObject();
                        String currentNodeName = (String) node.getUserObject();
                        int index = currentNodeName.lastIndexOf('/');
                        index = index >= 0 ? index : 0;
                        if (index > 0) {
                            bucketName = bucketName + "/" + currentNodeName.substring(0, index);
                        }
                        String keyName = (String) nodeInfo;
                        window.keyTextField.setText(keyName);
                    } // Node is a bucket
                    else {
                        bucketName = (String) nodeInfo;
                        window.keyTextField.setText("");
                    }
                } else { // Node is a bucket
                    bucketName = (String) nodeInfo;
                    window.keyTextField.setText("");
                }

                window.uploadBucketTextField.setText(bucketName);
                window.downloadBucketTextField.setText(downloadPanelBucketName);
            }
        });

        this.dialog = new XBayaDialog(xBayaEngine.getGUI(), "Amazon S3 Upload/Download Tool", mainPanel, buttonPanel);

    }

    public void hide() {
        this.dialog.hide();
    }

    public void show() {
        this.dialog.show();
    }
}