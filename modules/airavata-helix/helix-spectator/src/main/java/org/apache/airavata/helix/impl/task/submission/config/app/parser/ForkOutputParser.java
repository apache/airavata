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
package org.apache.airavata.helix.impl.task.submission.config.app.parser;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.impl.task.submission.config.OutputParser;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ForkOutputParser implements OutputParser {
    private static final Logger log = LoggerFactory.getLogger(ForkOutputParser.class);

    @Override
    public String parseJobSubmission(String rawOutput) throws Exception {
	    return AiravataUtils.getId("JOB_ID_");
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        return false;
    }

    @Override
    public JobStatus parseJobStatus(String jobID, String rawOutput) throws Exception {
        return null;
    }

    @Override
    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception {

    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        // For fork jobs there is no job ID, hence airavata generates a job ID
        return AiravataUtils.getId(jobName);
    }
}
