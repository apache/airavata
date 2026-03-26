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
package org.apache.airavata.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.apache.airavata.storage.resource.model.StorageDirectoryInfo;
import org.apache.airavata.storage.resource.model.StorageVolumeInfo;

/**
 * Adapter for job submission and file operations on a compute resource.
 */
public interface AgentAdapter {

    void init(String computeResource, String gatewayId, String userId, String token) throws AgentException;

    void destroy();

    CommandOutput executeCommand(String command, String workingDirectory) throws AgentException;

    void createDirectory(String path) throws AgentException;

    void createDirectory(String path, boolean recursive) throws AgentException;

    void deleteDirectory(String path) throws AgentException;

    void uploadFile(String localFile, String remoteFile) throws AgentException;

    void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException;

    void downloadFile(String remoteFile, String localFile) throws AgentException;

    void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata) throws AgentException;

    List<String> listDirectory(String path) throws AgentException;

    Boolean doesFileExist(String filePath) throws AgentException;

    List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException;

    FileMetadata getFileMetadata(String remoteFile) throws AgentException;

    StorageVolumeInfo getStorageVolumeInfo(String location) throws AgentException;

    StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException;

    // -------------------------------------------------------------------------
    // Nested types
    // -------------------------------------------------------------------------

    record CommandOutput(String stdOut, String stdError, int exitCode) {

        public String getStdOut() {
            return stdOut;
        }

        public String getStdError() {
            return stdError;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    record FileMetadata(String name, long size, int permissions, boolean isDirectory) {

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public int getPermissions() {
            return permissions;
        }

        public boolean isDirectory() {
            return isDirectory;
        }
    }

    class AgentException extends Exception {

        public AgentException(String message) {
            super(message);
        }

        public AgentException(String message, Throwable cause) {
            super(message, cause);
        }

        public AgentException(Throwable cause) {
            super(cause);
        }
    }
}
