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
package org.apache.airavata.helix.impl.task.submission.config;

import org.apache.airavata.model.status.JobStatus;

import java.util.Map;

public interface OutputParser {

    /**
     * This can be used to parseSingleJob the result of a job submission to get the JobID
     * @param rawOutput
     * @return the job id as a String, or null if no job id found
     */
    public String parseJobSubmission(String rawOutput) throws Exception;


    /**
     * Parse output return by job submission task and identify jobSubmission failures.
     * @param rawOutput
     * @return true if job submission has been failed, false otherwise.
     */
    public boolean isJobSubmissionFailed(String rawOutput);


    /**
     * This can be used to get the job status from the output
     * @param jobID
     * @param rawOutput
     */
    public JobStatus parseJobStatus(String jobID, String rawOutput) throws Exception;

    /**
     * This can be used to parseSingleJob a big output and get multipleJob statuses
     * @param statusMap list of status map will return and key will be the job ID
     * @param rawOutput
     */
    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception;


    public String parseJobId(String jobName, String rawOutput) throws Exception;
}
