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

import org.apache.airavata.gsi.ssh.impl.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SGEOutputParser implements OutputParser{
    private static final Logger log = LoggerFactory.getLogger(PBSOutputParser.class);

    public void parse(JobDescriptor jobDescriptor, String rawOutput) {
        log.debug(rawOutput);
        String[] info = rawOutput.split("\n");
        String[] line;
        for (int i = 0; i < info.length; i++) {
            if (info[i].contains("=")) {
                line = info[i].split("=", 2);
            } else {
                line = info[i].split(":", 2);
            }
            if (line.length >= 2) {
                String header = line[0].trim();
                log.debug("Header = " + header);
                String value = line[1].trim();
                log.debug("value = " + value);

                if (header.equals("Variable_List")) {
                    while (info[i + 1].startsWith("\t")) {
                        value += info[i + 1];
                        i++;
                    }
                    value = value.replaceAll("\t", "");
                    jobDescriptor.setVariableList(value);
                } else if ("Job Id".equals(header)) {
                    jobDescriptor.setJobID(value);
                } else if ("Job_Name".equals(header)) {
                    jobDescriptor.setJobName(value);
                } else if ("Account_Name".equals(header)) {
                    jobDescriptor.setAcountString(value);
                } else if ("job_state".equals(header)) {
                    jobDescriptor.setStatus(value);
                } else if ("Job_Owner".equals(header)) {
                    jobDescriptor.setOwner(value);
                } else if ("resources_used.cput".equals(header)) {
                    jobDescriptor.setUsedCPUTime(value);
                } else if ("resources_used.mem".equals(header)) {
                    jobDescriptor.setUsedMemory(value);
                } else if ("resources_used.walltime".equals(header)) {
                    jobDescriptor.setEllapsedTime(value);
                } else if ("job_state".equals(header)) {
                    jobDescriptor.setStatus(value);
                } else if ("queue".equals(header))
                    jobDescriptor.setQueueName(value);
                else if ("ctime".equals(header)) {
                    jobDescriptor.setCTime(value);
                } else if ("qtime".equals(header)) {
                    jobDescriptor.setQTime(value);
                } else if ("mtime".equals(header)) {
                    jobDescriptor.setMTime(value);
                } else if ("start_time".equals(header)) {
                    jobDescriptor.setSTime(value);
                } else if ("comp_time".equals(header)) {
                    jobDescriptor.setCompTime(value);
                } else if ("exec_host".equals(header)) {
                    jobDescriptor.setExecuteNode(value);
                } else if ("Output_Path".equals(header)) {
                    if (info[i + 1].contains("=") || info[i + 1].contains(":"))
                        jobDescriptor.setStandardOutFile(value);
                    else {
                        jobDescriptor.setStandardOutFile(value + info[i + 1].trim());
                        i++;
                    }
                } else if ("Error_Path".equals(header)) {
                    if (info[i + 1].contains("=") || info[i + 1].contains(":"))
                        jobDescriptor.setStandardErrorFile(value);
                    else {
                        String st = info[i + 1].trim();
                        jobDescriptor.setStandardErrorFile(value + st);
                        i++;
                    }

                } else if ("submit_args".equals(header)) {
                    while (i + 1 < info.length) {
                        if (info[i + 1].startsWith("\t")) {
                            value += info[i + 1];
                            i++;
                        } else
                            break;
                    }
                    value = value.replaceAll("\t", "");
                    jobDescriptor.setSubmitArgs(value);
                }
            }
        }
    }

	public String parse(String rawOutput) {
		log.debug(rawOutput);
		if (rawOutput != null && !rawOutput.isEmpty()) {
			String[] info = rawOutput.split("\n");
			String lastLine = info[info.length - 1];
			return lastLine.split(" ")[2]; // In PBS stdout is going to be directly the jobID
		} else {
			return "";
		}
	}

    public JobStatus parse(String jobID, String rawOutput) {
       // not implemented to sun grid engine
        return JobStatus.U;
    }

    public void parse(String userName, Map<String, JobStatus> statusMap, String rawOutput) {
        log.debug(rawOutput);
        String[] info = rawOutput.split("\n");
        int lastStop = 0;
        for (String jobID : statusMap.keySet()) {
            for(int i=lastStop;i<info.length;i++){
               if(jobID.split(",")[0].contains(info[i].split(" ")[0]) && !"".equals(info[i].split(" ")[0])){
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
                   lastStop = i+1;
                   if ("E".equals(columnList.get(4))) {
                       // There is another status with the same letter E other than error status
                       // to avoid that we make a small tweek to the job status
                       columnList.set(4, "Er");
                   }
                   statusMap.put(jobID, JobStatus.valueOf(columnList.get(4)));
                   break;
               }
            }
        }
    }


}
