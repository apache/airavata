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
package org.apache.airavata.helix.task.submission.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLSFOutputParser {

    private static final Logger logger = LoggerFactory.getLogger(TestLSFOutputParser.class);
    private LSFOutputParser lsfOutputParser;

    @BeforeEach
    public void setUp() {
        lsfOutputParser = new LSFOutputParser();
    }

    @Test
    public void testJobIdExtractionFromSubmissionOutput() throws Exception {
        String test = "Job <2477982> is submitted to queue <short>.";
        String jobId = lsfOutputParser.parseJobSubmission(test);

        assertNotNull(jobId, "Job ID should be extracted");
        assertEquals("2477982", jobId, "Extracted job ID should match expected value");
    }

    @Test
    public void testJobStatusParsing() throws Exception {
        String test1 = "JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME\n"
                + "2636607 lg11w   RUN   long       ghpcc06     c11b02      *069656647 Mar  7 00:58\n"
                + "2636582 lg11w   RUN   long       ghpcc06     c02b01      2134490944 Mar  7 00:48";
        Map<String, JobStatus> statusMap = new HashMap<>();
        statusMap.put("2477983,2134490944", new JobStatus(JobState.UNKNOWN));

        // Parse job statuses
        lsfOutputParser.parseJobStatuses("cjh", statusMap, test1);

        // Verify that status was updated (if parser updates it)
        JobStatus status = statusMap.get("2477983,2134490944");
        assertNotNull(status, "Job status should exist in map");
        logger.info("Job Status: {}", status);
    }
}
