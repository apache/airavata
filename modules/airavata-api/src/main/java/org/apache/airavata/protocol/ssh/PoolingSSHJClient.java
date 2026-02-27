/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.protocol.ssh;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.airavata.core.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will keep a pool of {@link SSHClient} and scale them according to the number of SSH requests.
 * This pool is MaxSessions per connection aware and thread safe. It is intelligent to decide the number of connections
 * that it should create and number of sessions should be used in each created connection to avoid possible connection
 * refusals from the server side.
 */
public class PoolingSSHJClient extends SSHClient {

    private static final Logger logger = LoggerFactory.getLogger(PoolingSSHJClient.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<SSHClient, SSHClientInfo> clientInfoMap = new HashMap<>();
    private final Set<SSHClient> erroredClients = new HashSet<>();

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

        poolMonitoringService.scheduleWithFixedDelay(
                this::removeStaleConnections, 10, maxConnectionIdleTimeMS * 2, TimeUnit.MILLISECONDS);
    }

    ////////////////// client specific operations ///////

    private SSHClient newClientWithSessionValidation() throws IOException {
        var newClient = createNewSSHClient();
        var info = new SSHClientInfo(1, IdGenerator.getUniqueTimestamp().toEpochMilli(), clientInfoMap.size());
        clientInfoMap.put(newClient, info);

        /* if this is the very first connection that is created to the compute host, fetch the MaxSessions
         * value form SSHD config file in order to tune the pool
         */
        logger.info("Fetching max sessions for the connection of {}", host);
        try (var sftpClient = newClient.newSFTPClient()) {
            var remoteFile = sftpClient.open("/etc/ssh/sshd_config");
            var readContent = new byte[(int) remoteFile.length()];
            remoteFile.read(0, readContent, 0, readContent.length);

            var content = new String(readContent, java.nio.charset.StandardCharsets.UTF_8);
            logger.trace("SSHD config file content : {}", content);
            var lines = content.split("\n");

            for (var line : lines) {
                if (line.trim().startsWith("MaxSessions")) {
                    var splits = line.split(" ");
                    if (splits.length == 2) {
                        int sessionCount = Integer.parseInt(splits[1]);
                        logger.info("Max session count is : {} for {}", sessionCount, host);
                        setMaxSessionsForConnection(sessionCount);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Failed to fetch max session count for {}. Continuing with default value 1. {}",
                    host,
                    e.getMessage());
        }
        return newClient;
    }

    private SSHClient leaseSSHClient() throws Exception {
        lock.writeLock().lock();
        try {
            if (clientInfoMap.isEmpty()) {
                return newClientWithSessionValidation();
            }

            var minEntry = clientInfoMap.entrySet().stream()
                    .min(Comparator.comparing(entry -> entry.getValue().sessionCount))
                    .orElseThrow(() -> new Exception("Failed to find a connection in the pool for host " + host));

            var info = minEntry.getValue();
            logger.debug(
                    "Session count for selected connection {} is {}. Threshold {} for host {}",
                    info.getClientId(),
                    info.getSessionCount(),
                    maxSessionsForConnection,
                    host);

            if (info.getSessionCount() >= maxSessionsForConnection) {
                logger.debug(
                        "Connection with least amount of sessions exceeds the threshold. "
                                + "Creating a new connection. Current connection count {} for host {}",
                        clientInfoMap.size(),
                        host);
                return newClientWithSessionValidation();
            }

            logger.debug("Reusing connection {} for host {}", info.getClientId(), host);
            info.setSessionCount(info.getSessionCount() + 1);
            info.setLastAccessedTime(IdGenerator.getUniqueTimestamp().toEpochMilli());

            var sshClient = minEntry.getKey();
            if (!sshClient.isConnected() || !sshClient.isAuthenticated() || isClientErrored(sshClient)) {
                logger.warn("Client for host {} is not connected or not authenticated. Creating a new client", host);
                removeDisconnectedClients(sshClient, true);
                return newClientWithSessionValidation();
            }
            return sshClient;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeDisconnectedClients(SSHClient client, boolean doDisconnect) {
        lock.writeLock().lock();

        if (doDisconnect) {
            try {
                client.disconnect();
            } catch (Exception e) {
                logger.warn("Errored while disconnecting the client {}", e.getMessage());
                // Ignore
            }
        }

        try {
            if (clientInfoMap.containsKey(client)) {
                logger.debug(
                        "Removing the disconnected connection {} for host {}",
                        clientInfoMap.get(client).getClientId(),
                        host);
                clientInfoMap.remove(client);
                erroredClients.remove(client);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    public void untrackClosedSessions(SSHClient client) {
        lock.writeLock().lock();

        try {
            if (clientInfoMap.containsKey(client)) {
                logger.debug(
                        "Removing the session for connection {} for host {}",
                        clientInfoMap.get(client).getClientId(),
                        host);
                SSHClientInfo sshClientInfo = clientInfoMap.get(client);
                sshClientInfo.setSessionCount(sshClientInfo.getSessionCount() - 1);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeStaleConnections() {
        List<Map.Entry<SSHClient, SSHClientInfo>> entriesTobeRemoved;
        lock.writeLock().lock();
        logger.info("Current active connections for {} @ {}:{} are {}", username, host, port, clientInfoMap.size());
        try {
            entriesTobeRemoved = clientInfoMap.entrySet().stream()
                    .filter(entry -> ((entry.getValue().getSessionCount() == 0)
                            && (entry.getValue().getLastAccessedTime() + maxConnectionIdleTimeMS
                                    < IdGenerator.getUniqueTimestamp().toEpochMilli())))
                    .toList();
            entriesTobeRemoved.forEach(entry -> {
                logger.info(
                        "Removing connection {} due to inactivity for host {}",
                        entry.getValue().getClientId(),
                        host);
                clientInfoMap.remove(entry.getKey());
            });
        } finally {
            lock.writeLock().unlock();
        }

        entriesTobeRemoved.forEach(entry -> {
            try {
                entry.getKey().disconnect();
                erroredClients.remove(entry.getKey());
            } catch (IOException e) {
                logger.warn("Failed to disconnect connection {} for host {}", entry.getValue().clientId, host);
            }
        });
    }

    private SSHClient createNewSSHClient() throws IOException {
        var sshClient = new SSHClient(config);

        sshClient.getConnection().getTransport().setDisconnectListener((reason, message) -> {
            logger.warn("Connection disconnected {} due to {}", message, reason.name());
            removeDisconnectedClients(sshClient, false);
        });

        if (hostKeyVerifier != null) {
            sshClient.addHostKeyVerifier(hostKeyVerifier);
        }

        sshClient.connect(host, port);

        sshClient.getConnection().getKeepAlive().setKeepAliveInterval(5); // send keep alive signal every 5sec

        if (authMethods != null) {
            sshClient.auth(username, authMethods);
        }

        return sshClient;
    }

    public static class SessionResource {
        private final Session session;
        private final SSHClient sshClient;
        private final PoolingSSHJClient pool;

        SessionResource(Session session, SSHClient sshClient, PoolingSSHJClient pool) {
            this.session = session;
            this.sshClient = sshClient;
            this.pool = pool;
        }

        public Session getSession() {
            return session;
        }

        public void close() throws Exception {
            session.close();
            pool.untrackClosedSessions(sshClient);
        }

        public void markErrored() {
            pool.markClientErrored(sshClient);
        }
    }

    public static class SCPFileTransferResource {
        private final SCPFileTransfer fileTransfer;
        private final SSHClient sshClient;
        private final PoolingSSHJClient pool;

        SCPFileTransferResource(SCPFileTransfer fileTransfer, SSHClient sshClient, PoolingSSHJClient pool) {
            this.fileTransfer = fileTransfer;
            this.sshClient = sshClient;
            this.pool = pool;
        }

        public SCPFileTransfer getFileTransfer() {
            return fileTransfer;
        }

        public void close() throws Exception {
            // SCPFileTransfer doesn't need explicit closing, just track the session
            pool.untrackClosedSessions(sshClient);
        }

        public void markErrored() {
            pool.markClientErrored(sshClient);
        }
    }

    public static class SFTPClientResource {
        private final SFTPClient sftpClient;
        private final SSHClient sshClient;
        private final PoolingSSHJClient pool;

        SFTPClientResource(SFTPClient sftpClient, SSHClient sshClient, PoolingSSHJClient pool) {
            this.sftpClient = sftpClient;
            this.sshClient = sshClient;
            this.pool = pool;
        }

        public SFTPClient getSFTPClient() {
            return sftpClient;
        }

        public void close() throws Exception {
            sftpClient.close();
            pool.untrackClosedSessions(sshClient);
        }

        public void markErrored() {
            pool.markClientErrored(sshClient);
        }
    }

    @FunctionalInterface
    private interface ResourceFactory<T> {
        T create(SSHClient client) throws Exception;
    }

    private <T> T leaseAndCreate(ResourceFactory<T> factory) throws Exception {
        var sshClient = leaseSSHClient();
        try {
            return factory.create(sshClient);
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                markClientErrored(sshClient);
            }
            untrackClosedSessions(sshClient);
            throw e;
        }
    }

    public SessionResource startSessionResource() throws Exception {
        return leaseAndCreate(client -> new SessionResource(client.startSession(), client, this));
    }

    public SCPFileTransferResource newSCPFileTransferResource() throws Exception {
        return leaseAndCreate(client -> new SCPFileTransferResource(client.newSCPFileTransfer(), client, this));
    }

    public SFTPClientResource newSFTPClientResource() throws Exception {
        return leaseAndCreate(client -> new SFTPClientResource(client.newSFTPClient(), client, this));
    }

    public void markClientErrored(SSHClient client) {
        lock.writeLock().lock();
        try {
            erroredClients.add(client);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isClientErrored(SSHClient client) {
        lock.readLock().lock();
        try {
            return erroredClients.contains(client);
        } finally {
            lock.readLock().unlock();
        }
    }

    public class SSHClientInfo {

        private int sessionCount;
        private long lastAccessedTime;
        private final int clientId;

        public SSHClientInfo(int sessionCount, long lastAccessedTime, int clientId) {
            this.sessionCount = sessionCount;
            this.lastAccessedTime = lastAccessedTime;
            this.clientId = clientId;
        }

        public int getSessionCount() {
            return sessionCount;
        }

        public void setSessionCount(int sessionCount) {
            this.sessionCount = sessionCount;
        }

        public long getLastAccessedTime() {
            return lastAccessedTime;
        }

        public void setLastAccessedTime(long lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
        }

        public int getClientId() {
            return clientId;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public void setMaxSessionsForConnection(int maxSessionsForConnection) {
        this.maxSessionsForConnection = maxSessionsForConnection;
    }
}
