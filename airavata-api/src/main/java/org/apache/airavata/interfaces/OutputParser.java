/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.interfaces;

import java.util.Map;
import org.apache.airavata.model.status.proto.JobStatus;

/**
 * SPI contract for parsing job submission/monitoring output.
 */
public interface OutputParser {

    /**
     * Parse the result of a job submission to get the JobID.
     */
    String parseJobSubmission(String rawOutput) throws Exception;

    /**
     * Check if the job submission has failed.
     */
    boolean isJobSubmissionFailed(String rawOutput);

    /**
     * Get the job status from monitoring output.
     */
    JobStatus parseJobStatus(String jobID, String rawOutput) throws Exception;

    /**
     * Parse multiple job statuses from a batch output.
     */
    void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception;

    /**
     * Parse the job ID from a job name.
     */
    String parseJobId(String jobName, String rawOutput) throws Exception;
}
