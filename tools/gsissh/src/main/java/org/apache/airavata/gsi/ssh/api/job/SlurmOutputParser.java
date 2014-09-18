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
 *
*/
package org.apache.airavata.gsi.ssh.api.job;

import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.impl.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.JobState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlurmOutputParser implements OutputParser {
    private static final Logger log = LoggerFactory.getLogger(PBSOutputParser.class);

    public void parse(JobDescriptor descriptor, String rawOutput) throws SSHApiException {
        log.info(rawOutput);
        String[] info = rawOutput.split("\n");
        String lastString = info[info.length - 1];
        if (lastString.contains("JOB ID")) {
            // because there's no state
            descriptor.setStatus("U");
        } else {
            int column = 0;
            System.out.println(lastString);
            for (String each : lastString.split(" ")) {
                if (each.trim().isEmpty()) {
                    continue;
                } else {
                    switch (column) {
                        case 0:
                            descriptor.setJobID(each);
                            column++;
                            break;
                        case 1:
                            descriptor.setPartition(each);
                            column++;
                            break;
                        case 2:
                            descriptor.setJobName(each);
                            column++;
                            break;
                        case 3:
                            descriptor.setUserName(each);
                            column++;
                            break;
                        case 4:
                            descriptor.setStatus(each);
                            column++;
                            break;
                        case 5:
                            descriptor.setUsedCPUTime(each);
                            column++;
                            break;
                        case 6:
                            descriptor.setNodes(Integer.parseInt(each));
                            column++;
                            break;
                        case 7:
                            descriptor.setNodeList(each);
                            column++;
                            break;
                    }
                }
            }
        }

    }

    /**
     * This can be used to parse the outpu of sbatch and extrac the jobID from the content
     *
     * @param rawOutput
     * @return
     */
    public String parse(String rawOutput) throws SSHApiException {
        log.info(rawOutput);
        String[] info = rawOutput.split("\n");
        for (String anInfo : info) {
            if (anInfo.contains("Submitted batch job")) {
                String[] split = anInfo.split("Submitted batch job");
                return split[1].trim();
            }
        }
        return "";
//        throw new SSHApiException(rawOutput);  //todo//To change body of implemented methods use File | Settings | File Templates.
    }

    public JobStatus parse(String jobID, String rawOutput) throws SSHApiException {
        log.info(rawOutput);
        String[] info = rawOutput.split("\n");
        String lastString = info[info.length - 1];
        if (lastString.contains("JOBID") || lastString.contains("PARTITION")) {
            // because there's no state
            return JobStatus.valueOf("U");
        } else {
            int column = 0;
            for (String each : lastString.split(" ")) {
                if (!each.trim().isEmpty()) {
                    switch (column) {
                        case 0:
                            column++;
                            break;
                        case 1:
                            column++;
                            break;
                        case 2:
                            column++;
                            break;
                        case 3:
                            column++;
                            break;
                        case 4:
                            return JobStatus.valueOf((each));
                        case 5:
                            column++;
                            break;
                        case 6:
                            column++;
                            break;
                        case 7:
                            column++;
                            break;
                    }
                }
            }
        }
        return JobStatus.valueOf("U");
    }

    public void parse(String userName, Map<String, JobStatus> statusMap, String rawOutput) throws SSHApiException {
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
                        statusMap.put(jobID, JobStatus.valueOf(columnList.get(4)));
                    } catch (IndexOutOfBoundsException e) {
                        statusMap.put(jobID, JobStatus.valueOf("U"));
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
}
