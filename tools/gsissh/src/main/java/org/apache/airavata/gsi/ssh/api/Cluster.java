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
package org.apache.airavata.gsi.ssh.api;

import java.util.List;
import java.util.Map;

import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.JobStatus;

import com.jcraft.jsch.Session;

/**
 * This interface represents a Cluster machine
 * End users of the API can implement this and come up with their own
 * implementations, but mostly this interface is for internal usage.
 */
public interface Cluster {

    /**
     * This will submit a job to the cluster with a given pbs file and some parameters
     *
     * @param pbsFilePath  path of the pbs file
     * @param workingDirectory working directory where pbs should has to copy
     * @return jobId after successful job submission
     * @throws SSHApiException throws exception during error
     */
    public String submitBatchJobWithScript(String pbsFilePath, String workingDirectory) throws SSHApiException;

    /**
     * This will submit the given job and not performing any monitoring
     *
     * @param jobDescriptor  job descriptor to submit to cluster, this contains all the parameter
     * @return jobID after successful job submission.
     * @throws SSHApiException  throws exception during error
     */
    public String submitBatchJob(JobDescriptor jobDescriptor) throws SSHApiException;

    /**
     * This will copy the localFile to remoteFile location in configured cluster
     *
     * @param remoteFile remote file location, this can be a directory too
     * @param localFile local file path of the file which needs to copy to remote location
     * @throws SSHApiException throws exception during error
     */
    public void scpTo(String remoteFile, String localFile) throws SSHApiException;

    /**
     * This will copy a remote file in path rFile to local file lFile
     * @param remoteFile remote file path, this has to be a full qualified path
     * @param localFile This is the local file to copy, this can be a directory too
     * @throws SSHApiException
     */
    public void scpFrom(String remoteFile, String localFile) throws SSHApiException;

    /**
     * This will copy a remote file in path rFile to local file lFile
     * @param remoteFile remote file path, this has to be a full qualified path
     * @param localFile This is the local file to copy, this can be a directory too
     * @throws SSHApiException
     */
    public void scpThirdParty(String remoteFileSorce, String remoteFileTarget) throws SSHApiException;
    
    /**
     * This will create directories in computing resources
     * @param directoryPath the full qualified path for the directory user wants to create
     * @throws SSHApiException throws during error
     */
    public void makeDirectory(String directoryPath) throws SSHApiException;


    /**
     * This will get the job description of a job which is there in the cluster
     * if jbo is not available with the given ID it returns
     * @param jobID jobId has to pass
     * @return Returns full job description of the job which submitted successfully
     * @throws SSHApiException throws exception during error
     */
    public JobDescriptor getJobDescriptorById(String jobID) throws SSHApiException;

    /**
     * This will delete the given job from the queue
     *
     * @param jobID  jobId of the job which user wants to delete
     * @return return the description of the deleted job
     * @throws SSHApiException throws exception during error
     */
    public JobDescriptor cancelJob(String jobID) throws SSHApiException;

    /**
     * This will get the job status of the the job associated with this jobId
     *
     * @param jobID jobId of the job user want to get the status
     * @return job status of the given jobID
     * @throws SSHApiException throws exception during error
     */
    public JobStatus getJobStatus(String jobID) throws SSHApiException;

    /**
     * This method can be used to poll the jobstatuses based on the given
     * user but we should pass the jobID list otherwise we will get unwanted
     * job statuses which submitted by different middleware outside apache
     * airavata with the same uername which we are not considering
     * @param userName userName of the jobs which required to get the status
     * @param jobIDs precises set of jobIDs
     * @return
     */
    public void getJobStatuses(String userName,Map<String,JobStatus> jobIDs)throws SSHApiException;
    /**
     * This will list directories in computing resources
     * @param directoryPath the full qualified path for the directory user wants to create
     * @throws SSHApiException throws during error
     */
    public List<String> listDirectory(String directoryPath) throws SSHApiException;

    /**
     * This method can be used to get created ssh session
     * to reuse the created session.
     * @throws SSHApiException
     */
    public Session getSession() throws SSHApiException;
    
    /**
     * This method can be used to close the connections initialized
     * to handle graceful shutdown of the system
     * @throws SSHApiException
     */
    public void disconnect() throws SSHApiException;

    /**
     * This gives the server Info
     * @return
     */
    public ServerInfo getServerInfo();

}
