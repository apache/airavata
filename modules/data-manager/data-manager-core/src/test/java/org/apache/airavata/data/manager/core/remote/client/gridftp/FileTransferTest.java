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
package org.apache.airavata.data.manager.core.remote.client.gridftp;


import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.airavata.data.manager.core.remote.client.gridftp.myproxy.SecurityContext;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSCredential;
import org.junit.Ignore;

import java.io.*;
import java.net.URI;


@Ignore
public class FileTransferTest extends TestCase {

    private GSSCredential gssCredential;

    private ExecutionContext executionContext;

    private static final Logger log = Logger.getLogger(FileTransferTest.class);


    public void setUp() throws Exception {

        String userName = System.getProperty("myproxy.user");
        String password = System.getProperty("myproxy.password");

        SecurityContext context = null;

        if (userName == null || password == null || userName.trim().equals("") || password.trim().equals("")) {
            log.error("myproxy.user and myproxy.password system properties are not set. Example :- " +
                    "> mvn clean install -Dmyproxy.user=u1 -Dmyproxy.password=xxx");

            Assert.fail("Please set myproxy.user and myproxy.password system properties.");

        } else {
            context = new SecurityContext(userName, password);
        }

        log.info("Using my proxy user name - " + userName);

        BasicConfigurator.configure();
        Logger logger = Logger.getLogger("GridFTPClient");
        Level lev = Level.toLevel("DEBUG");
        logger.setLevel(lev);


        context.login();
        executionContext = new ExecutionContext();


        String targeterp = executionContext.getGridFTPServerDestination();
        String remoteDestFile = executionContext.getDestinationDataLocation();

        URI dirLocation = GridFTP.createGsiftpURI(targeterp,
                remoteDestFile.substring(0, remoteDestFile.lastIndexOf("/")));
        gssCredential = context.getGssCredential();
        System.out.println(dirLocation);

    }

    public void testMakeDir() throws Exception {

        String targetErp = executionContext.getGridFTPServerDestination();
        String remoteDestinationFile = executionContext.getDestinationDataLocation();

        URI dirLocation = GridFTP.createGsiftpURI(targetErp,
                remoteDestinationFile.substring(0, remoteDestinationFile.lastIndexOf("/")));

        GridFTP ftp = new GridFTP();
        ftp.makeDir(dirLocation, gssCredential);

        Assert.assertTrue(ftp.exists(dirLocation, gssCredential));

    }

    public void testTransferData() throws Exception {

        String sourceERP = executionContext.getGridFTPServerSource();
        String remoteSrcFile = executionContext.getSourceDataLocation();

        String targetErp = executionContext.getGridFTPServerDestination();
        String remoteDestinationFile = executionContext.getDestinationDataLocation();

        URI srcURI = GridFTP.createGsiftpURI(sourceERP, remoteSrcFile);
        URI destURI = GridFTP.createGsiftpURI(targetErp, remoteDestinationFile);

        GridFTP ftp = new GridFTP();
        ftp.transfer(srcURI, destURI, gssCredential, true);

        Assert.assertTrue(ftp.exists(destURI, gssCredential));

    }

    public void testDownloadFile() throws Exception {

        String fileName = "./downloaded";

        File deleteFile = new File(fileName);

        if (deleteFile.exists()) {
            if (!deleteFile.delete())
                throw new RuntimeException("Unable to delete file " + fileName);
        }

        File f = new File(fileName);

        GridFTP ftp = new GridFTP();
        ftp.downloadFile(executionContext.getSourceDataFileUri(),
                gssCredential, f);

        Assert.assertTrue(f.exists());

    }

    public void testFileExists() throws Exception {

        GridFTP ftp = new GridFTP();
        Assert.assertTrue(ftp.exists(executionContext.getSourceDataFileUri(), gssCredential));
    }

    public void testUpdateFile() throws Exception {

        String currentDir = System.getProperty("projectDirectory");

        if (currentDir == null)
            currentDir = "src/test/resources";
        else
            currentDir = currentDir + "/src/test/resources";

        String file = currentDir + "/dummy";

        System.out.println("File to upload is " + file);

        File fileToUpload = new File(file);

        Assert.assertTrue(fileToUpload.canRead());

        GridFTP ftp = new GridFTP();
        ftp.updateFile(executionContext.getUploadingFilePathUri(), gssCredential, fileToUpload);

        Assert.assertTrue(ftp.exists(executionContext.getUploadingFilePathUri(), gssCredential));
    }
}