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

package org.apache.airavata.core.gfac.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.utils.ContactInfo;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSCredential;

public class GridFtp {
    public void makeDir(URI destURI, GSSCredential gssCred) throws GfacException {
        GridFTPClient destClient = null;
        try {
            ContactInfo destHost = new ContactInfo(destURI.getHost(), destURI.getPort());
            String destPath = destURI.getPath();
            System.out.println(("Creating Directory = " + destHost + "=" + destPath));

            destClient = new GridFTPClient(destHost.hostName, destHost.port);

            int tryCount = 0;
            while (true) {
                try {
                    destClient.setAuthorization(new HostAuthorization("host"));
                    destClient.authenticate(gssCred);
                    destClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

                    if (!destClient.exists(destPath)) {
                        destClient.makeDir(destPath);
                    }
                    break;
                } catch (Exception e) {
                    tryCount++;
                    if (tryCount >= 3) {
                        throw new GfacException(e.getMessage(), e);
                    }
                    Thread.sleep(10000);
                }
            }
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (destClient != null)
                try {
                    destClient.close();
                } catch (Exception e) {
                    // no op
                }
        }
    }

    public String readRemoteFile(URI destURI, GSSCredential gsCredential, File localFile) throws GfacException {
        GridFTPClient ftpClient = null;

        try {
            ContactInfo contactInfo = new ContactInfo(destURI.getHost(), destURI.getPort());
            String remoteFile = destURI.getPath();

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization("host"));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            File localTempfile;
            if (localFile == null) {
                localTempfile = File.createTempFile("stderr", "err");
            } else {
                localTempfile = localFile;
            }

            System.out.println("the local temp file is " + localTempfile);
            System.out.println("the remote file is " + remoteFile);

            ftpClient.get(remoteFile, localTempfile);
            FileInputStream instream = new FileInputStream(localTempfile);
            int size = instream.available();
            byte[] buf = new byte[size];

            instream.read(buf);

            return new String(buf);
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient != null)
                try {
                    ftpClient.close();
                } catch (Exception e) {
                    // no op
                }
        }
        return null;
    }

}
