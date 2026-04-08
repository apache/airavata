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
package org.apache.airavata.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * SPI contract for SSH connection operations.
 *
 * <p>Implementations manage SSH client lifecycle, authentication, and provide
 * session/SFTP/SCP capabilities. Modules that need SSH access depend only on
 * this interface; the credential-service module provides the sshj-based implementation.
 */
public interface SSHConnectionService {

    /**
     * Creates a pooling SSH connection with public-key authentication.
     *
     * @return a connected SSHConnection that can be used to start sessions, SFTP, and SCP
     */
    SSHConnection connect(
            String host, int port, String username, String publicKey, String privateKey, String passphrase)
            throws IOException;

    /**
     * Creates a simple (non-pooling) SSH connection with public-key authentication.
     *
     * @return a connected SSHConnection
     */
    SSHConnection connectSimple(
            String host, int port, String username, String publicKey, String privateKey, String passphrase)
            throws IOException;

    /**
     * Creates a simple SSH connection with password authentication.
     *
     * @return a connected SSHConnection
     */
    SSHConnection connectWithPassword(String host, int port, String username, String password) throws IOException;

    /**
     * Validates that SSH credentials can authenticate to a given host.
     *
     * @return true if authentication succeeds
     */
    boolean validateCredential(
            String host, int port, String username, String publicKey, String privateKey, String passphrase);

    /**
     * Represents an established SSH connection that can create sessions, SFTP clients,
     * and SCP file transfers.
     */
    interface SSHConnection extends Closeable {

        SSHSession startSession() throws IOException;

        SFTPSession newSFTPClient() throws IOException;

        SCPSession newSCPFileTransfer() throws IOException;

        /**
         * @return true if the connection is still open and authenticated
         */
        boolean isConnected();

        void disconnect() throws IOException;
    }

    /**
     * Represents an SSH session for command execution.
     */
    interface SSHSession extends Closeable {

        /**
         * Executes a command and returns the result.
         */
        SSHCommandResult exec(String command) throws IOException;

        /**
         * Marks this session as errored (for connection-pool awareness).
         */
        void setErrored(boolean errored);

        boolean isErrored();
    }

    /**
     * Result of an SSH command execution.
     */
    interface SSHCommandResult extends Closeable {

        InputStream getInputStream();

        InputStream getErrorStream();

        /**
         * Waits for the command to complete with the given timeout.
         */
        void join(long timeout, java.util.concurrent.TimeUnit unit) throws IOException;

        /**
         * @return the exit status, or null if not yet available
         */
        Integer getExitStatus();

        @Override
        void close() throws IOException;
    }

    /**
     * Represents an SFTP session for file operations.
     */
    interface SFTPSession extends Closeable {

        void mkdir(String path) throws IOException;

        void mkdirs(String path) throws IOException;

        void rmdir(String path) throws IOException;

        void rm(String path) throws IOException;

        List<RemoteFileInfo> ls(String path) throws IOException;

        /**
         * @return file attributes, or null if the file does not exist (for statExistence)
         */
        RemoteFileAttributes statExistence(String path) throws IOException;

        RemoteFileAttributes stat(String path) throws IOException;

        RemoteFileAttributes lstat(String path) throws IOException;

        void setErrored(boolean errored);

        boolean isErrored();
    }

    /**
     * Represents an SCP file transfer session.
     */
    interface SCPSession extends Closeable {

        void upload(String localPath, String remotePath) throws IOException;

        void download(String remotePath, String localPath) throws IOException;

        void upload(SSHLocalSourceFile localFile, String remotePath) throws IOException;

        void download(String remotePath, SSHLocalDestFile localFile) throws IOException;

        void setErrored(boolean errored);

        boolean isErrored();
    }

    /**
     * Abstraction for a local source file (for streaming uploads).
     */
    interface SSHLocalSourceFile {
        String getName();

        long getLength();

        InputStream getInputStream() throws IOException;

        int getPermissions() throws IOException;

        boolean isFile();

        boolean isDirectory();
    }

    /**
     * Abstraction for a local destination file (for streaming downloads).
     */
    interface SSHLocalDestFile {
        long getLength();

        OutputStream getOutputStream() throws IOException;

        OutputStream getOutputStream(boolean append) throws IOException;

        SSHLocalDestFile getTargetFile(String filename) throws IOException;
    }

    /**
     * Information about a remote file/directory entry from an ls() call.
     */
    interface RemoteFileInfo {
        String getName();

        String getPath();

        boolean isDirectory();
    }

    /**
     * Attributes of a remote file.
     */
    interface RemoteFileAttributes {
        long getSize();

        int getPermissions();

        boolean isDirectory();
    }
}
