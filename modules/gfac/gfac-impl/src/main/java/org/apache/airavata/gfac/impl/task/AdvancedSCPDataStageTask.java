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
 *
 */
package org.apache.airavata.gfac.impl.task;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.authentication.SSHKeyAuthentication;
import org.apache.airavata.gfac.core.authentication.SSHPasswordAuthentication;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.task.Task;
import org.apache.airavata.gfac.core.task.TaskException;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.SSHUtils;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.model.task.DataStagingTaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class AdvancedSCPDataStageTask implements Task{
	private static final Logger log = LoggerFactory.getLogger(AdvancedSCPDataStageTask.class);
	private static final int DEFAULT_SSH_PORT = 22;
	private String password;
	private String publicKeyPath;
	private String passPhrase;
	private String privateKeyPath;
	private String userName;
	private String hostName;
	private String inputPath;

	@Override
	public void init(Map<String, String> propertyMap) throws TaskException {
		inputPath = propertyMap.get("inputPath");
        hostName = propertyMap.get("hostName");
        userName = propertyMap.get("userName");
	}

	@Override
	public TaskStatus execute(TaskContext taskContext) {
        TaskStatus status = new TaskStatus(TaskState.CREATED);
        AuthenticationInfo authenticationInfo = null;
        DataStagingTaskModel subTaskModel = null;
        try {
            String tokenId = taskContext.getParentProcessContext().getTokenId();
            CredentialReader credentialReader = GFacUtils.getCredentialReader();
            Credential credential = credentialReader.getCredential(taskContext.getParentProcessContext().getGatewayId(), tokenId);
            if (credential instanceof SSHCredential){
                SSHCredential sshCredential =  (SSHCredential)credential;
                byte[] publicKey = sshCredential.getPublicKey();
                publicKeyPath = writeFileToDisk(publicKey);
                byte[] privateKey = sshCredential.getPrivateKey();
                privateKeyPath = writeFileToDisk(privateKey);
                passPhrase = sshCredential.getPassphrase();
//                userName = sshCredential.getPortalUserName(); // this might not same as login user name
                authenticationInfo = getSSHKeyAuthentication();
            }
            status = new TaskStatus(TaskState.COMPLETED);
            subTaskModel = (DataStagingTaskModel) ThriftUtils.getSubTaskModel
                    (taskContext.getTaskModel());
            URI sourceURI = new URI(subTaskModel.getSource());
            URI destinationURI = new URI(subTaskModel.getDestination());

            File templocalDataDir = getLocalDataDir(taskContext);
            if (!templocalDataDir.exists()) {
                if (!templocalDataDir.mkdirs()) {
                    // failed to create temp output location
                }
            }

            String fileName = sourceURI.getPath().substring(sourceURI.getPath().lastIndexOf(File.separator) + 1,
                    sourceURI.getPath().length());
            String filePath = templocalDataDir + File.separator + fileName;

            ServerInfo serverInfo = new ServerInfo(userName, hostName, DEFAULT_SSH_PORT);
            Session sshSession = Factory.getSSHSession(authenticationInfo, serverInfo);
            ProcessState processState = taskContext.getParentProcessContext().getProcessState();
            if (processState == ProcessState.INPUT_DATA_STAGING) {
                inputDataStaging(taskContext, sshSession, sourceURI, destinationURI, filePath);
                status.setReason("Successfully staged input data");
            }else if (processState == ProcessState.OUTPUT_DATA_STAGING) {
                outputDataStaging(taskContext, sshSession, sourceURI, destinationURI, filePath);
                status.setReason("Successfully staged output data");
            } else {
                status.setState(TaskState.FAILED);
                status.setReason("Invalid task invocation, Support " + ProcessState.INPUT_DATA_STAGING.name() + " and " +
                        "" + ProcessState.OUTPUT_DATA_STAGING.name() + " process phases. found " + processState.name());
            }
        }  catch (TException e) {
			String msg = "Couldn't create subTask model thrift model";
			log.error(msg, e);
			status.setState(TaskState.FAILED);
			status.setReason(msg);
			ErrorModel errorModel = new ErrorModel();
			errorModel.setActualErrorMessage(e.getMessage());
			errorModel.setUserFriendlyMessage(msg);
			taskContext.getTaskModel().setTaskError(errorModel);
			return status;
		} catch (ApplicationSettingsException | FileNotFoundException | CredentialStoreException | IllegalAccessException | InstantiationException e) {
            String msg = "Failed while reading credentials";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskError(errorModel);
        } catch (URISyntaxException e) {
            String msg = "Sorce or destination uri is not correct source : " + subTaskModel.getSource() + ", " +
                    "destination : " + subTaskModel.getDestination();
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskError(errorModel);
        } catch (SSHApiException e) {
            String msg = "Failed to do scp with compute resource";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskError(errorModel);
        } catch (AiravataException e) {
            String msg = "Error while creating ssh session with client";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskError(errorModel);
        } catch (JSchException | IOException e ) {
            String msg = "Failed to do scp with client";
            log.error(msg, e);
            status.setState(TaskState.FAILED);
            status.setReason(msg);
            ErrorModel errorModel = new ErrorModel();
            errorModel.setActualErrorMessage(e.getMessage());
            errorModel.setUserFriendlyMessage(msg);
            taskContext.getTaskModel().setTaskError(errorModel);
        }
		return status;
	}

	private void inputDataStaging(TaskContext taskContext, Session sshSession, URI sourceURI, URI
			destinationURI, String filePath) throws SSHApiException, IOException, JSchException {
		/**
		 * scp remote client file to airavata local dir.
		 */
		SSHUtils.scpFrom(sourceURI.getPath(), filePath, sshSession);

		/**
		 * scp local file to compute resource.
		 */
		taskContext.getParentProcessContext().getRemoteCluster().scpTo(filePath, destinationURI.getPath());
	}

	private void outputDataStaging(TaskContext taskContext, Session sshSession, URI sourceURI, URI destinationURI,
	                               String filePath) throws SSHApiException, AiravataException, IOException, JSchException {
		/**
		 * scp remote file from comute resource to airavata local
		 */
		taskContext.getParentProcessContext().getRemoteCluster().scpFrom(sourceURI.getPath(), filePath);

		/**
		 * scp local file to remote client
		 */
		SSHUtils.scpTo(filePath, destinationURI.getPath(), sshSession);
	}

	private File getLocalDataDir(TaskContext taskContext) {
		String outputPath = ServerSettings.getLocalDataLocation();
		outputPath = (outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator);
		return new File(outputPath + taskContext.getParentProcessContext() .getProcessId());
	}

	@Override
	public TaskStatus recover(TaskContext taskContext) {
		return null;
	}

	@Override
	public TaskTypes getType() {
		return null;
	}

	private SSHPasswordAuthentication getSSHPasswordAuthentication() {
		return new SSHPasswordAuthentication(userName, password);
	}

	private  SSHKeyAuthentication getSSHKeyAuthentication(){
		SSHKeyAuthentication sshKA = new SSHKeyAuthentication();
		sshKA.setUserName(userName);
		sshKA.setPassphrase(passPhrase);
		sshKA.setPrivateKeyFilePath(privateKeyPath);
		sshKA.setPublicKeyFilePath(publicKeyPath);
		sshKA.setStrictHostKeyChecking("no");
		return sshKA;
	}

    private String writeFileToDisk(byte[] data) {
        File temp = null;
        try {
            temp = File.createTempFile("id_rsa", "");
            //write it
            FileOutputStream bw = new FileOutputStream(temp);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return temp.getAbsolutePath();
    }
}
