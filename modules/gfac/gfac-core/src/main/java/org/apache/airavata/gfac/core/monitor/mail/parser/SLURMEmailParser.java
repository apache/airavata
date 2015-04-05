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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SLURMEmailParser implements EmailParser {

    private static final Logger log = LoggerFactory.getLogger(SLURMEmailParser.class);

    private static final String JOBID = "jobId";
    private static final String STATUS = "status";
    private static final String REGEX = "[A-Z]*\\s[a-zA-Z]*_[a-z]*=(?<" + JOBID
            + ">\\d*)\\s[a-zA-Z]*=[a-zA-Z0-9-]*\\s(?<" + STATUS + ">[]a-zA-Z]*),.*";

    @Override
    public JobStatusResult parseEmail(Message message) throws MessagingException, AiravataException{
        String subject = message.getSubject();
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(subject);
        JobStatusResult jobStatusResult = new JobStatusResult();
        if (matcher.find()) {
            jobStatusResult.setJobId(matcher.group(JOBID));
            jobStatusResult.setState(getJobState(matcher.group(STATUS)));
            // TODO remove following test lines
            String jobId = matcher.group(JOBID);
            String status = matcher.group(STATUS);
            log.info("SLURM " + status + " message received -> " + jobId);
            return jobStatusResult;
        } else {
            log.error("No matched found for subject -> " + subject);
        }
        return null;
    }

    private JobState getJobState(String state) {
        switch (state) {
            case "Began":
                return JobState.QUEUED;
            case "Ended":
                return JobState.COMPLETE;
            case "Failed":
                return JobState.FAILED;
            default:
                log.error("Job State " + state + " isn't handle by SLURM parser");
                return JobState.UNKNOWN;

        }
    }



/*    -----------------------
    This is the message envelope
    ---------------------------
    FROM: slurm@batch1.stampede.tacc.utexas.edu
    TO: shameera@scigap.org
    SUBJECT: SLURM Job_id=5055468 Name=A433255759 Began, Queued time 00:00:01
            ----------------------------
    CONTENT-TYPE: TEXT/PLAIN; charset=us-ascii
    This is plain text
    ---------------------------*/

/*    -----------------------
    This is the message envelope
    ---------------------------
    FROM: slurm@batch1.stampede.tacc.utexas.edu
    TO: shameera@scigap.org
    SUBJECT: SLURM Job_id=5055468 Name=A433255759 Ended, Run time 00:02:40
            ----------------------------
    CONTENT-TYPE: TEXT/PLAIN; charset=us-ascii
    This is plain text
    ---------------------------*/
}
