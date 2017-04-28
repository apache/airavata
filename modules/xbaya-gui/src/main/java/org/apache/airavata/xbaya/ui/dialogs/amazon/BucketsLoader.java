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

import org.apache.airavata.xbaya.ui.XBayaGUI;
import org.apache.airavata.xbaya.ui.dialogs.WaitDialog;
import org.apache.airavata.xbaya.ui.utils.Cancelable;
import org.apache.airavata.xbaya.ui.widgets.amazon.S3Tree;
import org.apache.airavata.xbaya.ui.widgets.amazon.S3TreeModel;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class BucketsLoader implements Cancelable {
    private XBayaGUI xbayaGUI;
    private JDialog parent;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a BucketsLoader.
     *
     * @param xbayaGUI XBayaGUI
     * @param parent JDialog
     */
    public BucketsLoader(XBayaGUI xbayaGUI, JDialog parent) {
        this.xbayaGUI=xbayaGUI;
        this.parent = parent;
        this.loadingDialog = new WaitDialog(this, "Loading S3 Buckets.", "Loading S3 Buckets.\n"
                + "Please wait for a moment.", this.xbayaGUI);
    }

    /**
     * @see org.apache.airavata.xbaya.ui.utils.Cancelable#cancel()
     */
    @Override
    public void cancel() {
        this.canceled = true;
    }

    /**
     * 
     * @param s3 S3Service
     * @param s3Tree S3Tree
     */
    public void load(final S3Service s3, final S3Tree s3Tree) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                S3Bucket[] bucketArray;
                try {
                    bucketArray = s3.listAllBuckets();
                    for (S3Bucket s3Bucket : bucketArray) {
                        DefaultMutableTreeNode tempTreeNode = s3Tree.addObject((DefaultMutableTreeNode) null,
                                s3Bucket.getName());

                        if (BucketsLoader.this.canceled)
                            return;

                        S3Object[] s3ObjectArray = s3.listObjects(s3Bucket.getName());
                        for (S3Object s3Object : s3ObjectArray) {
                            String keyName = s3Object.getName();
                            if (keyName.contains("$")) {
                                keyName = keyName.substring(0, keyName.indexOf('_'));
                            }
                            s3Tree.addObject(tempTreeNode, keyName);
                        }
                    }

                    s3Tree.refresh();

                    if (bucketArray.length == 0) {
                        JOptionPane.showMessageDialog(BucketsLoader.this.parent, "Connection Failed!", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(BucketsLoader.this.parent, "Connection Successfully!", "Info",
                            JOptionPane.INFORMATION_MESSAGE);

                    // already connect
                    S3TreeModel.getInstance().connect();

                } catch (S3ServiceException ex) {
                    BucketsLoader.this.xbayaGUI.getErrorWindow().error(BucketsLoader.this.parent,
                            "Cannot List S3 buckets", ex);
                } finally {
                    BucketsLoader.this.loadingDialog.hide();
                }
            }
        }).start();

        this.loadingDialog.show();
    }

}