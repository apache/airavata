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
package org.apache.airavata.gfac.impl;

import com.jcraft.jsch.Session;
import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.authentication.SSHKeyAuthentication;
import org.apache.airavata.gfac.core.cluster.*;
import org.apache.airavata.model.status.JobStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * Created by shameera on 11/9/15.
 */
public class LocalRemoteCluster extends AbstractRemoteCluster {

    public LocalRemoteCluster(ServerInfo serverInfo, JobManagerConfiguration jobManagerConfiguration, AuthenticationInfo authenticationInfo) {
        super(serverInfo, jobManagerConfiguration, authenticationInfo);
    }

    @Override
    public JobSubmissionOutput submitBatchJob(String jobScriptFilePath, String workingDirectory) throws SSHApiException {
        try {
            JobSubmissionOutput jsoutput = new JobSubmissionOutput();
            copyTo(jobScriptFilePath, workingDirectory); // scp script file to working directory
            RawCommandInfo submitCommand = jobManagerConfiguration.getSubmitCommand(workingDirectory, jobScriptFilePath);
            submitCommand.setRawCommand("cd " + workingDirectory + "; " + submitCommand.getRawCommand());
            LocalCommandOutput localCommandOutput = new LocalCommandOutput();
            executeCommand(submitCommand, localCommandOutput);
            jsoutput.setJobId(outputParser.parseJobSubmission(localCommandOutput.getStandardOut()));
            jsoutput.setExitCode(localCommandOutput.getExitCode());
            jsoutput.setStdOut(localCommandOutput.getStandardOut());
            jsoutput.setStdErr(localCommandOutput.getStandardErrorString());
            return jsoutput;
        } catch (IOException e) {
            throw new SSHApiException("Error while submitting local batch job", e);
        }
    }

    @Override
    public void copyTo(String localFile, String remoteFile) throws SSHApiException {
        Path sourcePath = Paths.get(localFile);
        Path targetPath = Paths.get(remoteFile);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SSHApiException("Error while copying sourceFile: " + sourcePath.toString()
                    + ", to destinationFile: " + targetPath.toString(), e);
        }
    }

    @Override
    public void copyFrom(String remoteFile, String localFile) throws SSHApiException {
        Path sourcePath = Paths.get(remoteFile);
        Path targetPath = Paths.get(localFile);
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SSHApiException("Error while copying sourceFile: " + sourcePath.toString()
                    + ", to destinationFile: " + targetPath.toString(), e);
        }
    }

    @Override
    public void scpThirdParty(String sourceFile, String destinationFile, Session session, DIRECTION inOrOut, boolean ignoreEmptyFile) throws SSHApiException {
        throw new UnsupportedOperationException("Scp third party is not support with LocalRemoteCluster");
    }

    @Override
    public void makeDirectory(String directoryPath) throws SSHApiException {
        Path dirPath = Paths.get(directoryPath);
        Set<PosixFilePermission> perms = new HashSet<>();
        // add permission as rw-r--r-- 644
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.OTHERS_READ);
        FileAttribute<Set<PosixFilePermission>> fileAttributes = PosixFilePermissions.asFileAttribute(perms);
        try {
            Files.createDirectory(dirPath, fileAttributes);
        } catch (IOException e) {
            throw new SSHApiException("Error making directory", e);
        }

    }

    @Override
    public JobStatus cancelJob(String jobID) throws SSHApiException {
        JobStatus oldStatus = getJobStatus(jobID);
        RawCommandInfo cancelCommand = jobManagerConfiguration.getCancelCommand(jobID);
        execute(cancelCommand);
        return oldStatus;
    }


    @Override
    public JobStatus getJobStatus(String jobID) throws SSHApiException {
        RawCommandInfo monitorCommand = jobManagerConfiguration.getMonitorCommand(jobID);
        LocalCommandOutput localCommandOutput = new LocalCommandOutput();
        try {
            executeCommand(monitorCommand, localCommandOutput);
            return outputParser.parseJobStatus(jobID, localCommandOutput.getStandardErrorString());
        } catch (IOException e) {
            throw new SSHApiException("Error while getting jobStatus", e);
        }
    }

    @Override
    public String getJobIdByJobName(String jobName, String userName) throws SSHApiException {
        try {
            RawCommandInfo jobIdMonitorCommand = jobManagerConfiguration.getJobIdMonitorCommand(jobName, userName);
            LocalCommandOutput localCommandOutput = new LocalCommandOutput();
            executeCommand(jobIdMonitorCommand, localCommandOutput);
            return outputParser.parseJobId(jobName, localCommandOutput.getStandardOut());
        } catch (IOException e) {
            throw new SSHApiException("Error while getting jobId using JobName", e);
        }
    }

    @Override
    public void getJobStatuses(String userName, Map<String, JobStatus> jobStatusMap) throws SSHApiException {
        try {
            RawCommandInfo userBasedMonitorCommand = jobManagerConfiguration.getUserBasedMonitorCommand(userName);
            LocalCommandOutput localCommandOutput = new LocalCommandOutput();
            executeCommand(userBasedMonitorCommand, localCommandOutput);
            outputParser.parseJobStatuses(userName, jobStatusMap, localCommandOutput.getStandardOut());
        } catch (IOException e) {
            throw new SSHApiException("Error while getting job statuses", e);
        }
    }

    @Override
    public List<String> listDirectory(String directoryPath) throws SSHApiException {
        File directory = new File(directoryPath);
        List<String> results = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            results.add(file.getName());
        }
        return results;
    }

    @Override
    public boolean execute(CommandInfo commandInfo) throws SSHApiException {
        LocalCommandOutput localCommandOutput = new LocalCommandOutput();
        try {
            executeCommand(commandInfo, localCommandOutput);
        } catch (IOException e) {
            throw new SSHApiException("Error while executing command " + commandInfo.getCommand(), e);
        }
        return true;
    }

    private void executeCommand(CommandInfo commandInfo, LocalCommandOutput localCommandOutput) throws IOException {
        Process process = Runtime.getRuntime().exec(commandInfo.getCommand());
        localCommandOutput.readOutputs(process);
    }

    @Override
    public Session getSession() throws SSHApiException {
        return null;
    }

    @Override
    public void disconnect() throws SSHApiException {

    }

    @Override
    public ServerInfo getServerInfo() {
        return null;
    }

    @Override
    public AuthenticationInfo getAuthentication() {
        return null;
    }
}
