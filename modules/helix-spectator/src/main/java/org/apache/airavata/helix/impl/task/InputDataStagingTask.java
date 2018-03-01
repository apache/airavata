package org.apache.airavata.helix.impl.task;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.commons.io.FileUtils;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@TaskDef(name = "Input Data Staging Task")
public class InputDataStagingTask extends DataStagingTask {

    private static final Logger logger = LogManager.getLogger(InputDataStagingTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper) {
        logger.info("Starting Input Data Staging Task " + getTaskId());

        try {
            // Get and validate data staging task model
            DataStagingTaskModel dataStagingTaskModel = getDataStagingTaskModel();

            // Fetch and validate input data type from data staging task model
            InputDataObjectType processInput = dataStagingTaskModel.getProcessInput();
            if (processInput != null && processInput.getValue() == null) {
                String message = "expId: " + getExperimentId() + ", processId: " + getProcessId() + ", taskId: " + getTaskId() +
                        ":- Couldn't stage file " + processInput.getName() + " , file name shouldn't be null. ";
                logger.error(message);
                if (processInput.isIsRequired()) {
                    message += "File name is null, but this input's isRequired bit is not set";
                } else {
                    message += "File name is null";
                }
                logger.error(message);
                throw new TaskOnFailException(message, true, null);
            }

            // Fetch and validate storage resource
            StorageResourceDescription storageResource = getStorageResource();

            // Fetch and validate source and destination URLS
            URI sourceURI;
            URI destinationURI;
            String sourceFileName;
            try {
                sourceURI = new URI(dataStagingTaskModel.getSource());
                destinationURI = new URI(dataStagingTaskModel.getDestination());

                if (logger.isDebugEnabled()) {
                    logger.debug("Source file " + sourceURI.getPath() + ", destination uri " + destinationURI.getPath() + " for task " + getTaskId());
                }

                sourceFileName = sourceURI.getPath().substring(sourceURI.getPath().lastIndexOf(File.separator) + 1,
                        sourceURI.getPath().length());
            } catch (URISyntaxException e) {
                throw new TaskOnFailException("Failed to obtain source URI for input data staging task " + getTaskId(), true, e);
            }

            // Fetch and validate storage adaptor
            StorageResourceAdaptor storageResourceAdaptor = getStorageAdaptor(taskHelper.getAdaptorSupport());

            // Fetch and validate compute resource adaptor
            AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

            String localSourceFilePath = getLocalDataPath(sourceFileName);
            // Downloading input file from the storage resource
            try {
                logger.info("Downloading input file " + sourceURI.getPath() + " to the local path " + localSourceFilePath);
                storageResourceAdaptor.downloadFile(sourceURI.getPath(), localSourceFilePath);
                logger.info("Input file downloaded to " + localSourceFilePath);
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed downloading input file " + sourceFileName + " to the local path " + localSourceFilePath, true, e);
            }

            // Uploading input file to the compute resource
            try {
                logger.info("Uploading the input file to " + destinationURI.getPath() + " from local path " + localSourceFilePath);
                adaptor.copyFileTo(localSourceFilePath, destinationURI.getPath());
                logger.info("Output file uploaded to " + destinationURI.getPath());
            } catch (AgentException e) {
                throw new TaskOnFailException("Failed uploading the input file to " + destinationURI.getPath() + " from local path " + localSourceFilePath, true, e);
            }

            return onSuccess("Input data staging task " + getTaskId() + " successfully completed");

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return onFail(e.getReason(), e.isCritical(), e.getError());

        }catch (Exception e) {
            logger.error("Unknown error while executing input data staging task " + getTaskId(), e);
            return onFail("Unknown error while executing input data staging task " + getTaskId(), false,  e);
        }
    }

    @Override
    public void onCancel() {

    }
}
