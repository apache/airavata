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
package org.apache.airavata.file.service;

import java.nio.file.Path;
import org.apache.airavata.file.model.AiravataDirectory;
import org.apache.airavata.file.model.AiravataFile;
import org.apache.airavata.protocol.AgentAdapter.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for browsing and transferring files on remote compute resources
 * via process-scoped SSH adapters.
 */
public interface FileService {

    FileMetadata getInfo(String processId, String subPath) throws Exception;

    AiravataDirectory listDir(String processId, String subPath) throws Exception;

    AiravataFile listFile(String processId, String subPath) throws Exception;

    void uploadFile(String processId, String subPath, MultipartFile file) throws Exception;

    Path downloadFile(String processId, String subPath) throws Exception;
}
