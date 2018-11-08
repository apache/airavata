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
package org.apache.airavata.gfac.impl.task;

import com.jcraft.jsch.Session;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.cluster.CommandInfo;
import org.apache.airavata.gfac.core.cluster.RawCommandInfo;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.cpi.ExpCatChildDataType;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This will be used for both Input file staging and output file staging, hence if you do any changes to a part of logic
 * in this class please consider that will works with both input and output cases.
 */
public class SCPDataStageTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(SCPDataStageTask.class);

    /**
     * Maximum size of the file which is compatible for STRING output parsing
     */
    private static final long MAX_FILE_SIZE_TO_READ = 2 * 1024 * 1024;

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {

    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        TaskStatus status = new TaskStatus(TaskState.EXECUTING);
        DataStagingTaskModel subTaskModel;

        ProcessContext processContext = taskContext.getParentProcessContext();
        ProcessState processState = processContext.getProcessState();
        try {
            subTaskModel = ((DataStagingTaskModel) taskContext.getSubTaskModel());
            if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                OutputDataObjectType processOutput = taskContext.getProcessOutput();

                if (processOutput == null) {
                    log.error("expId: {}, processId:{}, taskId: {}: Process output can not be null",
                            taskContext.getExperimentId(), taskContext.getProcessId(), taskContext.getTaskId());
                    status = new TaskStatus(TaskState.FAILED);
                    status.setReason("Process output can not be null");
                    return status;
                }

                if (processOutput.getValue() == null) {
                    log.error("expId: {}, processId:{}, taskId: {}:- Couldn't stage file {} , file name shouldn't be null",
                            taskContext.getExperimentId(), taskContext.getProcessId(), taskContext.getTaskId(),
                            processOutput.getName());
                    status = new TaskStatus(TaskState.FAILED);
                    if (processOutput.isIsRequired()) {
                        status.setReason("File name is null, but this output's isRequired bit is not set");
                    } else {
                        status.setReason("File name is null");
                    }
                    return status;

                }

                if (processOutput.getType() == DataType.URI || processOutput.getType() == DataType.URI_COLLECTION ||
                        processOutput.getType() == DataType.STDOUT || processOutput.getType() == DataType.STDERR) {
                    return transferFiles(processContext, subTaskModel, taskContext, processState);

                } else if (processOutput.getType() == DataType.FLOAT || processOutput.getType() == DataType.STRING ||
                        processOutput.getType() == DataType.INTEGER ) {

                    if (processOutput.getSearchQuery() == null || "".equals(processOutput.getSearchQuery())) {
                        String msg = "Search query can not be empty in " + processOutput.getType().name() +
                                " type output : " + processOutput.getName() + ". Specify the string to search in file";
                        log.error(msg);
                        status.setState(TaskState.FAILED);
                        status.setReason(msg);
                        return status;
                    }

                    return extractStringFromFile(subTaskModel, taskContext, processOutput.getSearchQuery());

                } else {
                    String msg = "Unknown output data staging type " + processOutput.getType().name();
                    log.error(msg);
                    status.setState(TaskState.FAILED);
                    status.setReason(msg);
                    ErrorModel errorModel = new ErrorModel();
                    errorModel.setActualErrorMessage(msg);
                    errorModel.setUserFriendlyMessage(msg);
                    taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
                    return status;
                }

            } else if (processState == ProcessState.INPUT_DATA_STAGING) {
                InputDataObjectType processInput = taskContext.getProcessInput();
                if (processInput != null && processInput.getValue() == null) {
                    log.error("expId: {}, processId:{}, taskId: {}:- Couldn't stage file {} , file name shouldn't be null",
                            taskContext.getExperimentId(), taskContext.getProcessId(), taskContext.getTaskId(),
                            processInput.getName());
                    status = new TaskStatus(TaskState.FAILED);
                    if (processInput.isIsRequired()) {
                        status.setReason("File name is null, but this input's isRequired bit is not set");
                    } else {
                        status.setReason("File name is null");
                    }
                    return status;
                } else {
                    return transferFiles(processContext, subTaskModel, taskContext, processState);
                }

            } else {
                status.setState(TaskState.FAILED);
                status.setReason("Invalid task invocation, Support " + ProcessState.INPUT_DATA_STAGING.name() + " and " +
                        "" + ProcessState.OUTPUT_DATA_STAGING.name() + " process phases. found " + processState.name());
                return status;
            }

        } catch (TException e) {
            String msg = "Couldn't create subTask model thrift model";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
            return status;
        }
    }

    private TaskStatus extractStringFromFile(DataStagingTaskModel subTaskModel, TaskContext taskContext, String arguments) {

        TaskStatus status = new TaskStatus(TaskState.EXECUTING);

        File tempFile = null;
        try {
            if (arguments != null && !arguments.isEmpty()) {

                StringBuilder result = new StringBuilder();

                URI sourceURI = new URI(subTaskModel.getSource());
                long fileSize = taskContext.getParentProcessContext().getJobSubmissionRemoteCluster().getFileSize(sourceURI.getPath());
                log.info("File size of " + sourceURI.getPath() + " is " + fileSize + " bytes");
                // if the file size is grater than 2 MB, skip parsing to avoid possible OOM issues
                if (fileSize <= MAX_FILE_SIZE_TO_READ) {

                    String[] allArgs = arguments.split(",");
                    tempFile = File.createTempFile("temp-output", ".tmp");

                    log.info("Downloading file " + sourceURI.getPath() + " to temporary file " + tempFile);

                    taskContext.getParentProcessContext().getJobSubmissionRemoteCluster().copyFrom(sourceURI.getPath(), tempFile.getPath());

                    // this is to identify that output is parsed
                    result.append("parsed-out: ");

                    try (FileReader fr = new FileReader(tempFile)) {
                        log.info("Searching for lines that contains " + arguments + " in file " + tempFile);
                        BufferedReader br = new BufferedReader(fr);

                        String line;
                        while ((line = br.readLine()) != null) {

                            if (log.isTraceEnabled()) {
                                log.trace("Input file line : {}", line);
                            }

                            for (String arg : allArgs) {
                                if (line.contains(arg)) {
                                    log.debug("Found a line with argument {} : {}", arg, line);
                                    result.append(line).append("\n");
                                }
                            }
                        }

                        log.info("Values that contains given arguments : " + result.toString());
                        GFacUtils.saveExperimentOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), result.toString());
                        GFacUtils.saveProcessOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), result.toString());

                        status.setState(TaskState.COMPLETED);
                        status.setReason("Successfully parsed output file and fetched data");

                    } catch (IOException e) {
                        String msg = "Failed while reading from the file " + tempFile + " downloaded from " + sourceURI.getPath();
                        log.error(msg, e);
                        status.setState(TaskState.FAILED);
                        status.setReason(msg);
                        ErrorModel errorModel = new ErrorModel();
                        errorModel.setActualErrorMessage(e.getMessage());
                        errorModel.setUserFriendlyMessage(msg);
                        taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));

                    }

                } else {
                    String msg = "Skipping output parsing as the file " + sourceURI.getPath() + " sized exceeded 2MB (" + (fileSize/(1024*1024)) + ")";
                    log.warn(msg);
                    result.append(msg);
                    GFacUtils.saveExperimentOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), result.toString());
                    GFacUtils.saveProcessOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), result.toString());
                    status.setReason(msg);
                    status.setState(TaskState.COMPLETED);
                }
            }
            return status;

        } catch (GFacException e) {
            String msg = "Data staging (extracting values) failed";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
            return status;

        } catch (URISyntaxException e) {
            String msg = "Failed to generate uri form the source of sub task";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
            return status;

        } catch (IOException e) {
            String msg = "Failed to create the temporary file to download output file";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
            return status;

        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (tempFile.delete()) {
                    log.debug("Temp file {} successfully deleted", tempFile.getAbsolutePath());
                } else {
                    log.warn("Failed to delete temp file {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private TaskStatus transferFiles(ProcessContext processContext, DataStagingTaskModel subTaskModel,
                                     TaskContext taskContext, ProcessState processState) {
        TaskStatus status = new TaskStatus(TaskState.EXECUTING);

        try {
            StorageResourceDescription storageResource = processContext.getStorageResource();
//            StoragePreference storagePreference = taskContext.getParentProcessContext().getStoragePreference();

            String hostName;
            if (storageResource != null) {
                hostName = storageResource.getHostName();
            } else {
                throw new GFacException("Storage Resource is null");
            }
            String inputPath = processContext.getStorageFileSystemRootLocation();
            inputPath = (inputPath.endsWith(File.separator) ? inputPath : inputPath + File.separator);

            // use rsync instead of scp if source and destination host and user name is same.
            URI sourceURI = new URI(subTaskModel.getSource());
            String fileName = sourceURI.getPath().substring(sourceURI.getPath().lastIndexOf(File.separator) + 1,
                    sourceURI.getPath().length());

            Session remoteSession = Factory.getSSHSession(Factory.getComputerResourceSSHKeyAuthentication(processContext),
                    processContext.getComputeResourceServerInfo());
            Session storageSession = Factory.getSSHSession(Factory.getStorageSSHKeyAuthentication(processContext),
                    processContext.getStorageResourceServerInfo());

            URI destinationURI;
            if (subTaskModel.getDestination().startsWith("dummy")) {
                destinationURI = TaskUtils.getDestinationURI(taskContext, hostName, inputPath, fileName);
                subTaskModel.setDestination(destinationURI.toString());
            } else {
                destinationURI = new URI(subTaskModel.getDestination());
            }

            if (sourceURI.getHost().equalsIgnoreCase(destinationURI.getHost())
                    && sourceURI.getUserInfo().equalsIgnoreCase(destinationURI.getUserInfo())) {
                localDataCopy(taskContext, sourceURI, destinationURI);
                status.setState(TaskState.COMPLETED);
                status.setReason("Locally copied file using 'cp' command ");
                return status;
            }

            status = new TaskStatus(TaskState.COMPLETED);

            //Wildcard for file name. Has to find the correct name.
            if (fileName.contains("*")) {
                String destParentPath = (new File(destinationURI.getPath())).getParentFile().getPath();
                String sourceParentPath = (new File(sourceURI.getPath())).getParentFile().getPath();

                log.info("Fetching output files for wildcard " + fileName + " in path " + sourceParentPath);
                List<String> fileNames = taskContext.getParentProcessContext().getDataMovementRemoteCluster()
                        .getFileNameFromExtension(fileName, sourceParentPath, remoteSession);

                ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();

                log.info("File names that matched with wildcard " + fileName + " : " + fileNames.toString());

                String experimentId = processContext.getExperimentId();

                String processId = processContext.getProcessId();

                OutputDataObjectType processOutput = taskContext.getProcessOutput();

                for (String temp : fileNames) {
                    if (temp != null && !"".equals(temp)) {
                        fileName = temp;
                    }

                    if (destParentPath.endsWith(File.separator)) {
                        destinationURI = new URI(destParentPath + fileName);
                    } else {
                        destinationURI = new URI(destParentPath + File.separator + fileName);
                    }

                    //Wildcard support is only enabled for output data staging
                    if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                        URI newSourceURI = new URI((sourceParentPath.endsWith(File.separator) ?
                                sourceParentPath : sourceParentPath + File.separator) +
                                fileName);
                        processOutput.setName(fileName);

                        experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_OUTPUT, Collections.singletonList(processOutput), experimentId);
                        experimentCatalog.add(ExpCatChildDataType.PROCESS_OUTPUT, Collections.singletonList(processOutput), processId);

                        taskContext.setProcessOutput(processOutput);

                        makeDir(taskContext, destinationURI);
                        // TODO - save updated subtask model with new destination
                        log.info("Staging derived output file " + fileName + " from " + newSourceURI.toString());
                        outputDataStaging(taskContext, remoteSession, newSourceURI, storageSession, destinationURI);
                        status.setReason("Successfully staged output file " + fileName);
                    }
                }
                if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                    status.setReason("Successfully staged output data");
                } else {
                    status.setReason("Wildcard support is only enabled for output data staging");
                }
            } else {
                if (processState == ProcessState.INPUT_DATA_STAGING) {
                    inputDataStaging(taskContext, storageSession, sourceURI, remoteSession, destinationURI);
                    status.setReason("Successfully staged input data");
                } else if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                    makeDir(taskContext, destinationURI);
                    // TODO - save updated subtask model with new destination
                    outputDataStaging(taskContext, remoteSession, sourceURI, storageSession, destinationURI);
                    status.setReason("Successfully staged output data");
                }
            }
        } catch (URISyntaxException e) {
            String msg = "Source or destination uri is not correct source : " + subTaskModel.getSource() + ", " +
                    "destination : " + subTaskModel.getDestination();
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
        } catch (CredentialStoreException e) {
            String msg = "Storage authentication issue, could be invalid credential token";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
        } catch (RegistryException | GFacException e) {
            String msg = "Data staging failed";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Collections.singletonList(errorModel));
        }
        return status;
    }

    private void localDataCopy(TaskContext taskContext, URI sourceURI, URI destinationURI) throws GFacException {
        CommandInfo commandInfo = new RawCommandInfo("rsync -cr " + sourceURI.getPath() + " " + destinationURI.getPath());
        taskContext.getParentProcessContext().getDataMovementRemoteCluster().execute(commandInfo);
    }

    private void inputDataStaging(TaskContext taskContext, Session srcSession, URI sourceURI,  Session destSession, URI
            destinationURI) throws GFacException {


        // scp third party file transfer 'to' compute resource.
        taskContext.getParentProcessContext().getDataMovementRemoteCluster().scpThirdParty(sourceURI.getPath(), srcSession,
                destinationURI.getPath(), destSession, RemoteCluster.DIRECTION.FROM, false);
    }

    private void outputDataStaging(TaskContext taskContext, Session srcSession, URI sourceURI,  Session destSession, URI destinationURI)
            throws GFacException {

        //Wildcard file path has not been resolved and cannot be handled. Hence ignoring
        if(!destinationURI.toString().contains("*")) {
            log.info("Starting file transfer from " + sourceURI.toString() + " to " + destinationURI.toString());
            taskContext.getParentProcessContext().getDataMovementRemoteCluster().scpThirdParty(sourceURI.getPath(), srcSession,
                    destinationURI.getPath(), destSession, RemoteCluster.DIRECTION.TO, true);
            // update output locations
            GFacUtils.saveExperimentOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), destinationURI.toString());
            GFacUtils.saveProcessOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), destinationURI.toString());
            log.info("Finished file transfer to " + destinationURI.toString());
        } else {
            log.warn("Destination file path contains unresolved wildcards. Path: " + destinationURI.toString());
        }
    }

    private void makeDir(TaskContext taskContext, URI pathURI) throws GFacException {
        int endIndex = pathURI.getPath().lastIndexOf('/');
        if (endIndex < 1) {
            return;
        }
        String targetPath = pathURI.getPath().substring(0, endIndex);
        taskContext.getParentProcessContext().getDataMovementRemoteCluster().makeDirectory(targetPath);
    }

    @Override
    public TaskStatus recover(TaskContext taskContext) {
        TaskState state = taskContext.getTaskStatus().getState();
        if (state == TaskState.EXECUTING || state == TaskState.CREATED) {
            return execute(taskContext);
        } else {
            // files already transferred or failed
            return taskContext.getTaskStatus();
        }
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.DATA_STAGING;
    }

}
