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
package org.apache.airavata.data.manager.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.data.manager.DataManagerConstants;
import org.apache.airavata.data.manager.DataManagerException;
import org.apache.airavata.data.manager.utils.ssh.SSHApiException;
import org.apache.airavata.data.manager.utils.ssh.SSHKeyAuthentication;
import org.apache.airavata.data.manager.utils.ssh.SSHUserInfo;
import org.apache.airavata.data.manager.utils.ssh.SSHUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DataTransferUtils {
    private final static Logger logger = LoggerFactory.getLogger(DataTransferUtils.class);

    private final SSHKeyAuthentication authentication;
    private final JSch jSch;

    public DataTransferUtils(SSHKeyAuthentication authentication) throws DataManagerException {
        try {
            this.authentication = authentication;
            this.jSch = new JSch();
            this.jSch.addIdentity(authentication.getPrivateKeyFilePath(), authentication.getPublicKeyFilePath(),
                    authentication.getPassphrase().getBytes());
        } catch (JSchException e) {
            throw new DataManagerException("JSch initialization error ", e);
        }
    }

    public boolean copyData(URI srcUri, URI destUri) throws DataManagerException {
        if(srcUri.getScheme().equals(DataManagerConstants.LOCAL_URI_SCHEME)
                && destUri.getScheme().equals(DataManagerConstants.LOCAL_URI_SCHEME)){
            try{
                copyLocalToLocal(srcUri, destUri);
            }catch (IOException e){
                throw new DataManagerException("Error while copying sourceFile: " + srcUri.getPath()
                        + ", to destinationFile: " + destUri.getPath(), e);
            }
        }else if(srcUri.getScheme().equals(DataManagerConstants.LOCAL_URI_SCHEME)
                && destUri.getScheme().equals(DataManagerConstants.SCP_URI_SCHEME)){
            try {
                copyLocalToScp(srcUri, destUri);
            } catch (Exception e) {
                throw new DataManagerException("Error while copying sourceFile: " + srcUri.getPath()
                        + ", to destinationFile: " + destUri.getPath(), e);
            }
        }else if(srcUri.getScheme().equals(DataManagerConstants.SCP_URI_SCHEME)
                && destUri.getScheme().equals(DataManagerConstants.LOCAL_URI_SCHEME)){
            try {
                copyScpToLocal(srcUri, destUri);
            } catch (Exception e) {
                throw new DataManagerException("Error while copying sourceFile: " + srcUri.getPath()
                        + ", to destinationFile: " + destUri.getPath(), e);
            }
        }else if(srcUri.getScheme().equals(DataManagerConstants.SCP_URI_SCHEME)
                && destUri.getScheme().equals(DataManagerConstants.SCP_URI_SCHEME)){
            try {
                copyScpToScp(srcUri, destUri);
            } catch (Exception e) {
                throw new DataManagerException("Error while copying sourceFile: " + srcUri.getPath()
                        + ", to destinationFile: " + destUri.getPath(), e);
            }
        }else{
            throw new DataManagerException("Unsupported Data Transfer protocol. Currently Data Manager only supports" +
                    " one to one SCP/LOCAL transfers");
        }

        return true;
    }

    private void copyLocalToLocal(URI srcUri, URI destUri) throws IOException {
        Path sourcePath = Paths.get(srcUri.getPath());
        Path targetPath = Paths.get(destUri.getPath());
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyLocalToScp(URI srcUri, URI destUri) throws IOException, JSchException, SSHApiException {
        Session destSession = getSSHSession(destUri);
        destSession.connect();
        SSHUtils.scpTo(srcUri.getPath(), destUri.getPath(), destSession);
    }

    private void copyScpToLocal(URI srcUri, URI destUri) throws IOException, JSchException, SSHApiException {
        Session srcSession = getSSHSession(srcUri);
        srcSession.connect();
        SSHUtils.scpFrom(srcUri.getPath(), destUri.getPath(), srcSession);
    }

    private void copyScpToScp(URI srcUri, URI destUri) throws JSchException, IOException {
        Session srcSession = getSSHSession(srcUri);
        srcSession.connect();
        Session destSession = getSSHSession(destUri);
        destSession.connect();
        SSHUtils.scpThirdParty(srcUri.getPath(), srcSession, destUri.getPath(), destSession);
    }

    private Session getSSHSession(URI uri) throws JSchException {
        int port;
        if(uri.getPort() == -1){
            port = DataManagerConstants.DEFAULT_SSH_PORT;
        }else{
            port = uri.getPort();
        }
        Session session = jSch.getSession(authentication.getUserName(), uri.getHost(), port);
        session.setUserInfo(new SSHUserInfo(authentication.getUserName(), null, authentication.getPassphrase()));
        if (authentication.getStrictHostKeyChecking().equals("yes")) {
            jSch.setKnownHosts(authentication.getKnownHostsFilePath());
        } else {
            session.setConfig("StrictHostKeyChecking", "no");
        }
        return session;
    }
}