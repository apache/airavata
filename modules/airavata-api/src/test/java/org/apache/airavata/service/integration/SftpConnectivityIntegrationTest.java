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
package org.apache.airavata.service.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.InMemoryDestFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import org.apache.airavata.config.TestcontainersConfig;
import org.apache.airavata.config.TestcontainersConfig.SftpConnectionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Integration tests for SFTP connectivity.
 * These tests verify SFTP connection and file transfer operations against the SFTP container.
 * The SFTP container is fully managed by Testcontainers.
 */
@DisplayName("SFTP Connectivity Integration Tests")
@Timeout(value = 2, unit = TimeUnit.MINUTES) // Prevent tests from hanging indefinitely
public class SftpConnectivityIntegrationTest {

    private SSHClient sshClient;
    private SFTPClient sftpClient;

    @BeforeEach
    void setUp() throws IOException {
        // Get SFTP container from TestcontainersConfig - starts container if not running
        SftpConnectionInfo sftp = TestcontainersConfig.getSftpContainer();

        sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        // Set connection timeout to prevent hanging on connection issues
        sshClient.setConnectTimeout(10000); // 10 seconds
        sshClient.setTimeout(30000); // 30 seconds for socket operations
        sshClient.connect(sftp.host(), sftp.port());
        sshClient.authPassword(sftp.user(), sftp.password());
        sftpClient = sshClient.newSFTPClient();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (sftpClient != null) {
            sftpClient.close();
        }
        if (sshClient != null && sshClient.isConnected()) {
            sshClient.disconnect();
        }
    }

    @Nested
    @DisplayName("SFTP Connection Tests")
    class SftpConnectionTests {

        @Test
        @DisplayName("Should connect to SFTP host")
        void shouldConnectToSftpHost() {
            assertThat(sshClient.isConnected()).isTrue();
            assertThat(sshClient.isAuthenticated()).isTrue();
            assertThat(sftpClient).isNotNull();
        }

        @Test
        @DisplayName("Should get SFTP server version")
        void shouldGetSftpVersion() {
            assertThat(sftpClient.version()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("File Transfer Tests")
    class FileTransferTests {

        // The atmoz/sftp container uses chroot, so paths are relative to user's home
        private static final String UPLOAD_DIR = "/upload";

        @Test
        @DisplayName("Should upload file to SFTP server")
        void shouldUploadFile() throws IOException {
            String fileName = "test-upload-" + UUID.randomUUID() + ".txt";
            String fileContent = "Hello from SFTP upload test!";
            String remotePath = UPLOAD_DIR + "/" + fileName;

            // Upload file
            sftpClient.put(
                    new InMemorySourceFile() {
                        @Override
                        public String getName() {
                            return fileName;
                        }

                        @Override
                        public long getLength() {
                            return fileContent.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public java.io.InputStream getInputStream() {
                            return new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
                        }
                    },
                    remotePath);

            // Verify file exists
            assertThat(sftpClient.statExistence(remotePath)).isNotNull();

            // Cleanup
            sftpClient.rm(remotePath);
        }

        @Test
        @DisplayName("Should download file from SFTP server")
        void shouldDownloadFile() throws IOException {
            String fileName = "test-download-" + UUID.randomUUID() + ".txt";
            String fileContent = "Hello from SFTP download test!";
            String remotePath = UPLOAD_DIR + "/" + fileName;

            // First upload a file
            sftpClient.put(
                    new InMemorySourceFile() {
                        @Override
                        public String getName() {
                            return fileName;
                        }

                        @Override
                        public long getLength() {
                            return fileContent.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public java.io.InputStream getInputStream() {
                            return new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
                        }
                    },
                    remotePath);

            // Download the file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            sftpClient.get(remotePath, new InMemoryDestFile() {
                @Override
                public java.io.OutputStream getOutputStream() {
                    return outputStream;
                }

                @Override
                public java.io.OutputStream getOutputStream(boolean append) {
                    return outputStream;
                }

                @Override
                public long getLength() {
                    return outputStream.size();
                }
            });

            // Verify content
            String downloadedContent = outputStream.toString(StandardCharsets.UTF_8);
            assertThat(downloadedContent).isEqualTo(fileContent);

            // Cleanup
            sftpClient.rm(remotePath);
        }

        @Test
        @DisplayName("Should list directory contents")
        void shouldListDirectory() throws IOException {
            String fileName = "test-list-" + UUID.randomUUID() + ".txt";
            String fileContent = "Test file for listing";
            String remotePath = UPLOAD_DIR + "/" + fileName;

            // Upload a test file
            sftpClient.put(
                    new InMemorySourceFile() {
                        @Override
                        public String getName() {
                            return fileName;
                        }

                        @Override
                        public long getLength() {
                            return fileContent.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public java.io.InputStream getInputStream() {
                            return new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
                        }
                    },
                    remotePath);

            // List directory
            List<RemoteResourceInfo> files = sftpClient.ls(UPLOAD_DIR);

            // Verify our file is in the list
            assertThat(files).isNotEmpty();
            assertThat(files.stream().anyMatch(f -> f.getName().equals(fileName)))
                    .isTrue();

            // Cleanup
            sftpClient.rm(remotePath);
        }

        @Test
        @DisplayName("Should delete file from SFTP server")
        void shouldDeleteFile() throws IOException {
            String fileName = "test-delete-" + UUID.randomUUID() + ".txt";
            String fileContent = "File to be deleted";
            String remotePath = UPLOAD_DIR + "/" + fileName;

            // Upload a file
            sftpClient.put(
                    new InMemorySourceFile() {
                        @Override
                        public String getName() {
                            return fileName;
                        }

                        @Override
                        public long getLength() {
                            return fileContent.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public java.io.InputStream getInputStream() {
                            return new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
                        }
                    },
                    remotePath);

            // Verify file exists
            assertThat(sftpClient.statExistence(remotePath)).isNotNull();

            // Delete the file
            sftpClient.rm(remotePath);

            // Verify file no longer exists
            assertThat(sftpClient.statExistence(remotePath)).isNull();
        }

        @Test
        @DisplayName("Should create and remove directory")
        void shouldCreateAndRemoveDirectory() throws IOException {
            String dirName = "test-dir-" + UUID.randomUUID();
            String remotePath = UPLOAD_DIR + "/" + dirName;

            // Create directory
            sftpClient.mkdir(remotePath);

            // Verify directory exists
            assertThat(sftpClient.statExistence(remotePath)).isNotNull();

            // Remove directory
            sftpClient.rmdir(remotePath);

            // Verify directory no longer exists
            assertThat(sftpClient.statExistence(remotePath)).isNull();
        }
    }

    @Nested
    @DisplayName("File Attribute Tests")
    class FileAttributeTests {

        // The atmoz/sftp container uses chroot, so paths are relative to user's home
        private static final String UPLOAD_DIR = "/upload";

        @Test
        @DisplayName("Should get file attributes")
        void shouldGetFileAttributes() throws IOException {
            String fileName = "test-attrs-" + UUID.randomUUID() + ".txt";
            String fileContent = "Test file for attributes";
            String remotePath = UPLOAD_DIR + "/" + fileName;

            // Upload a file
            sftpClient.put(
                    new InMemorySourceFile() {
                        @Override
                        public String getName() {
                            return fileName;
                        }

                        @Override
                        public long getLength() {
                            return fileContent.getBytes(StandardCharsets.UTF_8).length;
                        }

                        @Override
                        public java.io.InputStream getInputStream() {
                            return new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));
                        }
                    },
                    remotePath);

            // Get file attributes
            var attrs = sftpClient.stat(remotePath);

            assertThat(attrs).isNotNull();
            assertThat(attrs.getSize()).isEqualTo(fileContent.getBytes(StandardCharsets.UTF_8).length);

            // Cleanup
            sftpClient.rm(remotePath);
        }
    }
}
