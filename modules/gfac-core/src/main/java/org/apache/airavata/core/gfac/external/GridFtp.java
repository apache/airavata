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
import java.io.InputStream;
import java.net.URI;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.utils.ContactInfo;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.HostPort;
import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSCredential;

public class GridFtp {

    public static final String GSIFTP_SCHEME = "gsiftp";

    /**
     * Make directory at remote location
     * 
     * @param destURI
     * @param gssCred
     * @throws GfacException
     */
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

    /**
     * Upload file from stream
     * 
     * @param destURI
     * @param gsCredential
     * @param localFile
     * @throws GfacException
     */
    public void updateFile(URI destURI, GSSCredential gsCredential, InputStream io) throws GfacException {       
        GridFTPClient ftpClient = null;

        try {
            ContactInfo contactInfo = new ContactInfo(destURI.getHost(), destURI.getPort());
            String remoteFile = destURI.getPath();

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization("host"));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            System.out.println("the remote file is " + remoteFile);

            ftpClient.put(remoteFile, new DataSourceStream(io), new MarkerListener() {
                public void markerArrived(Marker marker) {
                }
            });
            
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
    }
    /**
     * Upload file to remote location
     * 
     * @param destURI
     * @param gsCredential
     * @param localFile
     * @throws GfacException
     */
    public void updateFile(URI destURI, GSSCredential gsCredential, File localFile) throws GfacException {
        GridFTPClient ftpClient = null;

        try {
            ContactInfo contactInfo = new ContactInfo(destURI.getHost(), destURI.getPort());
            String remoteFile = destURI.getPath();

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization("host"));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            System.out.println("the local temp file is " + localFile);
            System.out.println("the remote file is " + remoteFile);

            ftpClient.put(localFile, remoteFile, false);
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
    }

    /**
     * Download File from remote location
     * 
     * @param destURI
     * @param gsCredential
     * @param localFile
     * @throws GfacException
     */
    public void downloadFile(URI destURI, GSSCredential gsCredential, File localFile) throws GfacException {
        GridFTPClient ftpClient = null;

        try {
            ContactInfo contactInfo = new ContactInfo(destURI.getHost(), destURI.getPort());
            String remoteFile = destURI.getPath();

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization("host"));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            System.out.println("the local temp file is " + localFile);
            System.out.println("the remote file is " + remoteFile);

            ftpClient.get(remoteFile, localFile);
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
    }

    /**
     * Stream remote file
     * 
     * @param destURI
     * @param gsCredential
     * @param localFile
     * @return
     * @throws GfacException
     */
    public String readRemoteFile(URI destURI, GSSCredential gsCredential, File localFile) throws GfacException {
        try {
            File localTempfile;
            if (localFile == null) {
                localTempfile = File.createTempFile("stderr", "err");
            } else {
                localTempfile = localFile;
            }

            downloadFile(destURI, gsCredential, localTempfile);

            FileInputStream instream = new FileInputStream(localTempfile);
            int size = instream.available();
            byte[] buf = new byte[size];

            instream.read(buf);

            return new String(buf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Transfer data from one gridFTp endpoint to another gridFTP endpoint
     * 
     * @param srchost
     * @param desthost
     * @param gssCred
     * @param srcActive
     * @throws ServerException
     * @throws ClientException
     * @throws IOException
     */
    public void transfer(URI srchost, URI desthost, GSSCredential gssCred, boolean srcActive) throws ServerException,
            ClientException, IOException {
        GridFTPClient destClient = null;
        GridFTPClient srcClient = null;

        try {
            destClient = new GridFTPClient(desthost.getHost(), desthost.getPort());
            destClient.setAuthorization(new HostAuthorization("host"));
            destClient.authenticate(gssCred);
            destClient.setType(Session.TYPE_IMAGE);

            srcClient = new GridFTPClient(srchost.getHost(), srchost.getPort());
            srcClient.setAuthorization(new HostAuthorization("host"));
            srcClient.authenticate(gssCred);
            srcClient.setType(Session.TYPE_IMAGE);

            if (srcActive) {
                HostPort hp = destClient.setPassive();
                srcClient.setActive(hp);
            } else {
                HostPort hp = srcClient.setPassive();
                destClient.setActive(hp);
            }

            /**
             * Transfer a file. The transfer() function blocks until the
             * transfer is complete.
             */
            srcClient.transfer(srchost.getPath(), destClient, desthost.getPath(), false, null);
            if (srcClient.getSize(srchost.getPath()) == destClient.getSize(desthost.getPath())) {
                System.out.println("CHECK SUM OK");
            } else {
                System.out.println("CHECK SUM FAIL");
            }

        } catch (ServerException e) {
            throw e;
        } catch (ClientException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            if (destClient != null) {
                try {
                    destClient.close();
                } catch (Exception e) {
                    // no op
                }
            }
            if (srcClient != null) {
                try {
                    srcClient.close();
                } catch (Exception e) {
                    // no op
                }
            }
        }
    }
}
