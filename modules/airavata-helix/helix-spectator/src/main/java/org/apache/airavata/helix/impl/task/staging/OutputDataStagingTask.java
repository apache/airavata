package org.apache.airavata.helix.impl.task.staging;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

@TaskDef(name = "Output Data Staging Task")
public class OutputDataStagingTask extends DataStagingTask {

    private static final Logger logger = LogManager.getLogger(OutputDataStagingTask.class);

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {

        logger.info("Starting output data staging task " + getTaskId() + " in experiment " + getExperimentId());
        saveAndPublishProcessStatus(ProcessState.OUTPUT_DATA_STAGING);

        try {
            // Get and validate data staging task model
            DataStagingTaskModel dataStagingTaskModel = getDataStagingTaskModel();

            // Fetch and validate input data type from data staging task model
            OutputDataObjectType processOutput = dataStagingTaskModel.getProcessOutput();
            if (processOutput != null && processOutput.getValue() == null) {
                String message = "expId: " + getExperimentId() + ", processId: " + getProcessId() + ", taskId: " + getTaskId() +
                        ":- Couldn't stage file " + processOutput.getName() + " , file name shouldn't be null. ";
                logger.error(message);
                if (processOutput.isIsRequired()) {
                    message += "File name is null, but this output's isRequired bit is not set";
                } else {
                    message += "File name is null";
                }
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
                sourceFileName = sourceURI.getPath().substring(sourceURI.getPath().lastIndexOf(File.separator) + 1,
                        sourceURI.getPath().length());

                if (dataStagingTaskModel.getDestination().startsWith("dummy")) {

                    String inputPath  = getTaskContext().getStorageFileSystemRootLocation();
                    inputPath = (inputPath.endsWith(File.separator) ? inputPath : inputPath + File.separator);
                    String experimentDataDir = getProcessModel().getExperimentDataDir();
                    String filePath;
                    if(experimentDataDir != null && !experimentDataDir.isEmpty()) {
                        if(!experimentDataDir.endsWith(File.separator)){
                            experimentDataDir += File.separator;
                        }
                        if (experimentDataDir.startsWith(File.separator)) {
                            filePath = experimentDataDir + sourceFileName;
                        } else {
                            filePath = inputPath + experimentDataDir + sourceFileName;
                        }
                    } else {
                        filePath = inputPath + getProcessId() + File.separator + sourceFileName;
                    }

                    destinationURI = new URI("file", getTaskContext().getStorageResourceLoginUserName(),
                            storageResource.getHostName(), 22, filePath, null, null);

                } else {
                    destinationURI = new URI(dataStagingTaskModel.getDestination());
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Source file " + sourceURI.getPath() + ", destination uri " + destinationURI.getPath() + " for task " + getTaskId());
                }
            } catch (URISyntaxException e) {
                throw new TaskOnFailException("Failed to obtain source URI for output data staging task " + getTaskId(), true, e);
            }

            // Fetch and validate storage adaptor
            StorageResourceAdaptor storageResourceAdaptor = getStorageAdaptor(taskHelper.getAdaptorSupport());

            // Fetch and validate compute resource adaptor
            AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

            if (sourceFileName.contains("*")) {
                // if file is declared as a wild card
                logger.info("Handling output files with " + sourceFileName + " extension for task " + getTaskId());

                String destParentPath = (new File(destinationURI.getPath())).getParentFile().getPath();
                String sourceParentPath = (new File(sourceURI.getPath())).getParentFile().getPath();

                logger.debug("Destination parent path " + destParentPath + ", source parent path " + sourceParentPath);
                List<String> fileNames;
                try {
                    fileNames = adaptor.getFileNameFromExtension(sourceFileName, sourceParentPath);

                    if (logger.isTraceEnabled()) {
                        fileNames.forEach(fileName -> logger.trace("File found : " + fileName));
                    }

                } catch (AgentException e) {
                    throw new TaskOnFailException("Failed to fetch the file list from extension " + sourceFileName, true, e);
                }

                for (String temp : fileNames) {
                    if (temp != null && !temp.equals("")) {
                        sourceFileName = temp;
                    }
                    if (destParentPath.endsWith(File.separator)) {
                        destinationURI = new URI(destParentPath + sourceFileName);
                    } else {
                        destinationURI = new URI(destParentPath + File.separator + sourceFileName);
                    }

                    //Wildcard support is only enabled for output data staging
                    assert processOutput != null;
                    processOutput.setName(sourceFileName);

                    try {
                        getTaskContext().getExperimentCatalog().add(ExpCatChildDataType.EXPERIMENT_OUTPUT,
                                Collections.singletonList(processOutput), getExperimentId());
                        getTaskContext().getExperimentCatalog().add(ExpCatChildDataType.PROCESS_OUTPUT,
                                Collections.singletonList(processOutput), getProcessId());
                    } catch (RegistryException e) {
                        throw new TaskOnFailException("Failed to update experiment or process outputs for task " + getTaskId(), true, e);
                    }

                    logger.info("Transferring file " + sourceFileName);
                    transferFile(sourceURI, destinationURI, sourceFileName, adaptor, storageResourceAdaptor);
                }
                return onSuccess("Output data staging task " + getTaskId() + " successfully completed");

            } else {
                // Downloading input file from the storage resource
                transferFile(sourceURI, destinationURI, sourceFileName, adaptor, storageResourceAdaptor);
                return onSuccess("Output data staging task " + getTaskId() + " successfully completed");
            }

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }
            return onFail(e.getReason(), e.isCritical(), e.getError());

        } catch (Exception e) {
            logger.error("Unknown error while executing output data staging task " + getTaskId(), e);
            return onFail("Unknown error while executing output data staging task " + getTaskId(), false,  e);
        }
    }

    private void transferFile(URI sourceURI, URI destinationURI, String fileName, AgentAdaptor adaptor,
                              StorageResourceAdaptor storageResourceAdaptor) throws TaskOnFailException {
        String localSourceFilePath = getLocalDataPath(fileName);

        try {
            logger.info("Downloading output file " + sourceURI.getPath() + " to the local path " + localSourceFilePath);
            adaptor.copyFileFrom(sourceURI.getPath(), localSourceFilePath);
            logger.info("Output file downloaded to " + localSourceFilePath);
        } catch (AgentException e) {
            throw new TaskOnFailException("Failed downloading output file " + sourceURI.getPath() + " to the local path " +
                    localSourceFilePath, true, e);
        }

        // Uploading input file to the compute resource
        try {
            logger.info("Uploading the output file to " + destinationURI.getPath() + " from local path " + localSourceFilePath);
            storageResourceAdaptor.uploadFile(localSourceFilePath, destinationURI.getPath());
            logger.info("Output file uploaded to " + destinationURI.getPath());
        } catch (AgentException e) {
            throw new TaskOnFailException("Failed uploading the output file to " + destinationURI.getPath() + " from local path " +
                    localSourceFilePath, true, e);
        }
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
