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

import java.io.File;

import javax.swing.JDialog;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Object;

/**
 * @author Patanachai Tangchaisin
 */
public class S3Uploader implements Cancelable {
    private XBayaEngine engine;
    private JDialog parent;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a S3Uploader.
     * 
     * @param engine
     * @param parent
     */
    public S3Uploader(XBayaEngine engine, JDialog parent) {
        this.engine = engine;
        this.parent = parent;
        this.loadingDialog = new WaitDialog(this, "Uploading file to S3.", "Uploading file to S3.\n"
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
     * @param s3tree
     * @param bucket
     * @param filePath
     */
    public void upload(final S3Service s3, final S3Tree s3tree, final String bucket, final String filePath) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                int index;
                index = filePath.lastIndexOf('/');
                String fileName;
                if (index == -1) {
                    index = filePath.lastIndexOf('\\');
                }
                fileName = filePath.substring(index + 1, filePath.length());
                System.out.println(filePath);
                try {
                    S3Object s3Object = new S3Object(new File(filePath));
                    s3.putObject(bucket, s3Object);

                    /*
                     * We cannot cancel during upload, so delete file instead
                     */
                    if (S3Uploader.this.canceled) {
                        s3.deleteObject(bucket, s3Object.getKey());
                    } else {

                        S3Uploader.this.engine.getErrorWindow().info(S3Uploader.this.parent, "",
                                "Uploaded successfully!");

                        // add key to S3Tree
                        String uploadString = bucket;
                        int startIndex = uploadString.lastIndexOf('/');
                        startIndex = startIndex >= 0 ? startIndex : 0;
                        if (startIndex != 0) {
                            fileName = uploadString.substring(startIndex) + '/' + fileName;
                        }

                        if (fileName.startsWith("/")) {
                            fileName = fileName.substring(1, fileName.length());
                        }

                        s3tree.addObject(bucket, fileName);
                    }

                } catch (Exception ex) {
                    S3Uploader.this.engine.getErrorWindow().error(S3Uploader.this.parent,
                            "Upload failed! Please ensure every fields are filled correctly", ex);
                } finally {
                    // close loading dialog
                    S3Uploader.this.loadingDialog.hide();
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
