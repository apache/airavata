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
 */
package org.apache.airavata.helix.adaptor;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.AgentUtils;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.helix.adaptor.wrapper.SCPFileTransferWrapper;
import org.apache.airavata.helix.agent.ssh.StandardOutReader;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SSHJAgentAdaptor implements AgentAdaptor {

    private final static Logger logger = LoggerFactory.getLogger(SSHJAgentAdaptor.class);

    private PoolingSSHJClient sshjClient;

    protected void createPoolingSSHJClient(String user, String host, String publicKey, String privateKey, String passphrase) throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        sshjClient = new PoolingSSHJClient(defaultConfig, host, 22);
        sshjClient.addHostKeyVerifier((hostname, port, key) -> true);

        sshjClient.setMaxSessionsForConnection(10);

        PasswordFinder passwordFinder = passphrase != null ? PasswordUtils.createOneOff(passphrase.toCharArray()) : null;

        KeyProvider keyProvider = sshjClient.loadKeys(privateKey, publicKey, passwordFinder);

        final List<AuthMethod> am = new LinkedList<>();
        am.add(new AuthPublickey(keyProvider));

        am.add(new AuthKeyboardInteractive(new ChallengeResponseProvider() {
            @Override
            public List<String> getSubmethods() {
                return new ArrayList<>();
            }

            @Override
            public void init(Resource resource, String name, String instruction) {

            }

            @Override
            public char[] getResponse(String prompt, boolean echo) {
                return new char[0];
            }

            @Override
            public boolean shouldRetry() {
                return false;
            }
        }));

        sshjClient.auth(user, am);
    }

    @Override
    public void init(String computeResource, String gatewayId, String userId, String token) throws AgentException {
        try {
            ComputeResourceDescription computeResourceDescription = AgentUtils.getRegistryServiceClient().getComputeResource(computeResource);

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            createPoolingSSHJClient(userId, computeResourceDescription.getHostName(),
                    sshCredential.getPublicKey(), sshCredential.getPrivateKey(), sshCredential.getPassphrase());

        } catch (Exception e) {
            logger.error("Error while initializing ssh agent for compute resource " + computeResource + " to token " + token, e);
            throw new AgentException("Error while initializing ssh agent for compute resource " + computeResource + " to token " + token, e);
        }
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        try (Session session = sshjClient.startSessionWrapper()) {
            Session.Command exec = session.exec((workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command);
            StandardOutReader standardOutReader = new StandardOutReader();
            standardOutReader.readStdOutFromStream(exec.getInputStream());
            standardOutReader.readStdErrFromStream(exec.getErrorStream());
            standardOutReader.setExitCode(exec.getExitStatus());
            return standardOutReader;
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public void createDirectory(String path) throws AgentException {
        try (SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            sftpClient.mkdir(path);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public void copyFileTo(String localFile, String remoteFile) throws AgentException {
        try(SCPFileTransferWrapper fileTransfer = sshjClient.newSCPFileTransferWrapper()) {
            fileTransfer.upload(localFile, remoteFile);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public void copyFileFrom(String remoteFile, String localFile) throws AgentException {
        try(SCPFileTransferWrapper fileTransfer = sshjClient.newSCPFileTransferWrapper()) {
            fileTransfer.download(remoteFile, localFile);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        try (SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            List<RemoteResourceInfo> ls = sftpClient.ls(path);
            return ls.stream().map(RemoteResourceInfo::getName).collect(Collectors.toList());
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        try (SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            return sftpClient.statExistence(filePath) != null;
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {

        try (SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            List<RemoteResourceInfo> ls = sftpClient.ls(parentPath, resource -> isMatch(resource.getName(), fileName));
            return ls.stream().map(RemoteResourceInfo::getPath).collect(Collectors.toList());
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    private boolean isMatch(String s, String p) {
        int i = 0;
        int j = 0;
        int starIndex = -1;
        int iIndex = -1;

        while (i < s.length()) {
            if (j < p.length() && (p.charAt(j) == '?' || p.charAt(j) == s.charAt(i))) {
                ++i;
                ++j;
            } else if (j < p.length() && p.charAt(j) == '*') {
                starIndex = j;
                iIndex = i;
                j++;
            } else if (starIndex != -1) {
                j = starIndex + 1;
                i = iIndex+1;
                iIndex++;
            } else {
                return false;
            }
        }
        while (j < p.length() && p.charAt(j) == '*') {
            ++j;
        }
        return j == p.length();
    }
}
