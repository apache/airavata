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
import org.apache.airavata.helix.impl.task.submission.config.app.JobUtil;
import org.apache.airavata.model.status.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PBSOutputParser implements OutputParser {
    private static final Logger log = LoggerFactory.getLogger(PBSOutputParser.class);

    public String parseJobSubmission(String rawOutput) {
        log.debug(rawOutput);
        String jobId = rawOutput;
        if (!rawOutput.isEmpty() && rawOutput.contains("\n")){
            String[] split = rawOutput.split("\n");
            if (split.length != 0){
                jobId = split[0];
            }
        }
        return jobId;  //In PBS stdout is going to be directly the jobID
    }

    @Override
    public boolean isJobSubmissionFailed(String rawOutput) {
        return false;
    }

    public JobStatus parseJobStatus(String jobID, String rawOutput) {
        boolean jobFount = false;
        log.debug(rawOutput);
        String[] info = rawOutput.split("\n");
        String[] line = null;
        int index = 0;
        for (String anInfo : info) {
            index++;
            if (anInfo.contains("Job Id:")) {
                if (anInfo.contains(jobID)) {
                    jobFount = true;
                    break;
                }
            }
        }
        if (jobFount) {
            for (int i=index;i<info.length;i++) {
                String anInfo = info[i];
                if (anInfo.contains("=")) {
                    line = anInfo.split("=", 2);
                    if (line.length != 0) {
                        if (line[0].contains("job_state")) {
	                        return new JobStatus(JobUtil.getJobState(line[1].replaceAll(" ", "")));
                        }
                    }
                }
            }
        }
        return null;
    }

    public void parseJobStatuses(String userName, Map<String, JobStatus> statusMap, String rawOutput) {
        log.debug(rawOutput);
        String[]    info = rawOutput.split("\n");
//        int lastStop = 0;
        for (String jobID : statusMap.keySet()) {
            String jobName = jobID.split(",")[1];
            boolean found = false;
            for (int i = 0; i < info.length; i++) {
                if (info[i].contains(jobName.substring(0,8))) {
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
//                    lastStop = i + 1;
                    try {
	                    statusMap.put(jobID, new JobStatus(JobUtil.getJobState(columnList.get(9))));
                    }catch(IndexOutOfBoundsException e) {
	                    statusMap.put(jobID, new JobStatus(JobUtil.getJobState("U")));
                    }
                    found = true;
                    break;
                }
            }
            if(!found)
            log.error("Couldn't find the status of the Job with JobName: " + jobName + "Job Id: " + jobID.split(",")[0]);
        }
    }

    @Override
    public String parseJobId(String jobName, String rawOutput) throws Exception {
        /* output will look like
        Job Id: 2080802.gordon-fe2.local
            Job_Name = A312402627
        */
        String regJobId = "jobId";
        Pattern pattern = Pattern.compile("(?<" + regJobId + ">[^\\s]*)\\s*.* " + jobName);
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
