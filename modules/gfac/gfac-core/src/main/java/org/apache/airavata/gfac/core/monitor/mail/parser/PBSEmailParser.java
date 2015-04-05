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
package org.apache.airavata.gfac.core.monitor.mail.parser;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.gfac.core.monitor.mail.JobStatusResult;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PBSEmailParser implements EmailParser {

    private static final Logger log = LoggerFactory.getLogger(PBSEmailParser.class);

    private static final String STATUS = "status";
    private static final String JOBID = "jobId";
    private static final String REGEX = "[a-zA-Z: ]*(?<" + JOBID + ">[a-zA-Z0-9-\\.]*)\\s+.*\\s+.*\\s+(?<"
            + STATUS + ">[a-zA-Z\\ ]*)";

    @Override
    public JobStatusResult parseEmail(Message message) throws MessagingException, AiravataException {
        try {
            String content = ((String) message.getContent());
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(content);
            JobStatusResult jobStatusResult = new JobStatusResult();
            if (matcher.find()) {
                jobStatusResult.setJobId(matcher.group(JOBID));
                String statusLine = matcher.group(STATUS);
                jobStatusResult.setState(getJobState(statusLine));
                return jobStatusResult;
            } else {
                log.error("No matched found for content => \n" + content);
            }

        } catch (IOException e) {
            throw new AiravataException("Error while reading content of the email message");
        }
        return null;
    }

    private JobState getJobState(String statusLine) {
        switch (statusLine) {
            case "Begun execution":
                return JobState.QUEUED;
            case "Execution terminated":
                return JobState.COMPLETE;
            default:
                return JobState.UNKNOWN;
        }
    }



/*    -----------------------
    This is the message envelope
    ---------------------------
    FROM: pbsconsult@sdsc.edu
    TO: shameera@scigap.org
    SUBJECT: PBS JOB 2556782.trestles-fe1.local
    ----------------------------
    CONTENT-TYPE: TEXT/PLAIN
    This is plain text
    ---------------------------
    PBS Job Id: 2556782.trestles-fe1.local
    Job Name:   A1182004055
    Exec host:  trestles-1-12/0+trestles-1-12/1+trestles-1-12/2+trestles-1-12/3
    Begun execution
    */
/*
    -----------------------
    This is the message envelope
    ---------------------------
    FROM: pbsconsult@sdsc.edu
    TO: shameera@scigap.org
    SUBJECT: PBS JOB 2556782.trestles-fe1.local
    ----------------------------
    CONTENT-TYPE: TEXT/PLAIN
    This is plain text
    ---------------------------
    PBS Job Id: 2556782.trestles-fe1.local
    Job Name:   A1182004055
    Exec host:  trestles-1-12/0+trestles-1-12/1+trestles-1-12/2+trestles-1-12/3
    Execution terminated
    Exit_status=0
    resources_used.cput=00:14:31
    resources_used.mem=124712kb
    resources_used.vmem=3504116kb
    resources_used.walltime=00:04:10
    Error_Path: trestles-login2.sdsc.edu:/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs/MonitorTest_9169517d-e2d9-4ff5-bed5-dee6eb3eebb2/Amber_Sander.stderr
    Output_Path: trestles-login2.sdsc.edu:/oasis/scratch/trestles/ogce/temp_project/gta-work-dirs/MonitorTest_9169517d-e2d9-4ff5-bed5-dee6eb3eebb2/Amber_Sander.stdout
    */


}
