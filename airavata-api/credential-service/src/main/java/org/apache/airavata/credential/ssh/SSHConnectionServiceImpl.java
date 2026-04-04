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
package org.apache.airavata.credential.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.apache.airavata.interfaces.SSHConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * sshj-based implementation of {@link SSHConnectionService}.
 */
@Service
public class SSHConnectionServiceImpl implements SSHConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(SSHConnectionServiceImpl.class);

    private static final HostKeyVerifier ACCEPT_ALL_VERIFIER = new HostKeyVerifier() {
        @Override
        public boolean verify(String hostname, int port, PublicKey key) {
            return true;
        }

        @Override
        public List<String> findExistingAlgorithms(String hostname, int port) {
            return Collections.emptyList();
        }
    };

    @Override
    public SSHConnection connect(
            String host, int port, String username, String publicKey, String privateKey, String passphrase)
            throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        PoolingSSHJClient sshjClient = new PoolingSSHJClient(defaultConfig, host, port == 0 ? 22 : port);
        sshjClient.addHostKeyVerifier(ACCEPT_ALL_VERIFIER);
        sshjClient.setMaxSessionsForConnection(1);

        PasswordFinder passwordFinder =
                passphrase != null ? PasswordUtils.createOneOff(passphrase.toCharArray()) : null;
        KeyProvider keyProvider = sshjClient.loadKeys(privateKey, publicKey, passwordFinder);

        final List<AuthMethod> am = new LinkedList<>();
        am.add(new AuthPublickey(keyProvider));
        am.add(new AuthKeyboardInteractive(new ChallengeResponseProvider() {
            @Override
            public List<String> getSubmethods() {
                return new ArrayList<>();
            }

            @Override
            public void init(Resource resource, String name, String instruction) {}

            @Override
            public char[] getResponse(String prompt, boolean echo) {
                return new char[0];
            }

            @Override
            public boolean shouldRetry() {
                return false;
            }
        }));
        sshjClient.auth(username, am);

        return new PoolingSSHConnectionImpl(sshjClient);
    }

    @Override
    public SSHConnection connectSimple(
            String host, int port, String username, String publicKey, String privateKey, String passphrase)
            throws IOException {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(ACCEPT_ALL_VERIFIER);
        ssh.connect(host, port == 0 ? 22 : port);

        PasswordFinder passwordFinder = passphrase != null && !passphrase.isEmpty()
                ? PasswordUtils.createOneOff(passphrase.toCharArray())
                : null;
        KeyProvider keyProvider = ssh.loadKeys(privateKey, publicKey, passwordFinder);
        ssh.authPublickey(username, keyProvider);

        return new SimpleSSHConnectionImpl(ssh);
    }

    @Override
    public SSHConnection connectWithPassword(String host, int port, String username, String password)
            throws IOException {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(ACCEPT_ALL_VERIFIER);
        ssh.connect(host, port == 0 ? 22 : port);
        ssh.authPassword(username, password);
        return new SimpleSSHConnectionImpl(ssh);
    }

    @Override
    public boolean validateCredential(
            String host, int port, String username, String publicKey, String privateKey, String passphrase) {
        SSHClient ssh = new SSHClient();
        try {
            ssh.addHostKeyVerifier(ACCEPT_ALL_VERIFIER);
            ssh.connect(host, port == 0 ? 22 : port);

            PasswordFinder passwordFinder = passphrase != null && !passphrase.isEmpty()
                    ? PasswordUtils.createOneOff(passphrase.toCharArray())
                    : null;
            KeyProvider keyProvider = ssh.loadKeys(privateKey, publicKey, passwordFinder);
            ssh.authPublickey(username, keyProvider);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                ssh.disconnect();
            } catch (IOException ignored) {
            }
        }
    }

    // ======================== Pooling connection implementation ========================

    private static class PoolingSSHConnectionImpl implements SSHConnection {
        private final PoolingSSHJClient client;

        PoolingSSHConnectionImpl(PoolingSSHJClient client) {
            this.client = client;
        }

        @Override
        public SSHSession startSession() throws IOException {
            try {
                SessionWrapper session = client.startSessionWrapper();
                return new PoolingSSHSessionImpl(session);
            } catch (Exception e) {
                throw new IOException("Failed to start SSH session", e);
            }
        }

        @Override
        public SFTPSession newSFTPClient() throws IOException {
            try {
                SFTPClientWrapper sftp = client.newSFTPClientWrapper();
                return new SSHJSFTPSessionImpl(sftp);
            } catch (Exception e) {
                throw new IOException("Failed to create SFTP client", e);
            }
        }

        @Override
        public SCPSession newSCPFileTransfer() throws IOException {
            try {
                SCPFileTransferWrapper scp = client.newSCPFileTransferWrapper();
                return new SSHJSCPSessionImpl(scp);
            } catch (Exception e) {
                throw new IOException("Failed to create SCP transfer", e);
            }
        }

        @Override
        public boolean isConnected() {
            return client.isConnected();
        }

        @Override
        public void disconnect() throws IOException {
            client.disconnect();
        }

        @Override
        public void close() throws IOException {
            client.disconnect();
            client.close();
        }
    }

    // ======================== Simple (non-pooling) connection implementation ========================

    private static class SimpleSSHConnectionImpl implements SSHConnection {
        private final SSHClient client;

        SimpleSSHConnectionImpl(SSHClient client) {
            this.client = client;
        }

        @Override
        public SSHSession startSession() throws IOException {
            Session session = client.startSession();
            return new SimpleSSHSessionImpl(session);
        }

        @Override
        public SFTPSession newSFTPClient() throws IOException {
            SFTPClient sftp = client.newSFTPClient();
            return new SimpleSFTPSessionImpl(sftp);
        }

        @Override
        public SCPSession newSCPFileTransfer() throws IOException {
            return new SimpleSCPSessionImpl(client);
        }

        @Override
        public boolean isConnected() {
            return client.isConnected();
        }

        @Override
        public void disconnect() throws IOException {
            client.disconnect();
        }

        @Override
        public void close() throws IOException {
            client.disconnect();
        }
    }

    // ======================== Session implementations ========================

    private static class PoolingSSHSessionImpl implements SSHSession {
        private final SessionWrapper session;

        PoolingSSHSessionImpl(SessionWrapper session) {
            this.session = session;
        }

        @Override
        public SSHCommandResult exec(String command) throws IOException {
            try {
                Session.Command cmd = session.exec(command);
                return new SSHJCommandResultImpl(cmd, session);
            } catch (Exception e) {
                throw new IOException("Failed to execute command", e);
            }
        }

        @Override
        public void setErrored(boolean errored) {
            session.setErrored(errored);
        }

        @Override
        public boolean isErrored() {
            return session.isErrored();
        }

        @Override
        public void close() throws IOException {
            try {
                session.close();
            } catch (Exception e) {
                throw new IOException("Failed to close session", e);
            }
        }
    }

    private static class SimpleSSHSessionImpl implements SSHSession {
        private final Session session;

        SimpleSSHSessionImpl(Session session) {
            this.session = session;
        }

        @Override
        public SSHCommandResult exec(String command) throws IOException {
            try {
                Session.Command cmd = session.exec(command);
                return new SimpleSSHCommandResultImpl(cmd, session);
            } catch (Exception e) {
                throw new IOException("Failed to execute command", e);
            }
        }

        @Override
        public void setErrored(boolean errored) {
            // no-op for simple sessions
        }

        @Override
        public boolean isErrored() {
            return false;
        }

        @Override
        public void close() throws IOException {
            try {
                session.close();
            } catch (Exception e) {
                throw new IOException("Failed to close session", e);
            }
        }
    }

    // ======================== Command result implementations ========================

    private static class SSHJCommandResultImpl implements SSHCommandResult {
        private final Session.Command cmd;
        private final SessionWrapper session;

        SSHJCommandResultImpl(Session.Command cmd, SessionWrapper session) {
            this.cmd = cmd;
            this.session = session;
        }

        @Override
        public InputStream getInputStream() {
            return cmd.getInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return cmd.getErrorStream();
        }

        @Override
        public void join(long timeout, TimeUnit unit) throws IOException {
            try {
                cmd.join(timeout, unit);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public Integer getExitStatus() {
            return cmd.getExitStatus();
        }

        @Override
        public void close() throws IOException {
            try {
                cmd.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private static class SimpleSSHCommandResultImpl implements SSHCommandResult {
        private final Session.Command cmd;
        private final Session session;

        SimpleSSHCommandResultImpl(Session.Command cmd, Session session) {
            this.cmd = cmd;
            this.session = session;
        }

        @Override
        public InputStream getInputStream() {
            return cmd.getInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return cmd.getErrorStream();
        }

        @Override
        public void join(long timeout, TimeUnit unit) throws IOException {
            try {
                cmd.join(timeout, unit);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public Integer getExitStatus() {
            return cmd.getExitStatus();
        }

        @Override
        public void close() throws IOException {
            try {
                cmd.close();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    // ======================== SFTP implementations ========================

    private static class SSHJSFTPSessionImpl implements SFTPSession {
        private final SFTPClientWrapper sftp;

        SSHJSFTPSessionImpl(SFTPClientWrapper sftp) {
            this.sftp = sftp;
        }

        @Override
        public void mkdir(String path) throws IOException {
            sftp.mkdir(path);
        }

        @Override
        public void mkdirs(String path) throws IOException {
            sftp.mkdirs(path);
        }

        @Override
        public void rmdir(String path) throws IOException {
            sftp.rmdir(path);
        }

        @Override
        public void rm(String path) throws IOException {
            sftp.rm(path);
        }

        @Override
        public List<RemoteFileInfo> ls(String path) throws IOException {
            return sftp.ls(path).stream()
                    .map(SSHConnectionServiceImpl::toRemoteFileInfo)
                    .collect(Collectors.toList());
        }

        @Override
        public RemoteFileAttributes statExistence(String path) throws IOException {
            FileAttributes attrs = sftp.statExistence(path);
            return attrs != null ? toRemoteFileAttributes(attrs) : null;
        }

        @Override
        public RemoteFileAttributes stat(String path) throws IOException {
            return toRemoteFileAttributes(sftp.stat(path));
        }

        @Override
        public RemoteFileAttributes lstat(String path) throws IOException {
            return toRemoteFileAttributes(sftp.lstat(path));
        }

        @Override
        public void setErrored(boolean errored) {
            sftp.setErrored(errored);
        }

        @Override
        public boolean isErrored() {
            return sftp.isErrored();
        }

        @Override
        public void close() throws IOException {
            sftp.close();
        }
    }

    private static class SimpleSFTPSessionImpl implements SFTPSession {
        private final SFTPClient sftp;

        SimpleSFTPSessionImpl(SFTPClient sftp) {
            this.sftp = sftp;
        }

        @Override
        public void mkdir(String path) throws IOException {
            sftp.mkdir(path);
        }

        @Override
        public void mkdirs(String path) throws IOException {
            sftp.mkdirs(path);
        }

        @Override
        public void rmdir(String path) throws IOException {
            sftp.rmdir(path);
        }

        @Override
        public void rm(String path) throws IOException {
            sftp.rm(path);
        }

        @Override
        public List<RemoteFileInfo> ls(String path) throws IOException {
            return sftp.ls(path).stream()
                    .map(SSHConnectionServiceImpl::toRemoteFileInfo)
                    .collect(Collectors.toList());
        }

        @Override
        public RemoteFileAttributes statExistence(String path) throws IOException {
            FileAttributes attrs = sftp.statExistence(path);
            return attrs != null ? toRemoteFileAttributes(attrs) : null;
        }

        @Override
        public RemoteFileAttributes stat(String path) throws IOException {
            return toRemoteFileAttributes(sftp.stat(path));
        }

        @Override
        public RemoteFileAttributes lstat(String path) throws IOException {
            return toRemoteFileAttributes(sftp.lstat(path));
        }

        @Override
        public void setErrored(boolean errored) {
            /* no-op */
        }

        @Override
        public boolean isErrored() {
            return false;
        }

        @Override
        public void close() throws IOException {
            sftp.close();
        }
    }

    // ======================== SCP implementations ========================

    private static class SSHJSCPSessionImpl implements SCPSession {
        private final SCPFileTransferWrapper scp;

        SSHJSCPSessionImpl(SCPFileTransferWrapper scp) {
            this.scp = scp;
        }

        @Override
        public void upload(String localPath, String remotePath) throws IOException {
            scp.upload(localPath, remotePath);
        }

        @Override
        public void download(String remotePath, String localPath) throws IOException {
            scp.download(remotePath, localPath);
        }

        @Override
        public void upload(SSHLocalSourceFile localFile, String remotePath) throws IOException {
            scp.upload(toSshjLocalSourceFile(localFile), remotePath);
        }

        @Override
        public void download(String remotePath, SSHLocalDestFile localFile) throws IOException {
            scp.download(remotePath, toSshjLocalDestFile(localFile));
        }

        @Override
        public void setErrored(boolean errored) {
            scp.setErrored(errored);
        }

        @Override
        public boolean isErrored() {
            return scp.isErrored();
        }

        @Override
        public void close() throws IOException {
            scp.close();
        }
    }

    private static class SimpleSCPSessionImpl implements SCPSession {
        private final SSHClient client;

        SimpleSCPSessionImpl(SSHClient client) {
            this.client = client;
        }

        @Override
        public void upload(String localPath, String remotePath) throws IOException {
            client.newSCPFileTransfer().upload(localPath, remotePath);
        }

        @Override
        public void download(String remotePath, String localPath) throws IOException {
            client.newSCPFileTransfer().download(remotePath, localPath);
        }

        @Override
        public void upload(SSHLocalSourceFile localFile, String remotePath) throws IOException {
            client.newSCPFileTransfer().upload(toSshjLocalSourceFile(localFile), remotePath);
        }

        @Override
        public void download(String remotePath, SSHLocalDestFile localFile) throws IOException {
            client.newSCPFileTransfer().download(remotePath, toSshjLocalDestFile(localFile));
        }

        @Override
        public void setErrored(boolean errored) {
            /* no-op */
        }

        @Override
        public boolean isErrored() {
            return false;
        }

        @Override
        public void close() throws IOException {
            /* no-op for simple */
        }
    }

    // ======================== Conversion helpers ========================

    private static RemoteFileInfo toRemoteFileInfo(RemoteResourceInfo rri) {
        return new RemoteFileInfo() {
            @Override
            public String getName() {
                return rri.getName();
            }

            @Override
            public String getPath() {
                return rri.getPath();
            }

            @Override
            public boolean isDirectory() {
                return rri.isDirectory();
            }
        };
    }

    private static RemoteFileAttributes toRemoteFileAttributes(FileAttributes attrs) {
        return new RemoteFileAttributes() {
            @Override
            public long getSize() {
                return attrs.getSize();
            }

            @Override
            public int getPermissions() {
                return FilePermission.toMask(attrs.getPermissions());
            }

            @Override
            public boolean isDirectory() {
                return attrs.getType() == FileMode.Type.DIRECTORY;
            }
        };
    }

    private static LocalSourceFile toSshjLocalSourceFile(SSHLocalSourceFile src) {
        return new LocalSourceFile() {
            @Override
            public String getName() {
                return src.getName();
            }

            @Override
            public long getLength() {
                return src.getLength();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return src.getInputStream();
            }

            @Override
            public int getPermissions() throws IOException {
                return src.getPermissions();
            }

            @Override
            public boolean isFile() {
                return src.isFile();
            }

            @Override
            public boolean isDirectory() {
                return src.isDirectory();
            }

            @Override
            public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter) {
                return null;
            }

            @Override
            public boolean providesAtimeMtime() {
                return false;
            }

            @Override
            public long getLastAccessTime() {
                return 0;
            }

            @Override
            public long getLastModifiedTime() {
                return 0;
            }
        };
    }

    private static LocalDestFile toSshjLocalDestFile(SSHLocalDestFile dest) {
        return new LocalDestFile() {
            @Override
            public long getLength() {
                return dest.getLength();
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return dest.getOutputStream();
            }

            @Override
            public OutputStream getOutputStream(boolean append) throws IOException {
                return dest.getOutputStream(append);
            }

            @Override
            public LocalDestFile getChild(String name) {
                return null;
            }

            @Override
            public LocalDestFile getTargetFile(String filename) throws IOException {
                SSHLocalDestFile target = dest.getTargetFile(filename);
                return target != null ? toSshjLocalDestFile(target) : this;
            }

            @Override
            public LocalDestFile getTargetDirectory(String dirname) {
                return null;
            }

            @Override
            public void setPermissions(int perms) {}

            @Override
            public void setLastAccessedTime(long t) {}

            @Override
            public void setLastModifiedTime(long t) {}
        };
    }
}
