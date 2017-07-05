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
package org.apache.airavata.gfac.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.authentication.SSHKeyAuthentication;
import org.apache.airavata.gfac.core.cluster.AbstractRemoteCluster;
import org.apache.airavata.gfac.core.cluster.CommandInfo;
import org.apache.airavata.gfac.core.cluster.CommandOutput;
import org.apache.airavata.gfac.core.cluster.JobSubmissionOutput;
import org.apache.airavata.gfac.core.cluster.RawCommandInfo;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * One Remote cluster instance for each compute resource.
 */
public class HPCRemoteCluster extends AbstractRemoteCluster{
    private static final Logger log = LoggerFactory.getLogger(HPCRemoteCluster.class);
	private static final int MAX_RETRY_COUNT = 3;
	private final SSHKeyAuthentication authentication;
	private final JSch jSch;

	public HPCRemoteCluster(ServerInfo serverInfo, JobManagerConfiguration jobManagerConfiguration, AuthenticationInfo
			authenticationInfo) throws AiravataException, GFacException {
		super(serverInfo, jobManagerConfiguration, authenticationInfo);
		try {
			if (authenticationInfo instanceof SSHKeyAuthentication) {
				authentication = (SSHKeyAuthentication) authenticationInfo;
			} else {
				throw new AiravataException("Support ssh key authentication only");
			}
			jSch = new JSch();
			jSch.addIdentity(UUID.randomUUID().toString(), authentication.getPrivateKey(), authentication.getPublicKey(),
					authentication.getPassphrase().getBytes());
		} catch (JSchException e) {
			throw new AiravataException("JSch initialization error ", e);
		}
	}

	private Session getOpenSession() throws JSchException {
		Session newSession = jSch.getSession(serverInfo.getUserName(), serverInfo.getHost(), serverInfo.getPort());
		newSession.setUserInfo(new DefaultUserInfo(serverInfo.getUserName(), null, authentication.getPassphrase()));
		if (authentication.getStrictHostKeyChecking().equals("yes")) {
			jSch.setKnownHosts(authentication.getKnownHostsFilePath());
		} else {
			newSession.setConfig("StrictHostKeyChecking", "no");
		}
		newSession.connect(); // 0 connection timeout
		return newSession;
	}

	@Override
	public JobSubmissionOutput submitBatchJob(String jobScriptFilePath, String workingDirectory) throws GFacException {
		JobSubmissionOutput jsoutput = new JobSubmissionOutput();
		copyTo(jobScriptFilePath, workingDirectory); // scp script file to working directory
		RawCommandInfo submitCommand = jobManagerConfiguration.getSubmitCommand(workingDirectory, jobScriptFilePath);
		submitCommand.setRawCommand("cd " + workingDirectory + "; " + submitCommand.getRawCommand());
		StandardOutReader reader = new StandardOutReader();
		executeCommand(submitCommand, reader);
//		throwExceptionOnError(reader, submitCommand);
		jsoutput.setJobId(outputParser.parseJobSubmission(reader.getStdOutputString()));
		if (jsoutput.getJobId() == null) {
			if (outputParser.isJobSubmissionFailed(reader.getStdOutputString())) {
				jsoutput.setJobSubmissionFailed(true);
				jsoutput.setFailureReason("stdout : " + reader.getStdOutputString() +
						"\n stderr : " + reader.getStdErrorString());
			}
		}
		jsoutput.setExitCode(reader.getExitCode());
		if (jsoutput.getExitCode() != 0) {
			jsoutput.setJobSubmissionFailed(true);
			jsoutput.setFailureReason("stdout : " + reader.getStdOutputString() +
					"\n stderr : " + reader.getStdErrorString());
		}
		jsoutput.setStdOut(reader.getStdOutputString());
		jsoutput.setStdErr(reader.getStdErrorString());
		return jsoutput;
	}

	@Override
	public void copyTo(String localFile, String remoteFile) throws GFacException {
		int retry = 3;
		while (retry > 0) {
			try {
				log.info("Transferring localhost:" + localFile  + " to " + serverInfo.getHost() + ":" + remoteFile);
				SSHUtils.scpTo(localFile, remoteFile,  getSshSession());
				retry = 0;
			} catch (Exception e) {
				retry--;
				if (retry == 0) {
					throw new GFacException("Failed to scp localhost:" + localFile + " to " + serverInfo.getHost() +
							":" + remoteFile, e);
				} else {
					log.info("Retry transfer localhost:" + localFile + " to " + serverInfo.getHost() + ":" +
							remoteFile);
				}
			}
		}
	}

	private Session getSshSession() throws GFacException {
		return Factory.getSSHSession(authenticationInfo, serverInfo);
	}

	@Override
	public void copyFrom(String remoteFile, String localFile) throws GFacException {
		int retry = 3;
		while(retry>0) {
			try {
				log.info("Transferring " + serverInfo.getHost() + ":" + remoteFile + " To localhost:" + localFile);
				SSHUtils.scpFrom(remoteFile, localFile, getSession());
				retry=0;
			} catch (Exception e) {
				retry--;
				if (retry == 0) {
					throw new GFacException("Failed to scp " + serverInfo.getHost() + ":" + remoteFile + " to " +
							"localhost:" + localFile, e);
				} else {
					log.info("Retry transfer " + serverInfo.getHost() + ":" + remoteFile + "  to localhost:" + localFile);
				}
			}
		}
	}

	@Override
	public void scpThirdParty(String sourceFile,
							  Session srcSession,
							  String destinationFile,
							  Session destSession,
							  DIRECTION direction,
							  boolean ignoreEmptyFile) throws GFacException {
		int retryCount= 0;
		try {
			while (retryCount < MAX_RETRY_COUNT) {
				retryCount++;
				log.info("Transferring from:" + sourceFile + " To: " + destinationFile);
				try {
					SSHUtils.scpThirdParty(sourceFile, srcSession, destinationFile, destSession, ignoreEmptyFile);
					break; // exit while loop
				} catch (JSchException e) {
					if (retryCount == MAX_RETRY_COUNT) {
						log.error("Retry count " + MAX_RETRY_COUNT + " exceeded for  transferring from:"
								+ sourceFile + " To: " + destinationFile, e);
						throw e;
					}
					log.error("Issue with jsch, Retry transferring from:" + sourceFile + " To: " + destinationFile, e);
				}
			}
        } catch (IOException | JSchException e) {
			throw new GFacException("Failed scp file:" + sourceFile + " to remote file "
					+destinationFile , e);
		}
	}

	/**
	 * This method can be used to get the file name of a file giving the extension. It assumes that there will be only
	 * one file with that extension. In case if there are more than one file one random file name from the matching ones
	 * will be returned.
	 *
	 * @param fileRegex
	 * @param parentPath
	 * @param session
	 * @return
	 */
	//FIXME Find a better way to find wildcard file names
	@Override
	public List<String> getFileNameFromExtension(String fileRegex, String parentPath, Session session) throws GFacException {
		try {
			List<String> fileNames = SSHUtils.listDirectory(parentPath, session);
			List<String> matchingNames = new ArrayList<>();
			for(String fileName : fileNames){
				String tempFileName = fileName;

				//FIXME Find better way to match wildcard file names
				String[] splits = fileRegex.split("\\*");
				boolean matching = true;
				for(String split : splits){
					if(!tempFileName.contains(split)){
						matching = false;
						break;
					}else{
						int idx = tempFileName.indexOf(split);
						tempFileName = tempFileName.substring(idx + split.length());
					}
				}
				if(matching){
					matchingNames.add(fileName);
				}
			}
			log.warn("No matching file found for extension: " + fileRegex + " in the " + parentPath + " directory");
			return matchingNames;
		} catch (Exception e) {
			e.printStackTrace();
			throw new GFacException("Failed to list directory " + parentPath);
		}
	}

	@Override
	public void makeDirectory(String directoryPath) throws GFacException {
		int retryCount = 0;
		try {
			while (retryCount < MAX_RETRY_COUNT) {
				retryCount++;
				log.info("Creating directory: " + serverInfo.getHost() + ":" + directoryPath);
				try {
					SSHUtils.makeDirectory(directoryPath, getSession());
					break;  // Exit while loop
				} catch (JSchException e) {
					if (retryCount == MAX_RETRY_COUNT) {
						log.error("Retry count " + MAX_RETRY_COUNT + " exceeded for creating directory: "
								+ serverInfo.getHost() + ":" + directoryPath, e);

						throw e;
					}
					log.error("Issue with jsch, Retry creating directory: " + serverInfo.getHost() + ":" + directoryPath);
				}
			}
		} catch (JSchException | IOException e) {
			throw new GFacException("Failed to create directory " + serverInfo.getHost() + ":" + directoryPath, e);
		}
	}

	@Override
	public JobStatus cancelJob(String jobId) throws GFacException {
		JobStatus oldStatus = getJobStatus(jobId);
		RawCommandInfo cancelCommand = jobManagerConfiguration.getCancelCommand(jobId);
		StandardOutReader reader = new StandardOutReader();
		executeCommand(cancelCommand, reader);
		throwExceptionOnError(reader, cancelCommand);
		return oldStatus;
	}

	@Override
	public JobStatus getJobStatus(String jobId) throws GFacException {
		RawCommandInfo monitorCommand = jobManagerConfiguration.getMonitorCommand(jobId);
		StandardOutReader reader = new StandardOutReader();
		executeCommand(monitorCommand, reader);
		throwExceptionOnError(reader, monitorCommand);
		return outputParser.parseJobStatus(jobId, reader.getStdOutputString());
	}

	@Override
	public String getJobIdByJobName(String jobName, String userName) throws GFacException {
		RawCommandInfo jobIdMonitorCommand = jobManagerConfiguration.getJobIdMonitorCommand(jobName, userName);
		StandardOutReader reader = new StandardOutReader();
		executeCommand(jobIdMonitorCommand, reader);
		throwExceptionOnError(reader, jobIdMonitorCommand);
		return outputParser.parseJobId(jobName, reader.getStdOutputString());
	}

	@Override
	public void getJobStatuses(String userName, Map<String, JobStatus> jobStatusMap) throws GFacException {
		RawCommandInfo userBasedMonitorCommand = jobManagerConfiguration.getUserBasedMonitorCommand(userName);
		StandardOutReader reader = new StandardOutReader();
		executeCommand(userBasedMonitorCommand, reader);
		throwExceptionOnError(reader, userBasedMonitorCommand);
		outputParser.parseJobStatuses(userName, jobStatusMap, reader.getStdOutputString());
	}

	@Override
	public List<String> listDirectory(String directoryPath) throws GFacException {
		try {
			log.info("Creating directory: " + serverInfo.getHost() + ":" + directoryPath);
			return SSHUtils.listDirectory(directoryPath, getSession());
		} catch (JSchException | IOException e) {
			throw new GFacException("Failed to list directory " + serverInfo.getHost() + ":" + directoryPath, e);
		}
	}

	@Override
	public boolean execute(CommandInfo commandInfo) throws GFacException {
		StandardOutReader reader = new StandardOutReader();
		executeCommand(commandInfo, reader);
		return true;
	}

	@Override
	public Session getSession() throws GFacException {
		return getSshSession();
	}

	@Override
	public void disconnect() throws GFacException {
		Factory.disconnectSSHSession(serverInfo);
	}

	/**
	 * This method return <code>true</code> if there is an error in standard output. If not return <code>false</code>
	 *
	 * @param reader        - command output reader
	 * @param submitCommand - command which executed in remote machine.
	 * @return command has return error or not.
	 */
	private void throwExceptionOnError(StandardOutReader reader, RawCommandInfo submitCommand) throws GFacException {
		String stdErrorString = reader.getStdErrorString();
		String command = submitCommand.getCommand().substring(submitCommand.getCommand().lastIndexOf(File.separator)
				+ 1);
		if (stdErrorString == null) {
			// noting to do
		} else if ((stdErrorString.contains(command.trim()) && !stdErrorString.contains("Warning")) || stdErrorString
				.contains("error")) {
			log.error("Command {} , Standard Error output {}", command, stdErrorString);
			throw new GFacException("Error running command " + command + "  on remote cluster. StandardError: " +
					stdErrorString);
		}
	}

	private void executeCommand(CommandInfo commandInfo, CommandOutput commandOutput) throws GFacException {
		String command = commandInfo.getCommand();
		int retryCount = 0;
		ChannelExec channelExec = null;
		try {
			while (retryCount < MAX_RETRY_COUNT) {
				retryCount++;
				try {
					Session session = getSshSession();
					channelExec = ((ChannelExec) session.openChannel("exec"));
					channelExec.setCommand(command);
					channelExec.setInputStream(null);
					channelExec.setErrStream(commandOutput.getStandardError());
					channelExec.connect();
					log.info("Executing command {}", commandInfo.getCommand());
					commandOutput.onOutput(channelExec);
					break; // exit from while loop
				} catch (JSchException e) {
					if (retryCount == MAX_RETRY_COUNT) {
						log.error("Retry count " + MAX_RETRY_COUNT + " exceeded for executing command : " + command, e);
						throw e;
					}
					log.error("Issue with jsch, Retry executing command : " + command, e);
				}
			}
		} catch (JSchException e) {
			throw new GFacException("Unable to execute command - " + command, e);
		} finally {
			//Only disconnecting the channel, session can be reused
			if (channelExec != null) {
				commandOutput.exitCode(channelExec.getExitStatus());
				channelExec.disconnect();
			}
		}
	}

	@Override
	public ServerInfo getServerInfo() {
		return this.serverInfo;
	}

    @Override
    public AuthenticationInfo getAuthentication() {
        return this.authentication;
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
