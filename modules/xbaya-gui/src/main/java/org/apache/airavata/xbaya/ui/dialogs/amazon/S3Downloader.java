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
import org.apache.airavata.xbaya.ui.dialogs.WaitDialog;
import org.apache.airavata.xbaya.ui.utils.Cancelable;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Object;

import javax.swing.*;
import java.io.*;

public class S3Downloader implements Cancelable {

    private XBayaEngine engine;
    private JDialog parent;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a S3Downloader.
     * 
     * @param engine XBayaEngine
     * @param parent JDialog
     */
    public S3Downloader(XBayaEngine engine, JDialog parent) {
        this.engine = engine;
        this.parent = parent;
        this.loadingDialog = new WaitDialog(this, "Downloading file from S3.", "Downloading file from S3.\n"
                + "Please wait for a moment.", this.engine.getGUI());
    }

    /**
     * @see org.apache.airavata.xbaya.ui.utils.Cancelable#cancel()
     */
    @Override
    public void cancel() {
        this.canceled = true;
    }

    /**
     * Download bucket.
     *
     * @param s3 S3Service
     * @param bucket bucket
     * @param key Key
     * @param directory directory
     */
    public void download(final S3Service s3, final String bucket, final String key, final String directory) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                BufferedWriter out = null;
                BufferedReader in = null;
                try {
                    S3Object s3Object = s3.getObject(bucket, key);

                    File fileOut = new File(directory + File.separator + s3Object.getKey());
                    if (!fileOut.getParentFile().exists()) {
                        fileOut.getParentFile().mkdirs();
                    }
                    if (!fileOut.exists()) {
                        fileOut.createNewFile();
                    }

                    out = new BufferedWriter(new FileWriter(fileOut));
                    in = new BufferedReader(new InputStreamReader(s3Object.getDataInputStream()));
                    String data = null;
                    while ((data = in.readLine()) != null) {

                        // stop download and delete file
                        if (S3Downloader.this.canceled) {
                            out.close();
                            fileOut.delete();
                            return;
                        }

                        out.write(data);
                        out.newLine();
                    }

                    S3Downloader.this.engine.getGUI().getErrorWindow().info(S3Downloader.this.parent, "",
                            "Downloaded successfully!");
                } catch (Exception ex) {
                    S3Downloader.this.engine.getGUI().getErrorWindow().error(S3Downloader.this.parent,
                            "Download failed! Please ensure every fields are filled correctly", ex);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException io) {
                            // do nothing
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException io) {
                            // do nothing
                        }
                    }

                    // close loading dialog
                    S3Downloader.this.loadingDialog.hide();
                }
            }

        }).start();

        this.loadingDialog.show();
    }
}