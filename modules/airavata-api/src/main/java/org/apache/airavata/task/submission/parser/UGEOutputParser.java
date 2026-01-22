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

public class UGEOutputParser implements OutputParser {
    private static final Logger log = LoggerFactory.getLogger(UGEOutputParser.class);
    public static final String JOB_ID = "jobId";

    public String parseJobSubmission(String rawOutput) {
        log.debug(rawOutput);
        if (rawOutput != null && !rawOutput.isEmpty() && !isJobSubmissionFailed(rawOutput)) {
            String[] info = rawOutput.split("\n");
            String lastLine = info[info.length - 1];
            return lastLine.split(" ")[2]; // In PBS stdout is going to be directly the jobID
        } else {
            return "";
        }
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        Pattern pattern = Pattern.compile("Rejecting");
        Matcher matcher = pattern.matcher(rawOutput);
        return matcher.find();
    }

    public JobStatus parseJobStatus(String jobID, String rawOutput) {
        log.debug("Parsing job status for jobID {}: {}", jobID, rawOutput);
        if (rawOutput == null || rawOutput.trim().isEmpty()) {
            var jobStatus = new JobStatus();
            jobStatus.setJobState(JobState.UNKNOWN);
            return jobStatus;
        }

        // Parse qstat output format - find the line containing this jobID
        // Format is typically: job_number state priority name owner ...
        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            String[] lineParts = line.split(" ");
            // Filter out empty strings
            List<String> columnList = new java.util.ArrayList<>(
                    java.util.Arrays.stream(lineParts).filter(s -> !s.isEmpty()).toList());

            // Check if this line contains our jobID
            // JobID might be in column 0 or we need to match "job_number: <jobID>" pattern
            if (columnList.size() > 0) {
                String firstColumn = columnList.get(0);
                // Check for "job_number: <jobID>" pattern
                if (firstColumn.contains("job_number:")) {
                    String jobNumberPart =
                            firstColumn.substring(firstColumn.indexOf(":") + 1).trim();
                    if (jobNumberPart.equals(jobID)) {
                        // Found the job, extract status from column 4 (similar to parseJobStatuses)
                        if (columnList.size() > 4) {
                            String status = columnList.get(4);
                            // Handle special case: "E" might conflict, map to "Er"
                            if ("E".equals(status)) {
                                status = "Er";
                            }
                            try {
                                var jobStatus = new JobStatus();
                                jobStatus.setJobState(JobState.valueOf(status));
                                log.info("Parsed job status for jobID {}: {}", jobID, jobStatus.getJobState());
                                return jobStatus;
                            } catch (IllegalArgumentException e) {
                                log.warn("Unknown UGE status: {} for jobID {}", status, jobID);
                            }
                        }
                    }
                } else if (firstColumn.equals(jobID)) {
                    // JobID is in first column directly
                    if (columnList.size() > 4) {
                        String status = columnList.get(4);
                        if ("E".equals(status)) {
                            status = "Er";
                        }
                        try {
                            var jobStatus = new JobStatus();
                            jobStatus.setJobState(JobState.valueOf(status));
                            log.info("Parsed job status for jobID {}: {}", jobID, jobStatus.getJobState());
                            return jobStatus;
                        } catch (IllegalArgumentException e) {
                            log.warn("Unknown UGE status: {} for jobID {}", status, jobID);
                        }
                    }
                }
            }
        }

        // Job not found in output
        log.warn("JobID {} not found in qstat output", jobID);
        var jobStatus = new JobStatus();
        jobStatus.setJobState(JobState.UNKNOWN);
        return jobStatus;
    }

    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) {
        log.debug(rawOutput);
        String[] info = rawOutput.split("\n");
        int lastStop = 0;
        for (String jobID : statusMap.keySet()) {
            for (int i = lastStop; i < info.length; i++) {
                String[] lineParts = info[i].split(" ");
                if (jobID.split(",")[0].contains(lineParts[0]) && !lineParts[0].isEmpty()) {
                    // now starts processing this line
                    log.info(info[i]);
                    List<String> columnList = new java.util.ArrayList<>(java.util.Arrays.stream(lineParts)
                            .filter(s -> !s.isEmpty())
                            .toList());
                    lastStop = i + 1;
                    if ("E".equals(columnList.get(4))) {
                        // There is another status with the same letter E other than error status
                        // to avoid that we make a small tweek to the job status
                        columnList.set(4, "Er");
                    }
                    var jobStatus = new JobStatus();
                    jobStatus.setJobState(JobState.valueOf(columnList.get(4)));
                    statusMap.put(jobID, jobStatus);
                    break;
                }
            }
        }
    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        if (jobName.length() > 10) {
            jobName = jobName.substring(0, 10);
        }
        Pattern pattern = Pattern.compile("(?<" + JOB_ID + ">\\S+)\\s+\\S+\\s+(" + jobName + ")");
        Matcher matcher = pattern.matcher(rawOutput);
        if (matcher.find()) {
            return matcher.group(JOB_ID);
        }
        return null;
    }
}
