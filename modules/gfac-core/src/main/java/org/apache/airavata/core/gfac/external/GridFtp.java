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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.GridFTPContactInfo;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.HostPort;
import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GridFTP tools
 */
public class GridFtp {

    public static final Logger log = LoggerFactory.getLogger(GridFtp.class);

    public static final String GSIFTP_SCHEME = "gsiftp";
    public static final String HOST = "host";

    /**
     * Make directory at remote location
     *
     * @param destURI
     * @param gssCred
     * @throws ServerException
     * @throws IOException
     */
    public void makeDir(URI destURI, GSSCredential gssCred) throws ToolsException {
        GridFTPClient destClient = null;
        GridFTPContactInfo destHost = new GridFTPContactInfo(destURI.getHost(), destURI.getPort());
        try {

            String destPath = destURI.getPath();
            log.info(("Creating Directory = " + destHost + "=" + destPath));

            destClient = new GridFTPClient(destHost.hostName, destHost.port);

            int tryCount = 0;
            while (true) {
                try {
                    destClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
                    destClient.authenticate(gssCred);
                    destClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

                    if (!destClient.exists(destPath)) {
                        destClient.makeDir(destPath);
                    }
                    break;
                } catch (ServerException e) {
                    tryCount++;
                    if (tryCount >= 3) {
                        throw new ToolsException(e.getMessage(), e);
                    }
                    Thread.sleep(10000);
                } catch (IOException e) {
                    tryCount++;
                    if (tryCount >= 3) {
                        throw new ToolsException(e.getMessage(), e);
                    }
                    Thread.sleep(10000);
                }
            }
        } catch (ServerException e) {
            throw new ToolsException("Cannot Create GridFTP Client to:" + destHost.toString(), e);
        } catch (IOException e) {
            throw new ToolsException("Cannot Create GridFTP Client to:" + destHost.toString(), e);
        } catch (InterruptedException e) {
            throw new ToolsException("Internal Error cannot sleep", e);
        } finally {
            if (destClient != null) {
                try {
                    destClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection",e);
                }
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
    public void uploadFile(URI destURI, GSSCredential gsCredential, InputStream io) throws ToolsException {
        GridFTPClient ftpClient = null;
        GridFTPContactInfo contactInfo = new GridFTPContactInfo(destURI.getHost(), destURI.getPort());

        try {

            String remoteFile = destURI.getPath();
            log.info("The remote file is " + remoteFile);

            log.debug("Setup GridFTP Client");

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            log.debug("Uploading file");

            ftpClient.put(remoteFile, new DataSourceStream(io), new MarkerListener() {
                public void markerArrived(Marker marker) {
                }
            });

            log.info("Upload file to:" + remoteFile + " is done");

        } catch (ServerException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + contactInfo.toString(), e);
        } catch (IOException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + contactInfo.toString(), e);
        } catch (ClientException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + contactInfo.toString(), e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection",e);
                }
            }
        }
    }

    public void uploadFile(URI srcURI,  URI destURI, GSSCredential gsCredential) throws ToolsException {
        GridFTPClient srcClient = null;
        GridFTPContactInfo destContactInfo = new GridFTPContactInfo(destURI.getHost(), destURI.getPort());
        GridFTPContactInfo srcContactInfo = new GridFTPContactInfo(srcURI.getHost(),srcURI.getPort());
        try {
            String remoteFile = destURI.getPath();
            log.info("The remote file is " + remoteFile);
            log.debug("Setup GridFTP Client");
            srcClient = new GridFTPClient(srcContactInfo.hostName, srcContactInfo.port);
            srcClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            srcClient.authenticate(gsCredential);
            srcClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            GridFTPClient destClient = new GridFTPClient(srcContactInfo.hostName, srcContactInfo.port);
            destClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            destClient.authenticate(gsCredential);
            destClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);


            log.debug("Uploading file");
            srcClient.transfer(srcURI.getPath(),destClient, remoteFile, false, null);

            log.info("Upload file to:" + remoteFile + " is done");

        } catch (ServerException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + destContactInfo.toString(), e);
        } catch (IOException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + destContactInfo.toString(), e);
        } catch (ClientException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + destContactInfo.toString(), e);
        } finally {
            if (srcClient != null) {
                try {
                    srcClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection",e);
                }
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
    public void uploadFile(URI destURI, GSSCredential gsCredential, File localFile) throws ToolsException {
        GridFTPClient ftpClient = null;
        GridFTPContactInfo contactInfo = new GridFTPContactInfo(destURI.getHost(), destURI.getPort());
        try {

            String remoteFile = destURI.getPath();

            log.info("The local temp file is " + localFile);
            log.info("the remote file is " + remoteFile);

            log.debug("Setup GridFTP Client");

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            log.debug("Uploading file");

            ftpClient.put(localFile, remoteFile, false);

            log.info("Upload file to:" + remoteFile + " is done");

        } catch (ServerException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + contactInfo.toString(), e);
        } catch (IOException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + contactInfo.toString(), e);
        } catch (ClientException e) {
            throw new ToolsException("Cannot upload file to GridFTP:" + contactInfo.toString(), e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection",e);
                }
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
    public void downloadFile(URI destURI, GSSCredential gsCredential, File localFile) throws ToolsException {
        GridFTPClient ftpClient = null;
        GridFTPContactInfo contactInfo = new GridFTPContactInfo(destURI.getHost(), destURI.getPort());
        try {
            String remoteFile = destURI.getPath();

            log.info("The local temp file is " + localFile);
            log.info("the remote file is " + remoteFile);

            log.debug("Setup GridFTP Client");

            ftpClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
            ftpClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            ftpClient.authenticate(gsCredential);
            ftpClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);

            log.debug("Downloading file");

            ftpClient.get(remoteFile, localFile);

            log.info("Download file to:" + remoteFile + " is done");

        } catch (ServerException e) {
            throw new ToolsException("Cannot download file from GridFTP:" + contactInfo.toString(), e);
        } catch (IOException e) {
            throw new ToolsException("Cannot download file from GridFTP:" + contactInfo.toString(), e);
        } catch (ClientException e) {
            throw new ToolsException("Cannot download file from GridFTP:" + contactInfo.toString(), e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection",e);
                }
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
    public String readRemoteFile(URI destURI, GSSCredential gsCredential, File localFile) throws ToolsException {
        BufferedReader instream = null;
        File localTempfile = null;
        try {

            if (localFile == null) {
                localTempfile = File.createTempFile("stderr", "err");
            } else {
                localTempfile = localFile;
            }

            log.debug("Loca temporary file:" + localTempfile);

            downloadFile(destURI, gsCredential, localTempfile);

            instream = new BufferedReader(new FileReader(localTempfile));
            StringBuffer buff = new StringBuffer();
            String temp = null;
            while ((temp = instream.readLine()) != null) {
                buff.append(temp);
                buff.append(GFacConstants.NEWLINE);
            }

            log.debug("finish read file:" + localTempfile);

            return buff.toString();
        } catch (FileNotFoundException e) {
            throw new ToolsException("Cannot read localfile file:" + localTempfile, e);
        } catch (IOException e) {
            throw new ToolsException("Cannot read localfile file:" + localTempfile, e);
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection",e);
                }
            }
        }
    }

    /**
     * Transfer data from one GridFTp Endpoint to another GridFTP Endpoint
     *
     * @param srchost
     * @param desthost
     * @param gssCred
     * @param srcActive
     * @throws ServerException
     * @throws ClientException
     * @throws IOException
     */
    public void transfer(URI srchost, URI desthost, GSSCredential gssCred, boolean srcActive) throws ToolsException {
        GridFTPClient destClient = null;
        GridFTPClient srcClient = null;

        try {
            destClient = new GridFTPClient(desthost.getHost(), desthost.getPort());
            destClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            destClient.authenticate(gssCred);
            destClient.setType(Session.TYPE_IMAGE);


            srcClient = new GridFTPClient(srchost.getHost(), srchost.getPort());
            srcClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            srcClient.authenticate(gssCred);
            srcClient.setType(Session.TYPE_IMAGE);

            if (srcActive) {
                log.debug("Set src active");
                HostPort hp = destClient.setPassive();
                srcClient.setActive(hp);
            } else {
                log.debug("Set dst active");
                HostPort hp = srcClient.setPassive();
                destClient.setActive(hp);
            }

            log.debug("Start transfer file from GridFTP:" + srchost.toString() + " to " + desthost.toString());

            /**
             * Transfer a file. The transfer() function blocks until the transfer is complete.
             */
            srcClient.transfer(srchost.getPath(), destClient, desthost.getPath(), false, null);
            if (srcClient.getSize(srchost.getPath()) == destClient.getSize(desthost.getPath())) {
                log.debug("CHECK SUM OK");
            } else {
                log.debug("****CHECK SUM FAILED****");
            }

        } catch (ServerException e) {
            throw new ToolsException("Cannot transfer file from GridFTP:" + srchost.toString() + " to "
                    + desthost.toString(), e);
        } catch (IOException e) {
            throw new ToolsException("Cannot transfer file from GridFTP:" + srchost.toString() + " to "
                    + desthost.toString(), e);
        } catch (ClientException e) {
            throw new ToolsException("Cannot transfer file from GridFTP:" + srchost.toString() + " to "
                    + desthost.toString(), e);
        } finally {
            if (destClient != null) {
                try {
                    destClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection at Desitnation:" + desthost.toString());
                }
            }
            if (srcClient != null) {
                try {
                    srcClient.close();
                } catch (Exception e) {
                    log.warn("Cannot close GridFTP client connection at Source:" + srchost.toString(),e);
                }
            }
        }
    }

	/**
	 * List files in a GridFTP directory
	 * @param dirURI
	 * @param gssCred
	 * @return
	 * @throws ToolsException
	 */
    @SuppressWarnings("unchecked")
	public List<String> listDir(URI dirURI, GSSCredential gssCred) throws ToolsException {
    	List<String> files = new  ArrayList<String>();
	    GridFTPClient srcClient = null;
			try {
				GridFTPContactInfo contactInfo = new GridFTPContactInfo(dirURI.getHost(), dirURI.getPort());

				srcClient = new GridFTPClient(contactInfo.hostName, contactInfo.port);
				srcClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
				srcClient.authenticate(gssCred);
				srcClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);
				srcClient.setType(Session.TYPE_ASCII);
				srcClient.changeDir(dirURI.getPath());

				Vector<Object> fileInfo = null;
				try {
					fileInfo = srcClient.mlsd();
				} catch (Throwable e) {
					fileInfo = srcClient.list();
				}

				if (!fileInfo.isEmpty()) {
					for (int j = 0; j < fileInfo.size(); ++j) {
						String name = null;
						if (fileInfo.get(j) instanceof MlsxEntry) {
							name = ((MlsxEntry) fileInfo.get(j)).getFileName();
						} else if (fileInfo.get(j) instanceof FileInfo) {
							name = ((FileInfo) fileInfo.get(j)).getName();
						} else {
							throw new ToolsException("Unsupported type returned by gridftp " + fileInfo.get(j));
						}

						if (!name.equals(".") && !name.equals("..")) {
							URI uri = GfacUtils.createGsiftpURI(contactInfo.hostName, dirURI.getPath() + File.separator + name);
							files.add(uri.getPath());
						}
					}
				}
				return files;
			} catch (IOException e) {
				throw new ToolsException("Could not list directory: " + dirURI.toString() ,e);
			} catch (ServerException e) {
				throw new ToolsException("Could not list directory: " + dirURI.toString() ,e);
			} catch (ClientException e) {
				throw new ToolsException("Could not list directory: " + dirURI.toString() ,e);
			} catch (URISyntaxException e) {
				throw new ToolsException("Error creating URL of listed files: " + dirURI.toString() ,e);
			} finally {
				if (srcClient != null) {
	                try {
	                    srcClient.close();
	                } catch (Exception e) {
	                    log.warn("Cannot close GridFTP client connection", e);
	                }
	            }
		}
	}
}
