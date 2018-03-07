package org.apache.airavata.helix.impl.task.staging;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public abstract class DataStagingTask extends AiravataTask {

    @SuppressWarnings("WeakerAccess")
    protected DataStagingTaskModel getDataStagingTaskModel() throws TaskOnFailException {
        try {
            Object subTaskModel = getTaskContext().getSubTaskModel();
            if (subTaskModel != null) {
                return DataStagingTaskModel.class.cast(subTaskModel);
            } else {
                throw new TaskOnFailException("Data staging task model can not be null for task " + getTaskId(), true, null);
            }
        } catch (Exception e) {
            throw new TaskOnFailException("Failed while obtaining data staging task model for task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected StorageResourceDescription getStorageResource() throws TaskOnFailException {
        try {
            StorageResourceDescription storageResource = getTaskContext().getStorageResource();
            if (storageResource == null) {
                throw new TaskOnFailException("Storage resource can not be null for task " + getTaskId(), true, null);
            }
            return storageResource;
        } catch (AppCatalogException e) {
            throw new TaskOnFailException("Failed to fetch the storage resource for task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected StorageResourceAdaptor getStorageAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        try {
            StorageResourceAdaptor storageResourceAdaptor = adaptorSupport.fetchStorageAdaptor(
                    getGatewayId(),
                    getTaskContext().getStorageResourceId(),
                    "SSH",
                    getTaskContext().getStorageResourceCredentialToken(),
                    getTaskContext().getStorageResourceLoginUserName());

            if (storageResourceAdaptor == null) {
                throw new TaskOnFailException("Storage resource adaptor for " + getTaskContext().getStorageResourceId() + " can not be null", true, null);
            }
            return storageResourceAdaptor;
        } catch (AgentException e) {
            throw new TaskOnFailException("Failed to obtain adaptor for storage resource " + getTaskContext().getStorageResourceId() +
                    " in task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected AgentAdaptor getComputeResourceAdaptor(AdaptorSupport adaptorSupport) throws TaskOnFailException {
        try {
            return adaptorSupport.fetchAdaptor(
                    getTaskContext().getGatewayId(),
                    getTaskContext().getComputeResourceId(),
                    getTaskContext().getJobSubmissionProtocol().name(),
                    getTaskContext().getComputeResourceCredentialToken(),
                    getTaskContext().getComputeResourceLoginUserName());
        } catch (Exception e) {
            throw new TaskOnFailException("Failed to obtain adaptor for compute resource " + getTaskContext().getComputeResourceId() +
                    " in task " + getTaskId(), true, e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected String getLocalDataPath(String fileName) throws TaskOnFailException {
        String localDataPath = ServerSettings.getLocalDataLocation();
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator);
        localDataPath = (localDataPath.endsWith(File.separator) ? localDataPath : localDataPath + File.separator) +
                getProcessId() + File.separator + "temp_inputs" + File.separator;
        try {
            FileUtils.forceMkdir(new File(localDataPath));
        } catch (IOException e) {
            throw new TaskOnFailException("Failed build directories " + localDataPath, true, e);
        }
        localDataPath = localDataPath + fileName;
        return localDataPath;
    }
}
