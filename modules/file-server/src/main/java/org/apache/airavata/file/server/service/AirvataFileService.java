package org.apache.airavata.file.server.service;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.FileMetadata;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.file.server.model.AiravataDirectory;
import org.apache.airavata.file.server.model.AiravataFile;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class AirvataFileService {

    private final static Logger logger = LoggerFactory.getLogger(AirvataFileService.class);

    @Autowired
    private AdaptorSupport adaptorSupport;

    @Autowired
    ThriftClientPool<RegistryService.Client> registryClientPool;

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
    public AiravataDirectory listFiles(String processId, String subPath) throws Exception {
        ProcessDataManager dataManager = new ProcessDataManager(registryClientPool, processId, adaptorSupport);

        AgentAdaptor agentAdaptor = getAgentAdaptor(dataManager, processId);

        AiravataDirectory airavataDirectory = new AiravataDirectory("root", System.currentTimeMillis()); // TODO: set dir name

        subPath = dataManager.getBaseDir() + (subPath.isEmpty()? "" : "/" + subPath);
        logger.info("Listing files in path {}", subPath);
        List<String> fileList = agentAdaptor.listDirectory(subPath); // TODO: Validate if this is a file or dir
        for (String fileOrDir : fileList) {
            logger.info("Processing file {}", fileOrDir);
            FileMetadata fileMetadata = agentAdaptor.getFileMetadata(subPath + "/" + fileOrDir);
            airavataDirectory.getInnerFiles().add(new AiravataFile(fileMetadata.getName(),
                    fileMetadata.getSize(), System.currentTimeMillis(), System.currentTimeMillis())); // TODO: update created and updated time
        }

        return airavataDirectory;
    }
    public Path downloadFile(String processId, String subPath) throws Exception {

        ProcessDataManager dataManager = new ProcessDataManager(registryClientPool, processId, adaptorSupport);

        AgentAdaptor agentAdaptor = getAgentAdaptor(dataManager, processId);
        subPath = dataManager.getBaseDir() + (subPath.isEmpty()? "" : "/" + subPath);

        if (agentAdaptor.doesFileExist(subPath)) {
            Path tempFile = Files.createTempFile("tempfile_", ".data");
            tempFile.toFile().deleteOnExit();
            try {
                agentAdaptor.downloadFile(subPath, tempFile.toFile().getAbsolutePath());
                return tempFile;
            } catch (Exception e) {
                logger.error("Failed to download file {} from process {} to local path {}",
                        subPath, processId, tempFile.toFile().getAbsolutePath());
                try {
                    tempFile.toFile().delete();
                } catch (Exception ignore) {
                    // Ignore
                }
                throw e;
            }
        } else {
            throw new Exception("File " + subPath + "  does not exist in process " + processId);
        }
    }
}
