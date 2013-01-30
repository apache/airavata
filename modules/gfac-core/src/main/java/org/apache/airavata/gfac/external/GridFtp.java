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

package org.apache.airavata.gfac.external;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.utils.GridConfigurationHandler;
import org.apache.airavata.gfac.utils.GridFTPContactInfo;
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
                    makeExternalConfigurations(destClient, false);

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
     * @param io
     * @throws GFacException
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
            makeExternalConfigurations(ftpClient, false);

            log.info("Uploading file");
            if (checkBinaryExtensions(remoteFile)) {
                log.debug("Transfer mode is set to Binary for a file upload");
                ftpClient.setType(Session.TYPE_IMAGE);
            }


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
            makeExternalConfigurations(srcClient, true);

            GridFTPClient destClient = new GridFTPClient(destContactInfo.hostName, destContactInfo.port);
            destClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            destClient.authenticate(gsCredential);
            destClient.setDataChannelAuthentication(DataChannelAuthentication.SELF);
            makeExternalConfigurations(destClient, false);
		log.debug("Uploading file");
            if (checkBinaryExtensions(remoteFile)) {
                log.debug("Transfer mode is set to Binary for a file upload");
                srcClient.setType(Session.TYPE_IMAGE);
            }

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
     * @throws GFacException
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
            makeExternalConfigurations(ftpClient, false);

            log.debug("Uploading file");
            if (checkBinaryExtensions(remoteFile)) {
                log.debug("Transfer mode is set to Binary for a file upload");
                ftpClient.setType(Session.TYPE_IMAGE);
            }


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
     * @throws GFacException
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
            makeExternalConfigurations(ftpClient, true);

            log.debug("Downloading file");
            if (checkBinaryExtensions(remoteFile)) {
                log.debug("Transfer mode is set to Binary to download a file");
                ftpClient.setType(Session.TYPE_IMAGE);
            }

            ftpClient.get(remoteFile, localFile);

            log.info("Download file to:" + localFile + " is done");

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
     * @throws GFacException
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

            log.info("Loca temporary file:" + localTempfile);

            downloadFile(destURI, gsCredential, localTempfile);

            instream = new BufferedReader(new FileReader(localTempfile));
            StringBuffer buff = new StringBuffer();
            String temp = null;
            while ((temp = instream.readLine()) != null) {
                buff.append(temp);
                buff.append(Constants.NEWLINE);
            }

            log.info("finish read file:" + localTempfile);

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
            makeExternalConfigurations(destClient, false);

            if (checkBinaryExtensions(desthost.getPath())) {
                log.debug("Transfer mode is set to Binary");
                destClient.setType(Session.TYPE_IMAGE);
            }

            srcClient = new GridFTPClient(srchost.getHost(), srchost.getPort());
            srcClient.setAuthorization(new HostAuthorization(GridFtp.HOST));
            srcClient.authenticate(gssCred);
            makeExternalConfigurations(srcClient, true);

            if (checkBinaryExtensions(srchost.getPath())) {
                log.debug("Transfer mode is set to Binary");
                srcClient.setType(Session.TYPE_IMAGE);
            }

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
	            makeExternalConfigurations(srcClient, true);

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
							URI uri = GFacUtils.createGsiftpURI(contactInfo.hostName, dirURI.getPath() + File.separator + name);
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
    /**
     * Method to check file extension as binary to set transfer type
     * @param filePath
     * @return
     */
    private static boolean checkBinaryExtensions(String filePath){
        String extension = filePath.substring(filePath.lastIndexOf(".")+1,filePath.length());
        Set<String> extensions = new HashSet<String>(Arrays.asList(new String[] {"tar","zip","gz","tgz"}));
        if(extensions.contains(extension)){
            return true;
        }else{
            return false;
        }

    }

    /**
     * This function will call the external configuration handlers to configure the GridFTPClient
     * object.
     * @param client
     * @param source
     */
	private void makeExternalConfigurations(GridFTPClient client, boolean source) {
		GridConfigurationHandler[] handlers = GFacConfiguration.getGridConfigurationHandlers();
		for(GridConfigurationHandler handler:handlers){
			try {
				if (source) {
					handler.handleSourceFTPClient(client);
				}else{
					handler.handleDestinationFTPClient(client);
				}
			} catch (Exception e) {
				//TODO Right now we are just catching & ignoring the exception. But later on we need
				//to throw this exception to notify the user of configuration errors of their
				//custom configuration handlers.
				log.error("Error while configuring GridFTPClient for "
								+ client.getHost(), e);
			}
		}

	}
}
