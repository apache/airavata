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

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthMethod;
import org.apache.airavata.helix.adaptor.wrapper.SCPFileTransferWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SFTPClientWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class will keep a pool of {@link SSHClient} and scale them according to the number of SSH requests.
 * This pool is MaxSessions per connection aware and thread safe. It is intelligent to decide the number of connections
 * that it should create and number of sessions should be used in each created connection to avoid possible connection
 * refusals from the server side.
 */
public class PoolingSSHJClient extends SSHClient {

    private final static Logger logger = LoggerFactory.getLogger(PoolingSSHJClient.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<SSHClient, SSHClientInfo> clientInfoMap = new HashMap<>();

    private HostKeyVerifier hostKeyVerifier;
    private String username;
    private List<AuthMethod> authMethods;
    private Config config;
    private String host;
    private int port;

    private int maxSessionsForConnection = 10;

    public void addHostKeyVerifier(HostKeyVerifier verifier) {
        this.hostKeyVerifier = verifier;
    }

    public void auth(String username, List<AuthMethod> methods) throws UserAuthException, TransportException {
        this.username = username;
        this.authMethods = methods;
    }

    public PoolingSSHJClient(Config config, String host, int port) {
        this.config = config;
        this.host = host;
        this.port = port;
    }

    ////////////////// client specific operations ///////

    private SSHClient leaseSSHClient() throws Exception {
        lock.writeLock().lock();

        try {
            if (clientInfoMap.isEmpty()) {
                SSHClient newClient = createNewSSHClient();
                SSHClientInfo info = new SSHClientInfo(1, System.currentTimeMillis());
                clientInfoMap.put(newClient, info);

                /* if this is the very first connection that is created to the compute host, fetch the MaxSessions
                 * value form SSHD config file in order to tune the pool
                 */
                logger.info("Fetching max sessions for the connection of " + host);
                try (SFTPClient sftpClient = newClient.newSFTPClient()) {
                    RemoteFile remoteFile = sftpClient.open("/etc/ssh/sshd_config");
                    byte[] readContent = new byte[(int) remoteFile.length()];
                    remoteFile.read(0, readContent, 0, readContent.length);

                    if (logger.isTraceEnabled()) {
                        logger.trace("SSHD config file content : " + new String(readContent));
                    }
                    String[] lines = new String(readContent).split("\n");

                    for (String line : lines) {
                        if (line.trim().startsWith("MaxSessions")) {
                            String[] splits = line.split(" ");
                            if (splits.length == 2) {
                                int sessionCount = Integer.parseInt(splits[1]);
                                logger.info("Max session count is : " + sessionCount + " for " + host);
                                setMaxSessionsForConnection(sessionCount);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to fetch max session count for " + host + ". Continuing with default value 10. " + e.getMessage() );
                }
                return newClient;

            } else {

                Optional<Map.Entry<SSHClient, SSHClientInfo>> minEntryOp = clientInfoMap.entrySet().stream().min(Comparator.comparing(entry -> entry.getValue().sessionCount));
                if (minEntryOp.isPresent()) {
                    Map.Entry<SSHClient, SSHClientInfo> minEntry = minEntryOp.get();
                    // use the connection with least amount of sessions created.

                    logger.debug("Session count for selected connection {}. Threshold {}", minEntry.getValue().getSessionCount(), maxSessionsForConnection);
                    if (minEntry.getValue().getSessionCount() >= maxSessionsForConnection) {
                        // if it exceeds the maximum session count, create a new connection
                        logger.debug("Connection with least amount of sessions exceeds the threshold. So creating a new connection");
                        SSHClient newClient = createNewSSHClient();
                        SSHClientInfo info = new SSHClientInfo(1, System.currentTimeMillis());
                        clientInfoMap.put(newClient, info);
                        return newClient;

                    } else {
                        // otherwise reuse the same connetion
                        logger.debug("Reusing the same connection as it doesn't exceed the threshold");
                        minEntry.getValue().setSessionCount(minEntry.getValue().getSessionCount() + 1);
                        minEntry.getValue().setLastAccessedTime(System.currentTimeMillis());
                        return minEntry.getKey();
                    }
                } else {
                    throw new Exception("Failed to find a connection in the pool for " + host);
                }
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeDisconnectedClients(SSHClient client) {
        lock.writeLock().lock();

        try {
            if (clientInfoMap.containsKey(client)) {
                clientInfoMap.remove(client);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void untrackClosedSessions(SSHClient client, int sessionId) {
        lock.writeLock().lock();

        try {
            if (clientInfoMap.containsKey(client)) {
                SSHClientInfo sshClientInfo = clientInfoMap.get(client);
                sshClientInfo.setSessionCount(sshClientInfo.getSessionCount() - 1);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private SSHClient createNewSSHClient() throws IOException {

        SSHClient sshClient;
        if (config != null) {
            sshClient = new SSHClient(config);
        } else {
            sshClient = new SSHClient();
        }

        sshClient.getConnection().getTransport().setDisconnectListener(new DisconnectListener() {
            @Override
            public void notifyDisconnect(DisconnectReason reason, String message) {
                logger.warn("Connection disconnected " + message + " due to " + reason.name());
                removeDisconnectedClients(sshClient);
            }
        });

        if (hostKeyVerifier != null) {
            sshClient.addHostKeyVerifier(hostKeyVerifier);
        }

        sshClient.connect(host);

        sshClient.getConnection().getKeepAlive().setKeepAliveInterval(5); //send keep alive signal every 5sec

        if (authMethods != null) {
            sshClient.auth(username, authMethods);
        }

        return sshClient;
    }

    public Session startSessionWrapper() throws Exception {

        final SSHClient sshClient = leaseSSHClient();

        try {
            return new SessionWrapper(sshClient.startSession(), (id) -> untrackClosedSessions(sshClient, id));

        } catch (Exception e) {
            if (sshClient != null) {
                untrackClosedSessions(sshClient, -1);
            }
            throw e;
        }
    }

    public SCPFileTransferWrapper newSCPFileTransferWrapper() throws Exception {

        final SSHClient sshClient = leaseSSHClient();

        try {
            return new SCPFileTransferWrapper(sshClient.newSCPFileTransfer(), (id) -> untrackClosedSessions(sshClient, id));

        } catch (Exception e) {
            if (sshClient != null) {
                untrackClosedSessions(sshClient, -1);
            }
            throw e;
        }
    }

    public SFTPClient newSFTPClientWrapper() throws Exception {

        final SSHClient sshClient = leaseSSHClient();

        try {
            return new SFTPClientWrapper(sshClient.newSFTPClient(), (id) -> untrackClosedSessions(sshClient, id));
        } catch (Exception e) {

            if (sshClient != null) {
                untrackClosedSessions(sshClient, -1);
            }
            throw e;
        }
    }

    public class SSHClientInfo {

        private int sessionCount;
        private long lastAccessedTime;

        public SSHClientInfo(int sessionCount, long lastAccessedTime) {
            this.sessionCount = sessionCount;
            this.lastAccessedTime = lastAccessedTime;
        }

        public int getSessionCount() {
            return sessionCount;
        }

        public SSHClientInfo setSessionCount(int sessionCount) {
            this.sessionCount = sessionCount;
            return this;
        }

        public long getLastAccessedTime() {
            return lastAccessedTime;
        }

        public SSHClientInfo setLastAccessedTime(long lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
            return this;
        }
    }

    public HostKeyVerifier getHostKeyVerifier() {
        return hostKeyVerifier;
    }

    public PoolingSSHJClient setHostKeyVerifier(HostKeyVerifier hostKeyVerifier) {
        this.hostKeyVerifier = hostKeyVerifier;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public PoolingSSHJClient setUsername(String username) {
        this.username = username;
        return this;
    }

    public Config getConfig() {
        return config;
    }

    public PoolingSSHJClient setConfig(Config config) {
        this.config = config;
        return this;
    }

    public String getHost() {
        return host;
    }

    public PoolingSSHJClient setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public PoolingSSHJClient setPort(int port) {
        this.port = port;
        return this;
    }

    public int getMaxSessionsForConnection() {
        return maxSessionsForConnection;
    }

    public PoolingSSHJClient setMaxSessionsForConnection(int maxSessionsForConnection) {
        this.maxSessionsForConnection = maxSessionsForConnection;
        return this;
    }

    public Map<SSHClient, SSHClientInfo> getClientInfoMap() {
        return clientInfoMap;
    }
}
