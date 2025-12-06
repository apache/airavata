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
package org.apache.airavata.helix.impl.task.submission.config.app.parser;

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLSFOutputParser {

    private static final Logger logger = LoggerFactory.getLogger(TestLSFOutputParser.class);

    public static void main(String[] args) {
        String test = "Job <2477982> is submitted to queue <short>.";
        logger.info("Job ID: {}", test.substring(test.indexOf("<") + 1, test.indexOf(">")));
        String test1 = "JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME\n"
                + "2636607 lg11w   RUN   long       ghpcc06     c11b02      *069656647 Mar  7 00:58\n"
                + "2636582 lg11w   RUN   long       ghpcc06     c02b01      2134490944 Mar  7 00:48";
        Map<String, JobStatus> statusMap = new HashMap<String, JobStatus>();
        statusMap.put("2477983,2134490944", new JobStatus(JobState.UNKNOWN));
        LSFOutputParser lsfOutputParser = new LSFOutputParser();
        try {
            lsfOutputParser.parseJobStatuses("cjh", statusMap, test1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("Job Status: {}", statusMap.get("2477983,2134490944"));
    }
}
