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
package org.apache.airavata.task.submission.parser;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.task.submission.OutputParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LSFOutputParser implements OutputParser {
    private static final Logger logger = LoggerFactory.getLogger(LSFOutputParser.class);

    /**
     * Maps LSF status codes to JobState enum values.
     * LSF status codes: PEND, RUN, DONE, EXIT, SUSP, USUSP, SSUSP, PSUSP, WAIT, ZOMBI
     */
    private JobState mapLsfStatusToJobState(String lsfStatus) {
        if (lsfStatus == null) {
            return JobState.UNKNOWN;
        }
        return switch (lsfStatus.toUpperCase()) {
            case "PEND", "WAIT" -> JobState.QUEUED;
            case "RUN" -> JobState.ACTIVE;
            case "DONE" -> JobState.COMPLETE;
            case "EXIT" -> JobState.FAILED;
            case "SUSP", "USUSP", "SSUSP", "PSUSP" -> JobState.SUSPENDED;
            case "ZOMBI" -> JobState.UNKNOWN;
            default -> {
                logger.warn("Unknown LSF status code: {}", lsfStatus);
                yield JobState.UNKNOWN;
            }
        };
    }

    @Override
    public String parseJobSubmission(String rawOutput) throws Exception {
        logger.debug(rawOutput);
        if (rawOutput.indexOf("<") >= 0) {
            return rawOutput.substring(rawOutput.indexOf("<") + 1, rawOutput.indexOf(">"));
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
        logger.debug("Parsing job status for jobID {}: {}", jobID, rawOutput);
        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            var jobStatus = new JobStatus();
            jobStatus.setJobState(JobState.UNKNOWN);
            return jobStatus;
        }

        // Parse LSF bjobs output format - find the line containing this jobID
        // Format is typically: JOBID USER STAT QUEUE FROM_HOST EXEC_HOST JOB_NAME SUBMIT_TIME
        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            String[] lineParts = line.split(" ");
            // Filter out empty strings
            List<String> columnList =
                    java.util.Arrays.stream(lineParts).filter(s -> !s.isEmpty()).toList();

            // Check if this line contains our jobID (first column)
            if (columnList.size() > 2 && columnList.get(0).equals(jobID)) {
                try {
                    var jobStatus = new JobStatus();
                    // Status is typically in column 2 (index 2)
                    jobStatus.setJobState(mapLsfStatusToJobState(columnList.get(2)));
                    logger.info("Parsed job status for jobID {}: {}", jobID, jobStatus.getJobState());
                    return jobStatus;
                } catch (IndexOutOfBoundsException e) {
                    logger.warn("Invalid LSF output format for jobID {}", jobID);
                }
            }
        }

        // Job not found in output
        logger.warn("JobID {} not found in LSF bjobs output", jobID);
        var jobStatus = new JobStatus();
        jobStatus.setJobState(JobState.UNKNOWN);
        return jobStatus;
    }

    @Override
    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws Exception {
        logger.debug(rawOutput);

        String[] info = rawOutput.split("\n");
        for (String jobID : statusMap.keySet()) {
            String jobName = jobID.split(",")[1];
            boolean found = false;
            for (int i = 0; i < info.length; i++) {
                if (info[i].contains(jobName.substring(0, 8))) {
                    // now starts processing this line
                    logger.info(info[i]);
                    String correctLine = info[i];
                    List<String> columnList = java.util.Arrays.stream(correctLine.split(" "))
                            .filter(s -> !s.isEmpty())
                            .toList();
                    try {
                        var jobStatus = new JobStatus();
                        jobStatus.setJobState(mapLsfStatusToJobState(columnList.get(2)));
                        statusMap.put(jobID, jobStatus);
                    } catch (IndexOutOfBoundsException e) {
                        var jobStatus = new JobStatus();
                        jobStatus.setJobState(JobState.UNKNOWN);
                        statusMap.put(jobID, jobStatus);
                    }
                    found = true;
                    break;
                }
            }
            if (!found)
                logger.error("Couldn't find the status of the Job with JobName: " + jobName + "Job Id: "
                        + jobID.split(",")[0]);
        }
    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        String regJobId = "jobId";
        Pattern pattern = Pattern.compile(
                "(?=(?<" + regJobId + ">\\d+)\\s+\\w+\\s+" + jobName + ")"); // regex - look ahead and match
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
}
