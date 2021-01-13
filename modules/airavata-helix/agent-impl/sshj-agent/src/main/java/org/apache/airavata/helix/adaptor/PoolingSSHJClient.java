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
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthMethod;
import org.apache.airavata.helix.adaptor.wrapper.SCPFileTransferWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SFTPClientWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SSHClientWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * This class will keep a pool of {@link SSHClient} and scale them according to the number of SSH requests.
 * This pool is MaxSessions per connection aware and thread safe. It is intelligent to decide the number of connections
 * that it should create and number of sessions should be used in each created connection to avoid possible connection
 * refusals from the server side.
 */
public class PoolingSSHJClient extends SSHClient {

    private final static Logger logger = LoggerFactory.getLogger(PoolingSSHJClient.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<SSHClientWrapper, SSHClientInfo> clientInfoMap = new HashMap<>();

    private HostKeyVerifier hostKeyVerifier;
    private String username;
    private List<AuthMethod> authMethods;
    private Config config;
    private String host;
    private int port;

    private int maxSessionsForConnection = 10;
    private long maxConnectionIdleTimeMS = 10 * 60 * 1000;

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

        ScheduledExecutorService poolMonitoringService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SSH-Pool-Monitor-" + host + "-" + port);
            thread.setDaemon(true);
            return thread;
        });

        poolMonitoringService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                removeStaleConnections();
            }
        }, 10, maxConnectionIdleTimeMS * 2, TimeUnit.MILLISECONDS);
    }

    ////////////////// client specific operations ///////

    private SSHClientWrapper newClientWithSessionValidation() throws IOException {
        SSHClientWrapper newClient = createNewSSHClient();
        SSHClientInfo info = new SSHClientInfo(1, System.currentTimeMillis(), clientInfoMap.size());
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
            logger.warn("Failed to fetch max session count for " + host + ". Continuing with default value 1. " + e.getMessage() );
        }
        return newClient;
    }

    private SSHClientWrapper leaseSSHClient() throws Exception {
        lock.writeLock().lock();

        try {
            if (clientInfoMap.isEmpty()) {
                return newClientWithSessionValidation();

            } else {

                Optional<Map.Entry<SSHClientWrapper, SSHClientInfo>> minEntryOp = clientInfoMap.entrySet().stream().min(Comparator.comparing(entry -> entry.getValue().sessionCount));
                if (minEntryOp.isPresent()) {
                    Map.Entry<SSHClientWrapper, SSHClientInfo> minEntry = minEntryOp.get();
                    // use the connection with least amount of sessions created.

                    logger.debug("Session count for selected connection {} is {}. Threshold {} for host {}",
                            minEntry.getValue().getClientId(), minEntry.getValue().getSessionCount(), maxSessionsForConnection, host);
                    if (minEntry.getValue().getSessionCount() >= maxSessionsForConnection) {
                        // if it exceeds the maximum session count, create a new connection
                        logger.debug("Connection with least amount of sessions exceeds the threshold. So creating a new connection. " +
                                "Current connection count {} for host {}", clientInfoMap.size(), host);
                        return newClientWithSessionValidation();

                    } else {
                        // otherwise reuse the same connetion
                        logger.debug("Reusing the same connection {} as it doesn't exceed the threshold for host {}", minEntry.getValue().getClientId(), host);
                        minEntry.getValue().setSessionCount(minEntry.getValue().getSessionCount() + 1);
                        minEntry.getValue().setLastAccessedTime(System.currentTimeMillis());

                        SSHClientWrapper sshClient = minEntry.getKey();

                        if (!sshClient.isConnected() || !sshClient.isAuthenticated() || sshClient.isErrored()) {
                            logger.warn("Client for host {} is not connected or not authenticated. Creating a new client", host);
                            removeDisconnectedClients(sshClient, true);
                            return newClientWithSessionValidation();
                        } else {
                            return sshClient;
                        }
                    }
                } else {
                    throw new Exception("Failed to find a connection in the pool for host " + host);
                }
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeDisconnectedClients(SSHClientWrapper client, boolean doDisconnect) {
        lock.writeLock().lock();

        if (doDisconnect) {
            try {
                client.disconnect();
            } catch (Exception e) {
                log.warn("Errored while disconnecting the client " + e.getMessage());
                // Ignore
            }
        }

        try {
            if (clientInfoMap.containsKey(client)) {
                logger.debug("Removing the disconnected connection {} for host {}", clientInfoMap.get(client).getClientId(), host);
                clientInfoMap.remove(client);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void untrackClosedSessions(SSHClientWrapper client, int sessionId) {
        lock.writeLock().lock();

        try {
            if (clientInfoMap.containsKey(client)) {
                logger.debug("Removing the session for connection {} for host {}", clientInfoMap.get(client).getClientId(), host);
                SSHClientInfo sshClientInfo = clientInfoMap.get(client);
                sshClientInfo.setSessionCount(sshClientInfo.getSessionCount() - 1);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeStaleConnections() {
        List<Map.Entry<SSHClientWrapper, SSHClientInfo>> entriesTobeRemoved;
        lock.writeLock().lock();
        logger.info("Current active connections for  {} @ {} : {} are {}", username, host, port, clientInfoMap.size());
        try {
            entriesTobeRemoved = clientInfoMap.entrySet().stream().filter(entry ->
                    ((entry.getValue().getSessionCount() == 0) &&
                            (entry.getValue().getLastAccessedTime() + maxConnectionIdleTimeMS < System.currentTimeMillis()))).collect(Collectors.toList());
            entriesTobeRemoved.forEach(entry -> {
                logger.info("Removing connection {} due to inactivity for host {}", entry.getValue().getClientId(), host);
                clientInfoMap.remove(entry.getKey());
            });
        } finally {
            lock.writeLock().unlock();
        }

        entriesTobeRemoved.forEach(entry -> {
            try {
                entry.getKey().disconnect();
            } catch (IOException e) {
                logger.warn("Failed to disconnect connection {} for host {}", entry.getValue().clientId, host);
            }
        });
    }

    private SSHClientWrapper createNewSSHClient() throws IOException {

        SSHClientWrapper sshClient;
        if (config != null) {
            sshClient = new SSHClientWrapper(config);
        } else {
            sshClient = new SSHClientWrapper();
        }

        sshClient.getConnection().getTransport().setDisconnectListener(new DisconnectListener() {
            @Override
            public void notifyDisconnect(DisconnectReason reason, String message) {
                logger.warn("Connection disconnected " + message + " due to " + reason.name());
                removeDisconnectedClients(sshClient, false);
            }
        });

        if (hostKeyVerifier != null) {
            sshClient.addHostKeyVerifier(hostKeyVerifier);
        }

        sshClient.connect(host, port);

        sshClient.getConnection().getKeepAlive().setKeepAliveInterval(5); //send keep alive signal every 5sec

        if (authMethods != null) {
            sshClient.auth(username, authMethods);
        }

        return sshClient;
    }

    public SessionWrapper startSessionWrapper() throws Exception {

        final SSHClientWrapper sshClient = leaseSSHClient();

        try {
            return new SessionWrapper(sshClient.startSession(), (id) -> untrackClosedSessions(sshClient, id), sshClient);

        } catch (Exception e) {
            if (sshClient != null) {
                // If it is a ConnectionExceptions, explicitly invalidate the client
                if (e instanceof ConnectionException) {
                    sshClient.setErrored(true);
                }

                untrackClosedSessions(sshClient, -1);
            }
            throw e;
        }
    }

    public SCPFileTransferWrapper newSCPFileTransferWrapper() throws Exception {

        final SSHClientWrapper sshClient = leaseSSHClient();

        try {
            return new SCPFileTransferWrapper(sshClient.newSCPFileTransfer(), (id) -> untrackClosedSessions(sshClient, id), sshClient);

        } catch (Exception e) {

            if (sshClient != null) {
                // If it is a ConnectionExceptions, explicitly invalidate the client
                if (e instanceof ConnectionException) {
                    sshClient.setErrored(true);
                }

                untrackClosedSessions(sshClient, -1);
            }
            throw e;
        }
    }

    public SFTPClientWrapper newSFTPClientWrapper() throws Exception {

        final SSHClientWrapper sshClient = leaseSSHClient();

        try {
            return new SFTPClientWrapper(sshClient.newSFTPClient(), (id) -> untrackClosedSessions(sshClient, id), sshClient);
        } catch (Exception e) {

            if (sshClient != null) {
                // If it is a ConnectionExceptions, explicitly invalidate the client
                if (e instanceof ConnectionException) {
                    sshClient.setErrored(true);
                }

                untrackClosedSessions(sshClient, -1);
            }
            throw e;
        }
    }

    public class SSHClientInfo {

        private int sessionCount;
        private long lastAccessedTime;
        private int clientId;

        public SSHClientInfo(int sessionCount, long lastAccessedTime, int clientId) {
            this.sessionCount = sessionCount;
            this.lastAccessedTime = lastAccessedTime;
            this.clientId = clientId;
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

        public int getClientId() {
            return clientId;
        }

        public void setClientId(int clientId) {
            this.clientId = clientId;
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

    public Map<SSHClientWrapper, SSHClientInfo> getClientInfoMap() {
        return clientInfoMap;
    }
}
