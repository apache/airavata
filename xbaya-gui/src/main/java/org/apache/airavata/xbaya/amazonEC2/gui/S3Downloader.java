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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JDialog;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Object;

/**
 * @author Patanachai Tangchaisin
 */
public class S3Downloader implements Cancelable {

    private XBayaEngine engine;
    private JDialog parent;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a S3Downloader.
     * 
     * @param engine
     * @param parent
     */
    public S3Downloader(XBayaEngine engine, JDialog parent) {
        this.engine = engine;
        this.parent = parent;
        this.loadingDialog = new WaitDialog(this, "Downloading file from S3.", "Downloading file from S3.\n"
                + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    @Override
    public void cancel() {
        this.canceled = true;
    }

    /**
     * 
     * @param s3
     * @param bucket
     * @param key
     * @param directory
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
                    if (!fileOut.getParentFile().exists())
                        fileOut.getParentFile().mkdirs();
                    if (!fileOut.exists())
                        fileOut.createNewFile();

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

                    S3Downloader.this.engine.getErrorWindow().info(S3Downloader.this.parent, "",
                            "Downloaded successfully!");
                } catch (Exception ex) {
                    S3Downloader.this.engine.getErrorWindow().error(S3Downloader.this.parent,
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
