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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.airavata.helix.impl.task.submission.config.OutputParser;
import org.apache.airavata.helix.impl.task.submission.config.app.JobUtil;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlurmOutputParser implements OutputParser {
    private static final Logger log = LoggerFactory.getLogger(SlurmOutputParser.class);
    public static final int JOB_NAME_OUTPUT_LENGTH = 8;
    public static final String STATUS = "status";
    public static final String JOBID = "jobId";

    /**
     * This can be used to parseSingleJob the outpu of sbatch and extrac the jobID from the content
     *
     * @param rawOutput
     * @return
     */
    public String parseJobSubmission(String rawOutput) throws Exception {
        log.info(rawOutput);
        Pattern pattern = Pattern.compile("Submitted batch job (?<" + JOBID + ">[^\\s]*)");
        Matcher matcher = pattern.matcher(rawOutput);
        if (matcher.find()) {
            return matcher.group(JOBID);
        }
        return "";
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        Pattern pattern = Pattern.compile("FAILED");
        Matcher matcher = pattern.matcher(rawOutput);
        return matcher.find();
    }

    public JobStatus parseJobStatus(String jobID, String rawOutput) throws Exception {
        log.info(rawOutput);
        Pattern pattern = Pattern.compile(jobID + "(?=\\s+\\S+\\s+\\S+\\s+\\S+\\s+(?<" + STATUS + ">\\w+))");
        Matcher matcher = pattern.matcher(rawOutput);
        if (matcher.find()) {
            return new JobStatus(JobUtil.getJobState(matcher.group(STATUS)));
        }
        return null;
    }

    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception {
        log.debug(rawOutput);
        String[] info = rawOutput.split("\n");
        String lastString = info[info.length - 1];
        if (lastString.contains("JOBID") || lastString.contains("PARTITION")) {
            log.info("There are no jobs with this username ... ");
            return;
        }
        //        int lastStop = 0;
        for (String jobID : statusMap.keySet()) {
            String jobId = jobID.split(",")[0];
            String jobName = jobID.split(",")[1];
            boolean found = false;
            for (int i = 0; i < info.length; i++) {
                if (info[i].contains(jobName.substring(0, 8))) {
                    // now starts processing this line
                    log.info(info[i]);
                    String correctLine = info[i];
                    String[] columns = correctLine.split(" ");
                    List<String> columnList = new ArrayList<String>();
                    for (String s : columns) {
                        if (!"".equals(s)) {
                            columnList.add(s);
                        }
                    }
                    try {
                        statusMap.put(jobID, new JobStatus(JobState.valueOf(columnList.get(4))));
                    } catch (IndexOutOfBoundsException e) {
                        statusMap.put(jobID, new JobStatus(JobState.valueOf("U")));
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("Couldn't find the status of the Job with JobName: " + jobName + "Job Id: " + jobId);
            }
        }
    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        String regJobId = "jobId";
        if (jobName == null) {
            return null;
        } else if (jobName.length() > JOB_NAME_OUTPUT_LENGTH) {
            jobName = jobName.substring(0, JOB_NAME_OUTPUT_LENGTH);
        }
        Pattern pattern = Pattern.compile(
                "(?=(?<" + regJobId + ">\\d+)\\s+\\w+\\s+" + jobName + ")"); // regex - look ahead and match
        if (rawOutput != null) {
            Matcher matcher = pattern.matcher(rawOutput);
            if (matcher.find()) {
                return matcher.group(regJobId);
            } else {
                log.error("No match is found for JobName");
                return null;
            }
        } else {
            log.error("Error: RawOutput shouldn't be null");
            return null;
        }
    }
}
