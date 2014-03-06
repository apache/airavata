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

import java.util.Map;

public class SlurmOutputParser implements OutputParser {
    public void parse(JobDescriptor descriptor, String rawOutput) {
        System.out.println(rawOutput);
        String[] info = rawOutput.split("\n");
        String lastString = info[info.length -1];
        if (lastString.contains("JOB ID")) {
            // because there's no state
            descriptor.setStatus("E");
        }else{
            int column = 0;
            System.out.println(lastString);
            for(String each:lastString.split(" ")){
                if(each.trim().isEmpty()){
                    continue;
                }else{
                    switch (column){
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
    public String parse(String rawOutput) {
        String[] info = rawOutput.split("\n");
        for (String anInfo : info) {
            if (anInfo.contains("Submitted batch job")) {
                String[] split = anInfo.split("Submitted batch job");
                return split[1].trim();
            }
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JobStatus parse(String jobID, String rawOutput) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void parse(Map<String, JobStatus> statusMap, String rawOutput) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
