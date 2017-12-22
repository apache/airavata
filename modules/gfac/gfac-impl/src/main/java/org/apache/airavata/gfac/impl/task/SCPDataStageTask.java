/**
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

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.cluster.CommandInfo;
import org.apache.airavata.gfac.core.cluster.RawCommandInfo;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This will be used for both Input file staging and output file staging, hence if you do any changes to a part of logic
 * in this class please consider that will works with both input and output cases.
 */
public class SCPDataStageTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(SCPDataStageTask.class);
    private static final int DEFAULT_SSH_PORT = 22;

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {

    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        TaskStatus status = new TaskStatus(TaskState.EXECUTING);
        AuthenticationInfo authenticationInfo = null;
        DataStagingTaskModel subTaskModel = null;
        String localDataDir = null;

        ProcessContext processContext = taskContext.getParentProcessContext();
        ProcessState processState = processContext.getProcessState();
        try {
            subTaskModel = ((DataStagingTaskModel) taskContext.getSubTaskModel());
            if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                OutputDataObjectType processOutput = taskContext.getProcessOutput();
                if (processOutput != null && processOutput.getValue() == null) {
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
                }
            } else {
                status.setState(TaskState.FAILED);
                status.setReason("Invalid task invocation, Support " + ProcessState.INPUT_DATA_STAGING.name() + " and " +
                        "" + ProcessState.OUTPUT_DATA_STAGING.name() + " process phases. found " + processState.name());
                return status;
            }

            StorageResourceDescription storageResource = processContext.getStorageResource();
//            StoragePreference storagePreference = taskContext.getParentProcessContext().getStoragePreference();

            String hostName = null;
            if (storageResource != null) {
                hostName = storageResource.getHostName();
            } else {
                throw new GFacException("Storage Resource is null");
            }
            String inputPath  = processContext.getStorageFileSystemRootLocation();
            inputPath = (inputPath.endsWith(File.separator) ? inputPath : inputPath + File.separator);

            // use rsync instead of scp if source and destination host and user name is same.
            URI sourceURI = new URI(subTaskModel.getSource());
            String fileName = sourceURI.getPath().substring(sourceURI.getPath().lastIndexOf(File.separator) + 1,
                    sourceURI.getPath().length());

            Session remoteSession = Factory.getSSHSession(Factory.getComputerResourceSSHKeyAuthentication(processContext),
                    processContext.getComputeResourceServerInfo());
            Session storageSession = Factory.getSSHSession(Factory.getStorageSSHKeyAuthentication(processContext),
                    processContext.getStorageResourceServerInfo());

            URI destinationURI = null;
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
            if(fileName.contains("*")){
                String destParentPath = (new File(destinationURI.getPath())).getParentFile().getPath();
                String sourceParentPath = (new File(sourceURI.getPath())).getParentFile().getPath();
                List<String> fileNames = taskContext.getParentProcessContext().getDataMovementRemoteCluster()
                        .getFileNameFromExtension(fileName, sourceParentPath, remoteSession);

                ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();

                String experimentId = processContext.getExperimentId();

                String processId = processContext.getProcessId();

                OutputDataObjectType processOutput = taskContext.getProcessOutput();

                for(int i=0; i<fileNames.size(); i++){
                    String temp = fileNames.get(i);
                    if(temp != null && temp != ""){
                        fileName = temp;
                    }
                    if(destParentPath.endsWith(File.separator)){
                        destinationURI = new URI(destParentPath + fileName);
                    }else{
                        destinationURI = new URI(destParentPath + File.separator + fileName);
                    }

                    //Wildcard support is only enabled for output data staging
                    if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                        processOutput.setName(fileName);

                        experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_OUTPUT, Arrays.asList(processOutput), experimentId);
                        experimentCatalog.add(ExpCatChildDataType.PROCESS_OUTPUT, Arrays.asList(processOutput), processId);

                        taskContext.setProcessOutput(processOutput);

                        makeDir(taskContext, destinationURI);
                        // TODO - save updated subtask model with new destination
                        outputDataStaging(taskContext, remoteSession, sourceURI, storageSession, destinationURI);
                        status.setReason("Successfully staged output data");
                    }
                }
                if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                    status.setReason("Successfully staged output data");
                }else{
                    status.setReason("Wildcard support is only enabled for output data staging");
                }
            }else {
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
        } catch (TException e) {
            String msg = "Couldn't create subTask model thrift model";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
            return status;
        } catch (ApplicationSettingsException | FileNotFoundException e) {
            String msg = "Failed while reading credentials";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (URISyntaxException e) {
            String msg = "Source or destination uri is not correct source : " + subTaskModel.getSource() + ", " +
                    "destination : " + subTaskModel.getDestination();
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (CredentialStoreException e) {
            String msg = "Storage authentication issue, could be invalid credential token";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (AiravataException e) {
            String msg = "Error while creating ssh session with client";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (JSchException | IOException e) {
            String msg = "Failed to do scp with client";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch (RegistryException | GFacException e) {
            String msg = "Data staging failed";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        }
        return status;
    }

    private void localDataCopy(TaskContext taskContext, URI sourceURI, URI destinationURI) throws GFacException {
        File destinationFile = new File(destinationURI.getPath());
        File destinationParentDir = destinationFile.getParentFile();
        String destinationParentPath = destinationParentDir.getPath();
        // create the parent directory if it does not exist.
        String mkdirCommand = "mkdir -p " + destinationParentPath;
        String rsyncCommand = "rsync -cr " + sourceURI.getPath() + " " + destinationURI.getPath();
        CommandInfo mkdirCommandInfo = new RawCommandInfo(mkdirCommand);
        CommandInfo rsyncCommandInfo = new RawCommandInfo(rsyncCommand);
        taskContext.getParentProcessContext().getDataMovementRemoteCluster().execute(mkdirCommandInfo);
        taskContext.getParentProcessContext().getDataMovementRemoteCluster().execute(rsyncCommandInfo);
    }

    private void inputDataStaging(TaskContext taskContext, Session srcSession, URI sourceURI,  Session destSession, URI
            destinationURI) throws GFacException, IOException, JSchException {
        /**
         * scp third party file transfer 'to' compute resource.
         */
        taskContext.getParentProcessContext().getDataMovementRemoteCluster().scpThirdParty(sourceURI.getPath(), srcSession,
                destinationURI.getPath(), destSession, RemoteCluster.DIRECTION.FROM, false);
    }

    private void outputDataStaging(TaskContext taskContext, Session srcSession, URI sourceURI,  Session destSession, URI destinationURI)
            throws AiravataException, IOException, JSchException, GFacException {

        /**
         * scp third party file transfer 'from' comute resource.
         */
        //Wildcard file path has not been resolved and cannot be handled. Hence ignoring
        if(!destinationURI.toString().contains("*")){
            taskContext.getParentProcessContext().getDataMovementRemoteCluster().scpThirdParty(sourceURI.getPath(), srcSession,
                    destinationURI.getPath(), destSession, RemoteCluster.DIRECTION.TO, true);
            // update output locations
            GFacUtils.saveExperimentOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), destinationURI.toString());
            GFacUtils.saveProcessOutput(taskContext.getParentProcessContext(), taskContext.getProcessOutput().getName(), destinationURI.toString());
        }else{
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
