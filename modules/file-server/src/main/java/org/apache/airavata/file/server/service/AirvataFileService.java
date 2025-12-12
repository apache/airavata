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
import java.util.List;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.FileMetadata;
import org.apache.airavata.file.server.model.AiravataDirectory;
import org.apache.airavata.file.server.model.AiravataFile;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.profile.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AirvataFileService {

    private static final Logger logger = LoggerFactory.getLogger(AirvataFileService.class);

    private final AdaptorSupport adaptorSupport;
    private final RegistryService registryService;
    private final ApplicationContext applicationContext;
    private final UserProfileService userProfileService;
    private final CredentialStoreService credentialStoreService;

    public AirvataFileService(
            AdaptorSupport adaptorSupport,
            RegistryService registryService,
            ApplicationContext applicationContext,
            UserProfileService userProfileService,
            CredentialStoreService credentialStoreService) {
        this.adaptorSupport = adaptorSupport;
        this.registryService = registryService;
        this.applicationContext = applicationContext;
        this.userProfileService = userProfileService;
        this.credentialStoreService = credentialStoreService;
    }

    private AgentAdaptor getAgentAdaptor(ProcessDataManager dataManager, String processId) throws Exception {
        AgentAdaptor agentAdaptor;
        try {
            agentAdaptor = dataManager.getAgentAdaptor();
        } catch (Exception e) {
            logger.error("Failed to fetch adaptor for process {}", processId, e);
            throw new Exception("Failed to fetch adaptor for process " + processId, e);
        }
        return agentAdaptor;
    }

    public FileMetadata getInfo(String processId, String subPath) throws Exception {
        var dataManager = new ProcessDataManager(applicationContext, registryService, userProfileService, credentialStoreService, processId, adaptorSupport);
        var agentAdaptor = getAgentAdaptor(dataManager, processId);
        String absPath = dataManager.getBaseDir() + subPath;

        logger.info("Getting metadata for path {}", absPath);
        return agentAdaptor.getFileMetadata(absPath);
    }

    public AiravataDirectory listDir(String processId, String subPath) throws Exception {
        var dataManager = new ProcessDataManager(applicationContext, registryService, userProfileService, credentialStoreService, processId, adaptorSupport);
        var agentAdaptor = getAgentAdaptor(dataManager, processId);

        String absPath = dataManager.getBaseDir() + subPath;
        logger.info("Getting metadata for path {}", absPath);
        FileMetadata rm = agentAdaptor.getFileMetadata(absPath);
        if (!rm.isDirectory()) {
            throw new Exception("Path " + absPath + " is not a directory");
        }

        AiravataDirectory airavataDirectory = AiravataDirectory.fromMetadata(rm);
        logger.info("Listing files in path {}", absPath);
        List<String> fileList = agentAdaptor.listDirectory(absPath);
        for (String fileOrDir : fileList) {
            logger.info("Getting metadata for path {}", fileOrDir);
            FileMetadata m = agentAdaptor.getFileMetadata(absPath + "/" + fileOrDir);
            if (m.isDirectory()) {
                airavataDirectory.getInnerDirectories().add(AiravataDirectory.fromMetadata(m));
            } else {
                airavataDirectory.getInnerFiles().add(AiravataFile.fromMetadata(m));
            }
        }

        return airavataDirectory;
    }

    public AiravataFile listFile(String processId, String subPath) throws Exception {
        var dataManager = new ProcessDataManager(applicationContext, registryService, userProfileService, credentialStoreService, processId, adaptorSupport);
        var agentAdaptor = getAgentAdaptor(dataManager, processId);

        String absPath = dataManager.getBaseDir() + subPath;

        var info = agentAdaptor.getFileMetadata(absPath);
        return AiravataFile.fromMetadata(info);
    }

    public void uploadFile(String processId, String subPath, MultipartFile file) throws Exception {

        var tempPath = Files.createTempFile("tempfile_", ".data").toAbsolutePath();
        var metadata = new FileMetadata();
        metadata.setName(file.getName());
        metadata.setSize(file.getSize());
        Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

        var dataManager = new ProcessDataManager(applicationContext, registryService, userProfileService, credentialStoreService, processId, adaptorSupport);
        var agentAdaptor = getAgentAdaptor(dataManager, processId);
        String absPath = dataManager.getBaseDir() + subPath;

        try {
            var content = Files.newInputStream(tempPath);
            agentAdaptor.createDirectory(new File(absPath).getParent(), true);
            logger.info("Uploading file {}:{} to {}:{}", "temp", metadata.getName(), processId, subPath);
            agentAdaptor.uploadFile(content, metadata, absPath);
            logger.info("Uploaded file {}:{} to {}:{}", "temp", metadata.getName(), processId, subPath);
        } catch (Exception e) {
            logger.error("Failed to upload file {}:{} to {}:{}", "temp", metadata.getName(), processId, subPath);
            throw e;
        } finally {
            tempPath.toFile().deleteOnExit();
        }
    }

    public Path downloadFile(String processId, String subPath) throws Exception {

        var dataManager = new ProcessDataManager(applicationContext, registryService, userProfileService, credentialStoreService, processId, adaptorSupport);
        var agentAdaptor = getAgentAdaptor(dataManager, processId);
        String absPath = dataManager.getBaseDir() + subPath;

        if (agentAdaptor.doesFileExist(absPath)) {
            var tempPath = Files.createTempFile("tempfile_", ".data").toAbsolutePath();
            try {
                logger.info("Downloading file {}:{} to {}:{}", processId, subPath, "temp", tempPath.toString());
                agentAdaptor.downloadFile(absPath, tempPath.toString());
                if (tempPath.toFile().exists()) {
                    logger.info("Downloaded file {}:{} to {}:{}", processId, subPath, "temp", tempPath.toString());
                    return tempPath;
                } else {
                    throw new Exception("Failed to download file to " + tempPath.toString());
                }
            } catch (Exception e) {
                logger.error("Failed to download file {}:{} to {}:{}", processId, subPath, "temp", tempPath.toString());
                throw e;
            } finally {
                tempPath.toFile().deleteOnExit();
            }
        } else {
            throw new Exception("File " + absPath + " does not exist in process " + processId);
        }
    }
}
