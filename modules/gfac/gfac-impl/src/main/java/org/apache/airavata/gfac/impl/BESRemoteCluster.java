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

import com.jcraft.jsch.Session;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.JobManagerConfiguration;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.cluster.AbstractRemoteCluster;
import org.apache.airavata.gfac.core.cluster.CommandInfo;
import org.apache.airavata.gfac.core.cluster.JobSubmissionOutput;
import org.apache.airavata.gfac.core.cluster.ServerInfo;
import org.apache.airavata.model.status.JobStatus;

import java.util.List;
import java.util.Map;

public class BESRemoteCluster extends AbstractRemoteCluster{
    public BESRemoteCluster(ServerInfo serverInfo, JobManagerConfiguration jobManagerConfiguration, AuthenticationInfo authenticationInfo) {
        super(serverInfo, jobManagerConfiguration, authenticationInfo);
    }

    @Override
    public JobSubmissionOutput submitBatchJob(String jobScriptFilePath, String workingDirectory) throws GFacException {
        return null;
    }

    @Override
    public void copyTo(String localFile, String remoteFile) throws GFacException {

    }

    @Override
    public void copyFrom(String remoteFile, String localFile) throws GFacException {

    }

    @Override
    public void scpThirdParty(String sourceFile, Session srcSession, String destSession,
                              Session session, DIRECTION inOrOut, boolean ignoreEmptyFile) throws GFacException {

    }

    /**
     * This method can be used to get the file name of a file giving the extension. It assumes that there will be only
     * one file with that extension. In case if there are more than one file one random file name from the matching ones
     * will be returned.
     *
     * @param fileExtension
     * @param parentPath
     * @param session
     * @return
     */
    @Override
    public List<String> getFileNameFromExtension(String fileExtension, String parentPath, Session session) {
        return null;
    }

    @Override
    public void makeDirectory(String directoryPath) throws GFacException {

    }

    @Override
    public JobStatus cancelJob(String jobID) throws GFacException {
        return null;
    }

    @Override
    public JobStatus getJobStatus(String jobID) throws GFacException {
        return null;
    }

    @Override
    public String getJobIdByJobName(String jobName, String userName) throws GFacException {
        return null;
    }

    @Override
    public void getJobStatuses(String userName, Map<String, JobStatus> jobIDs) throws GFacException {

    }

    @Override
    public List<String> listDirectory(String directoryPath) throws GFacException {
        return null;
    }

    @Override
    public boolean execute(CommandInfo commandInfo) throws GFacException {
        return false;
    }

    @Override
    public Session getSession() throws GFacException {
        return null;
    }

    @Override
    public void disconnect() throws GFacException {

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
