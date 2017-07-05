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
package org.apache.airavata.gfac.core.cluster;

import com.jcraft.jsch.Session;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.model.status.JobStatus;

import java.util.List;
import java.util.Map;

/**
 * This interface represents a RemoteCluster machine
 * End users of the API can implement this and come up with their own
 * implementations, but mostly this interface is for internal usage.
 */
public interface RemoteCluster { // FIXME: replace SSHApiException with suitable exception.

	/**
	 * This will submit a job to the cluster with a given pbs file and some parameters
	 *
	 * @param jobScriptFilePath path of the job script file
	 * @param workingDirectory  working directory where pbs should has to copy
	 * @return jobId after successful job submission
	 * @throws SSHApiException throws exception during error
	 */
	public JobSubmissionOutput submitBatchJob(String jobScriptFilePath, String workingDirectory) throws GFacException;

	/**
	 * This will copy the localFile to remoteFile location in configured cluster
	 *
	 * @param localFile  local file path of the file which needs to copy to remote location
	 * @param remoteFile remote file location, this can be a directory too
	 * @throws SSHApiException throws exception during error
	 */
	public void copyTo(String localFile, String remoteFile) throws GFacException;

	/**
	 * This will copy a remote file in path rFile to local file lFile
	 *
	 * @param remoteFile      remote file path, this has to be a full qualified path
	 * @param localFile This is the local file to copy, this can be a directory too
	 */
	public void copyFrom(String remoteFile, String localFile) throws GFacException;

	/**
	 * This wil copy source remote file to target remote file.
	 *
	 * @param sourceFile remote file path, this has to be a full qualified path
	 * @param srcSession jcraft session of other coner of thirdparty file transfer.
	 * @param destinationFile This is the local file to copy, this can be a directory too
     * @param destSession jcraft session of other coner of thirdparty file transfer.
     * @param inOrOut direction to file transfer , to the remote cluster(DIRECTION.IN) or from the remote cluster(DIRECTION.OUT)
	 *
	 */
	public void scpThirdParty(String sourceFile,
							  Session srcSession,
							  String destinationFile ,
							  Session destSession ,
							  DIRECTION inOrOut,
							  boolean ignoreEmptyFile) throws GFacException;

	/**
	 * This method can be used to get the file name of a file giving the extension. It assumes that there will be only
	 * one file with that extension. In case if there are more than one file one random file name from the matching ones
	 * will be returned.
	 * @param fileExtension
	 * @param parentPath
	 * @param session
	 * @return
	 */
	public List<String> getFileNameFromExtension(String fileExtension, String parentPath, Session session) throws GFacException;

	/**
	 * This will create directories in computing resources
	 *
	 * @param directoryPath the full qualified path for the directory user wants to create
	 * @throws SSHApiException throws during error
	 */
	public void makeDirectory(String directoryPath) throws  GFacException;

	/**
	 * This will delete the given job from the queue
	 *
	 * @param jobID jobId of the job which user wants to delete
	 * @return return the description of the deleted job
	 * @throws SSHApiException throws exception during error
	 */
	public JobStatus cancelJob(String jobID) throws GFacException;

	/**
	 * This will get the job status of the the job associated with this jobId
	 *
	 * @param jobID jobId of the job user want to get the status
	 * @return job status of the given jobID
	 * @throws SSHApiException throws exception during error
	 */
	public JobStatus getJobStatus(String jobID) throws GFacException;

	/**
	 * This will get the job status of the the job associated with this jobId
	 *
	 * @param jobName jobName of the job user want to get the status
	 * @return jobId of the given jobName
	 * @throws SSHApiException throws exception during error
	 */
	public String getJobIdByJobName(String jobName, String userName) throws GFacException;

	/**
	 * This method can be used to poll the jobstatuses based on the given
	 * user but we should pass the jobID list otherwise we will get unwanted
	 * job statuses which submitted by different middleware outside apache
	 * airavata with the same uername which we are not considering
	 *
	 * @param userName userName of the jobs which required to get the status
	 * @param jobIDs   precises set of jobIDs
	 */
	public void getJobStatuses(String userName, Map<String, JobStatus> jobIDs) throws GFacException;

	/**
	 * This will list directories in computing resources
	 *
	 * @param directoryPath the full qualified path for the directory user wants to create
	 * @throws SSHApiException throws during error
	 */
	public List<String> listDirectory(String directoryPath) throws GFacException;

	/**
	 * This method can use to execute custom command on remote compute resource.
	 * @param commandInfo
	 * @return <code>true</code> if command successfully executed, <code>false</code> otherwise.
	 * @throws SSHApiException
	 */
	public boolean execute(CommandInfo commandInfo) throws GFacException;

	/**
	 * This method can be used to get created ssh session
	 * to reuse the created session.
	 */
	public Session getSession() throws GFacException;

	/**
	 * This method can be used to close the connections initialized
	 * to handle graceful shutdown of the system
	 */
	public void disconnect() throws GFacException;

	/**
	 * This gives the server Info
	 */
	public ServerInfo getServerInfo();

    public AuthenticationInfo getAuthentication();
    enum DIRECTION {
        TO,
        FROM
    }

}
