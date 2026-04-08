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
package org.apache.airavata.compute.task;

import java.util.Map;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Output parser for cloud (direct SSH) job submissions. Cloud jobs are executed directly
 * via {@code /bin/bash} and do not use a batch scheduler, so there is no structured job
 * output to parse. This follows the same pattern as {@link ForkOutputParser}.
 */
public class AiravataCustomCommandOutputParser implements OutputParser {
    private static final Logger log = LoggerFactory.getLogger(AiravataCustomCommandOutputParser.class);

    @Override
    public String parseJobSubmission(String rawOutput) throws Exception {
        // Cloud jobs run directly via bash — no scheduler job ID in output.
        // Generate a synthetic job ID like fork jobs do.
        return AiravataUtils.getId("CLOUD_JOB_");
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        // Submission failure is determined by exit code, not output parsing
        return false;
    }

    @Override
    public JobStatus parseJobStatus(String jobID, String rawOutput) throws Exception {
        // Cloud job status is checked via 'ps -p PID' — if the process exists,
        // the exit code is 0 and rawOutput contains status info. Since there is
        // no standard batch scheduler format, return null to let callers handle it.
        return null;
    }

    @Override
    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception {
        // No batch scheduler output to parse for cloud jobs
    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        // Cloud jobs have no external job ID; generate a synthetic one
        return AiravataUtils.getId(jobName);
    }
}
