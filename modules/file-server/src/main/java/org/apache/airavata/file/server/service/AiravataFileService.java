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
package org.apache.airavata.file.server.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.file.server.model.AiravataDirectory;
import org.apache.airavata.file.server.model.AiravataFile;
import org.apache.airavata.protocol.AdapterSupport;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.FileMetadata;
import org.apache.airavata.research.experiment.service.ExperimentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("fileServerFileService")
public class AiravataFileService {

    private static final Logger logger = LoggerFactory.getLogger(AiravataFileService.class);

    private final AdapterSupport adapterSupport;
    private final ProcessService processService;
    private final ExperimentService experimentService;

    public AiravataFileService(
            AdapterSupport adapterSupport, ProcessService processService, ExperimentService experimentService) {
        this.adapterSupport = adapterSupport;
        this.processService = processService;
        this.experimentService = experimentService;
    }

    private ProcessDataManager createDataManager(String processId) throws Exception {
        return new ProcessDataManager(processService, experimentService, processId, adapterSupport);
    }

    private AgentAdapter getAgentAdapter(ProcessDataManager dataManager, String processId) throws Exception {
        AgentAdapter agentAdapter;
        try {
            agentAdapter = dataManager.getAgentAdapter();
        } catch (Exception e) {
            logger.error("Failed to fetch adapter for process {}", processId, e);
            throw new Exception("Failed to fetch adapter for process " + processId, e);
        }
        return agentAdapter;
    }

    public FileMetadata getInfo(String processId, String subPath) throws Exception {
        var dataManager = createDataManager(processId);
        var agentAdapter = getAgentAdapter(dataManager, processId);
        String absPath = dataManager.getBaseDir() + subPath;

        logger.info("Getting metadata for path {}", absPath);
        return agentAdapter.getFileMetadata(absPath);
    }

    public AiravataDirectory listDir(String processId, String subPath) throws Exception {
        var dataManager = createDataManager(processId);
        var agentAdapter = getAgentAdapter(dataManager, processId);

        String absPath = dataManager.getBaseDir() + subPath;
        logger.info("Getting metadata for path {}", absPath);
        var rm = agentAdapter.getFileMetadata(absPath);
        if (!rm.isDirectory()) {
            throw new Exception("Path " + absPath + " is not a directory");
        }

        var airavataDirectory = AiravataDirectory.fromMetadata(rm);
        logger.info("Listing files in path {}", absPath);
        var fileList = agentAdapter.listDirectory(absPath);
        for (String fileOrDir : fileList) {
            logger.info("Getting metadata for path {}", fileOrDir);
            var m = agentAdapter.getFileMetadata(absPath + "/" + fileOrDir);
            if (m.isDirectory()) {
                airavataDirectory.getInnerDirectories().add(AiravataDirectory.fromMetadata(m));
            } else {
                airavataDirectory.getInnerFiles().add(AiravataFile.fromMetadata(m));
            }
        }

        return airavataDirectory;
    }

    public AiravataFile listFile(String processId, String subPath) throws Exception {
        var dataManager = createDataManager(processId);
        var agentAdapter = getAgentAdapter(dataManager, processId);

        String absPath = dataManager.getBaseDir() + subPath;

        var info = agentAdapter.getFileMetadata(absPath);
        return AiravataFile.fromMetadata(info);
    }

    public void uploadFile(String processId, String subPath, MultipartFile file) throws Exception {

        var tempPath = Files.createTempFile("tempfile_", ".data").toAbsolutePath();
        var metadata = new FileMetadata();
        metadata.setName(file.getName());
        metadata.setSize(file.getSize());
        try (var fileInput = file.getInputStream()) {
            Files.copy(fileInput, tempPath, StandardCopyOption.REPLACE_EXISTING);
        }

        var dataManager = createDataManager(processId);
        var agentAdapter = getAgentAdapter(dataManager, processId);
        String absPath = dataManager.getBaseDir() + subPath;

        try (var content = Files.newInputStream(tempPath)) {
            agentAdapter.createDirectory(new File(absPath).getParent(), true);
            logger.info("Uploading file {}:{} to {}:{}", "temp", metadata.getName(), processId, subPath);
            agentAdapter.uploadFile(content, metadata, absPath);
            logger.info("Uploaded file {}:{} to {}:{}", "temp", metadata.getName(), processId, subPath);
        } catch (Exception e) {
            logger.error("Failed to upload file {}:{} to {}:{}", "temp", metadata.getName(), processId, subPath);
            throw e;
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    public Path downloadFile(String processId, String subPath) throws Exception {

        var dataManager = createDataManager(processId);
        var agentAdapter = getAgentAdapter(dataManager, processId);
        String absPath = dataManager.getBaseDir() + subPath;

        if (agentAdapter.doesFileExist(absPath)) {
            var tempPath = Files.createTempFile("tempfile_", ".data").toAbsolutePath();
            try {
                logger.info("Downloading file {}:{} to {}:{}", processId, subPath, "temp", tempPath.toString());
                agentAdapter.downloadFile(absPath, tempPath.toString());
                if (tempPath.toFile().exists()) {
                    logger.info("Downloaded file {}:{} to {}:{}", processId, subPath, "temp", tempPath.toString());
                    return tempPath;
                } else {
                    throw new Exception("Failed to download file to " + tempPath.toString());
                }
            } catch (Exception e) {
                logger.error("Failed to download file {}:{} to {}:{}", processId, subPath, "temp", tempPath.toString());
                Files.deleteIfExists(tempPath);
                throw e;
            }
        } else {
            throw new Exception("File " + absPath + " does not exist in process " + processId);
        }
    }
}
