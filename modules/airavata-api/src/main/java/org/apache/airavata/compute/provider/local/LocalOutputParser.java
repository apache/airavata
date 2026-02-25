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
package org.apache.airavata.compute.provider.local;

import java.util.Map;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.compute.resource.submission.JobOutputParser;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalOutputParser implements JobOutputParser {
    private static final Logger log = LoggerFactory.getLogger(LocalOutputParser.class);

    @Override
    public String parseJobSubmission(String rawOutput) throws Exception {
        return IdGenerator.getId("JOB_ID_");
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        return false;
    }

    @Override
    public StatusModel<JobState> parseJobStatus(String jobID, String rawOutput) throws Exception {
        return null;
    }

    @Override
    public void parseJobStatuses(String userName, Map<String, StatusModel<JobState>> statusMap, String rawOutput)
            throws Exception {}

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        // For fork jobs there is no job ID, hence airavata generates a job ID
        return IdGenerator.getId(jobName);
    }
}
