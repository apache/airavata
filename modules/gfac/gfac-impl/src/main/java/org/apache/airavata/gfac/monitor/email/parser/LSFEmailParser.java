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
package org.apache.airavata.gfac.monitor.email.parser;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.gfac.core.monitor.EmailParser;
import org.apache.airavata.gfac.core.monitor.JobStatusResult;
import org.apache.airavata.model.status.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LSFEmailParser implements EmailParser {
    private static final Logger log = LoggerFactory.getLogger(LSFEmailParser.class);
    //root@c312-206.ls4.tacc.utexas.edu
    private static final String SIGNAL = "signal";
    private static final String LONESTAR_REGEX = "Job (?<" + JOBID + ">\\d+) \\(.*\\) (?<" + STATUS
            + ">.*)\\s[a-zA-Z =]+(?<" + EXIT_STATUS + ">\\d+)\\sSignal[ ]*=[ ]*(?<" + SIGNAL + ">[a-zA-z]*)";

    @Override
    public JobStatusResult parseEmail(Message message) throws MessagingException, AiravataException {
        JobStatusResult jobStatusResult = new JobStatusResult();
        try {
            String content = ((String) message.getContent());
            Pattern pattern = Pattern.compile(LONESTAR_REGEX);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                jobStatusResult.setJobId(matcher.group(JOBID));
                String status = matcher.group(STATUS);
                jobStatusResult.setState(getJobState(status, content));
                return jobStatusResult;
            } else {
                log.error("[EJM]: No matched found for content => \n" + content);
            }
        } catch (IOException e) {
            throw new AiravataException("i[EJM]: Error while reading content of the email message");
        }
        return jobStatusResult;
    }

    private JobState getJobState(String status, String content) {
        switch (status) {
            case "Aborted":
                return JobState.FAILED;
            case "Success":
                return JobState.COMPLETE;
            default:
                return JobState.UNKNOWN;
        }

    }
}
