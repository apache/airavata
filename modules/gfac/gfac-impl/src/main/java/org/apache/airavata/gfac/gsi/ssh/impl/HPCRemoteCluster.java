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
package org.apache.airavata.gfac.gsi.ssh.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.authentication.SSHKeyAuthentication;
import org.apache.airavata.gfac.core.cluster.CommandInfo;
import org.apache.airavata.gfac.core.cluster.CommandOutput;
import org.apache.airavata.gfac.core.cluster.RemoteCluster;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * This is the default implementation of a cluster.
 * this has most of the methods to be used by the end user of the
 * library.
 */
public class HPCRemoteCluster implements RemoteCluster{
    private static final Logger log = LoggerFactory.getLogger(HPCRemoteCluster.class);
	private final SSHKeyAuthentication authentication;
	private final ServerInfo serverInfo;
	private final JobManagerConfiguration jobManagerConfiguration;
	private final JSch jSch;
	private Session session;

	public HPCRemoteCluster(ServerInfo serverInfo, JobManagerConfiguration jobManagerConfiguration, AuthenticationInfo
			authenticationInfo) throws AiravataException {
		try {
			this.serverInfo = serverInfo;
			this.jobManagerConfiguration = jobManagerConfiguration;
			if (authenticationInfo instanceof SSHKeyAuthentication) {
				authentication = (SSHKeyAuthentication) authenticationInfo;
			} else {
				throw new AiravataException("Support ssh key authentication only");
			}

			jSch = new JSch();
			jSch.addIdentity(authentication.getPrivateKeyFilePath(), authentication.getPublicKeyFilePath(), authentication
					.getPassphrase().getBytes());
			session = jSch.getSession(serverInfo.getUserName(), serverInfo.getHost(), serverInfo.getPort());
			session.setUserInfo(new DefaultUserInfo(serverInfo.getUserName(), null, authentication.getPassphrase()));
			session.connect(); // 0 connection timeout
		} catch (JSchException e) {
			throw new AiravataException("JSch initialization error ", e);
		}
	}

	@Override
	public String submitBatchJob(String jobScriptFilePath, String workingDirectory) throws SSHApiException {
		
		return null;
	}

	@Override
	public void scpTo(String sourceFile, String destinationFile) throws SSHApiException {

	}

	@Override
	public void scpFrom(String sourceFile, String destinationFile) throws SSHApiException {

	}

	@Override
	public void scpThirdParty(String remoteFileSource, String remoteFileTarget) throws SSHApiException {

	}

	@Override
	public void makeDirectory(String directoryPath) throws SSHApiException {

	}

	@Override
	public boolean cancelJob(String jobID) throws SSHApiException {
		return false;
	}

	@Override
	public JobStatus getJobStatus(String jobID) throws SSHApiException {
		return null;
	}

	@Override
	public String getJobIdByJobName(String jobName, String userName) throws SSHApiException {
		return null;
	}

	@Override
	public void getJobStatuses(String userName, Map<String, JobStatus> jobIDs) throws SSHApiException {

	}

	@Override
	public List<String> listDirectory(String directoryPath) throws SSHApiException {
		return null;
	}

	@Override
	public Session getSession() throws SSHApiException {
		return null;
	}

	@Override
	public void disconnect() throws SSHApiException {

	}

	private void executeCommand(CommandInfo commandInfo, CommandOutput commandOutput) throws SSHApiException {
		String command = commandInfo.getCommand();
		ChannelExec channelExec = null;
		try {
			if (!session.isConnected()) {
				session.connect();
			}
			channelExec = ((ChannelExec) session.openChannel("exec"));
			channelExec.setCommand(command);
		    channelExec.setInputStream(null);
			channelExec.setErrStream(commandOutput.getStandardError());
			channelExec.connect();
			commandOutput.onOutput(channelExec);
		} catch (JSchException e) {
			throw new SSHApiException("Unable to execute command - ", e);
		}finally {
			//Only disconnecting the channel, session can be reused
			channelExec.disconnect();
		}
	}

	@Override
	public ServerInfo getServerInfo() {
		return this.serverInfo;
	}

	private class DefaultUserInfo implements UserInfo {

		private String userName;
		private String password;
		private String passphrase;

		public DefaultUserInfo(String userName, String password, String passphrase) {
			this.userName = userName;
			this.password = password;
			this.passphrase = passphrase;
		}

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassword(String s) {
			return false;
		}

		@Override
		public boolean promptPassphrase(String s) {
			return false;
		}

		@Override
		public boolean promptYesNo(String s) {
			return false;
		}

		@Override
		public void showMessage(String s) {

		}
	}
}
