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
package org.apache.airavata.agents.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageResourceAdaptor extends AgentAdaptor {
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException;
    public void uploadFile(String localFile, String remoteFile) throws AgentException;
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException;
    public void downloadFile(String remoteFile, String localFile) throws AgentException;
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata) throws AgentException;
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException;
    public void createDirectory(String path) throws AgentException;
    public void createDirectory(String path, boolean recursive) throws AgentException;
    public List<String> listDirectory(String path) throws AgentException;
    public Boolean doesFileExist(String filePath) throws AgentException;
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException;
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException;
}
