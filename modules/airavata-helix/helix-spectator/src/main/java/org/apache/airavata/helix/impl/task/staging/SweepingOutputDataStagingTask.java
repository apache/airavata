package org.apache.airavata.helix.impl.task.staging;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

@TaskDef(name = "Sweeping Output Data Staging Task")
public class SweepingOutputDataStagingTask extends DataStagingTask {

    private final static Logger logger = LoggerFactory.getLogger(SweepingOutputDataStagingTask.class);

    @TaskParam(name = "Job Index")
    private int jobIndex;

    @Override
    public TaskResult onRun(TaskHelper taskHelper, TaskContext taskContext) {
        logger.info("Starting sweeping output data staging task " + getTaskId() + " in experiment " + getExperimentId());
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

            // Fetch and validate storage adaptor
            StorageResourceAdaptor storageResourceAdaptor = getStorageAdaptor(taskHelper.getAdaptorSupport());

            // Fetch and validate compute resource adaptor
            AgentAdaptor adaptor = getComputeResourceAdaptor(taskHelper.getAdaptorSupport());

            //List<String> productUris = new ArrayList<>();

            String sourcePath = Paths.get(taskContext.getWorkingDir(), jobIndex + "", dataStagingTaskModel.getProcessOutput().getValue()).toString();
            String fileName = new File(dataStagingTaskModel.getProcessOutput().getValue()).getName();
            String destPath = Paths.get(getProcessModel().getExperimentDataDir(), jobIndex + "_" + fileName).toString();

            boolean transferred = transferFileToStorage(sourcePath, destPath, fileName, adaptor, storageResourceAdaptor);
            URI destinationURI = new URI("file", getTaskContext().getStorageResourceLoginUserName(),
                    storageResource.getHostName(), 22, destPath, null, null);
            if (transferred) {
                saveExperimentOutput(processOutput.getName(), destinationURI.toString());
            } else {
                logger.warn("File " + sourcePath + " did not transfer");
            }


            /*if (processOutput.getType() == DataType.STDERR || processOutput.getType() == DataType.STDOUT) {
                saveExperimentOutput(processOutput.getName(), productUris.get(0));
            } else {
                saveExperimentOutputCollection(processOutput.getName(), productUris);
            }*/
            //saveExperimentOutputCollection(processOutput.getName(), productUris);

            return onSuccess("Sweeping output data staging task " + getTaskId() + " successfully completed");

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

    @Override
    public void onCancel(TaskContext taskContext) {

    }

    public int getJobIndex() {
        return jobIndex;
    }

    public void setJobIndex(int jobIndex) {
        this.jobIndex = jobIndex;
    }
}
