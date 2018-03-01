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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.cluster.CommandInfo;
import org.apache.airavata.gfac.core.cluster.CommandOutput;
import org.apache.airavata.gfac.core.cluster.RawCommandInfo;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.StandardOutReader;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

public class ArchiveTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(ArchiveTask.class);
    private static final int DEFAULT_SSH_PORT = 22;
    private String hostName;
    private String userName;
    private String inputPath;

    @Override
    public void init(Map<String, String> propertyMap) throws TaskException {

    }

    @Override
    public TaskStatus execute(TaskContext taskContext) {
        // implement archive logic with jscp
        TaskStatus status = new TaskStatus(TaskState.EXECUTING);
        ProcessContext processContext = taskContext.getParentProcessContext();
        RemoteCluster remoteCluster = processContext.getJobSubmissionRemoteCluster();
        AuthenticationInfo authenticationInfo = null;

        DataStagingTaskModel subTaskModel = null;
        try {
            subTaskModel = (DataStagingTaskModel) ThriftUtils.getSubTaskModel
                    (taskContext.getTaskModel());
        } catch (TException e) {
            String msg = "Error! Deserialization issue with SubTask Model";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        }

        RegistryService.Client registryClient = Factory.getRegistryServiceClient();
        try {
            StorageResourceDescription storageResource = taskContext.getParentProcessContext().getStorageResource();

            if (storageResource != null) {
                hostName = storageResource.getHostName();
            } else {
                throw new GFacException("Storage Resource is null");
            }
            userName = processContext.getStorageResourceLoginUserName();
            inputPath = processContext.getStorageFileSystemRootLocation();
            inputPath = (inputPath.endsWith(File.separator) ? inputPath : inputPath + File.separator);

            status = new TaskStatus(TaskState.COMPLETED);

            Session srcSession = Factory.getSSHSession(Factory.getComputerResourceSSHKeyAuthentication(processContext),
                    processContext.getComputeResourceServerInfo(registryClient));
            Session destSession =  Factory.getSSHSession(Factory.getStorageSSHKeyAuthentication(processContext),
                    processContext.getStorageResourceServerInfo());

            URI sourceURI = new URI(subTaskModel.getSource());
            URI destinationURI = null;
            String workingDirName = null, path = null;
            if (sourceURI.getPath().endsWith("/")) {
                path = sourceURI.getPath().substring(0, sourceURI.getPath().length() - 1);
            } else {
                path = sourceURI.getPath();
            }
            workingDirName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
            // tar working dir
            // cd /Users/syodage/Desktop/temp/.. && tar -cvf path/workingDir.tar temp
            String archiveTar =  "archive.tar";
            String resourceAbsTarFilePath  = path + "/" + archiveTar;
            CommandInfo commandInfo = new RawCommandInfo("cd " + path + " && tar -cvf "
                    + resourceAbsTarFilePath + " ./* ");

            // move tar to storage resource
            remoteCluster.execute(commandInfo);
            destinationURI = TaskUtils.getDestinationURI(taskContext, hostName, inputPath, archiveTar);
            remoteCluster.scpThirdParty(resourceAbsTarFilePath ,
                    srcSession,
                    destinationURI.getPath() ,
                    destSession,
                    RemoteCluster.DIRECTION.FROM,
                    true);

            // delete tar in remote computer resource
            commandInfo = new RawCommandInfo("rm " + resourceAbsTarFilePath);
            remoteCluster.execute(commandInfo);

            // untar file and delete tar in storage resource
            String destPath = destinationURI.getPath();
            String destParent = destPath.substring(0, destPath.lastIndexOf("/"));
            String storageArchiveDir = "ARCHIVE";
            commandInfo = new RawCommandInfo("cd " + destParent + " && mkdir " + storageArchiveDir +
                    " && tar -xvf " + archiveTar + " -C " + storageArchiveDir + " && rm " + archiveTar +
                    " && chmod 755 -R " + storageArchiveDir + "/*");
            executeCommand(destSession, commandInfo, new StandardOutReader());
        } catch (CredentialStoreException e) {
            String msg = "Storage authentication issue, make sure you are passing valid credential token";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } catch ( URISyntaxException | GFacException | TException e) {
            String msg = "Error! Archive task failed";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskErrors(Arrays.asList(errorModel));
        } finally {
            if (registryClient != null) {
                ThriftUtils.close(registryClient);
            }
        }
        return status;
    }


    @Override
    public TaskStatus recover(TaskContext taskContext) {
        return new TaskStatus(TaskState.COMPLETED);
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.DATA_STAGING;
    }



    private void executeCommand(Session session,CommandInfo commandInfo, CommandOutput commandOutput) throws GFacException {
        String command = commandInfo.getCommand();
        ChannelExec channelExec = null;
        try {
            if (!session.isConnected()) {
//                session = getOpenSession();
                log.error("Error! client session is closed");
                throw new JSchException("Error! client session is closed");
            }
            channelExec = ((ChannelExec) session.openChannel("exec"));
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(commandOutput.getStandardError());
            log.info("Executing command {}", commandInfo.getCommand());
            channelExec.connect();
            commandOutput.onOutput(channelExec);
        } catch (JSchException e) {
            throw new GFacException("Unable to execute command - ", e);
        }finally {
            //Only disconnecting the channel, session can be reused
            if (channelExec != null) {
                commandOutput.exitCode(channelExec.getExitStatus());
                channelExec.disconnect();
            }
        }
    }
}
