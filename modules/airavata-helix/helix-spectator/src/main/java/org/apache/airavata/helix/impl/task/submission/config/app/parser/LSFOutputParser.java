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

import org.apache.airavata.helix.impl.task.submission.config.OutputParser;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LSFOutputParser implements OutputParser {
    private final static Logger logger = LoggerFactory.getLogger(LSFOutputParser.class);

    @Override
    public String parseJobSubmission(String rawOutput) throws Exception {
        logger.debug(rawOutput);
        if (rawOutput.indexOf("<") >= 0) {
            return rawOutput.substring(rawOutput.indexOf("<")+1,rawOutput.indexOf(">"));
        } else {
            return null;
        }
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        return false;
    }

    @Override
    public JobStatus parseJobStatus(String jobID, String rawOutput) throws Exception {
        boolean jobFount = false;
        logger.debug(rawOutput);
        //todo this is not used anymore
        return null;
    }

    @Override
    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception {
        logger.debug(rawOutput);

        String[]    info = rawOutput.split("\n");
//        int lastStop = 0;
        for (String jobID : statusMap.keySet()) {
            String jobName = jobID.split(",")[1];
            boolean found = false;
            for (int i = 0; i < info.length; i++) {
                if (info[i].contains(jobName.substring(0,8))) {
                    // now starts processing this line
                    logger.info(info[i]);
                    String correctLine = info[i];
                    String[] columns = correctLine.split(" ");
                    List<String> columnList = new ArrayList<String>();
                    for (String s : columns) {
                        if (!"".equals(s)) {
                            columnList.add(s);
                        }
                    }
//                    lastStop = i + 1;
                    try {
	                    statusMap.put(jobID, new JobStatus(JobState.valueOf(columnList.get(2))));
                    }catch(IndexOutOfBoundsException e) {
	                    statusMap.put(jobID, new JobStatus(JobState.valueOf("U")));
                    }
                    found = true;
                    break;
                }
            }
            if(!found)
                logger.error("Couldn't find the status of the Job with JobName: " + jobName + "Job Id: " + jobID.split(",")[0]);
        }
    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        String regJobId = "jobId";
        Pattern pattern = Pattern.compile("(?=(?<" + regJobId + ">\\d+)\\s+\\w+\\s+" + jobName + ")"); // regex - look ahead and match
        if (rawOutput != null) {
            Matcher matcher = pattern.matcher(rawOutput);
            if (matcher.find()) {
                return matcher.group(regJobId);
            } else {
                logger.error("No match is found for JobName");
                return null;
            }
        } else {
            logger.error("Error: RawOutput shouldn't be null");
            return null;
        }
    }

    public static void main(String[] args) {
        String test = "Job <2477982> is submitted to queue <short>.";
        System.out.println(test.substring(test.indexOf("<")+1, test.indexOf(">")));
        String test1 = "JOBID   USER    STAT  QUEUE      FROM_HOST   EXEC_HOST   JOB_NAME   SUBMIT_TIME\n" +
                "2636607 lg11w   RUN   long       ghpcc06     c11b02      *069656647 Mar  7 00:58\n" +
                "2636582 lg11w   RUN   long       ghpcc06     c02b01      2134490944 Mar  7 00:48";
        Map<String, JobStatus> statusMap = new HashMap<String, JobStatus>();
        statusMap.put("2477983,2134490944", new JobStatus(JobState.UNKNOWN));
        LSFOutputParser lsfOutputParser = new LSFOutputParser();
        try {
            lsfOutputParser.parseJobStatuses("cjh", statusMap, test1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        System.out.println(statusMap.get("2477983,2134490944"));

    }
}
