/**
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
package org.apache.airavata.monitor.email.parser;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LSFEmailParser implements EmailParser {
    private static final Logger log = LoggerFactory.getLogger(LSFEmailParser.class);
    private static final String REGEX = "[a-zA-Z]+\\s+(?<" + JOBID + ">[\\d]+):\\s+<(?<" + JOBNAME + ">[a-zA-Z0-9]+)>\\s+(?<" + STATUS + ">[a-zA-Z]+)";
    public static final String STARTED = "started";
    public static final String COMPLETE = "Done";
    public static final String FAILED = "Exited";

    @Override
    public JobStatusResult parseEmail(Message message, RegistryService.Client registryClient) throws MessagingException, AiravataException {
        JobStatusResult jobStatusResult = new JobStatusResult();

        parseContent(message, jobStatusResult);
        return jobStatusResult;
    }

    private void parseContent(Message message, JobStatusResult jobStatusResult) throws MessagingException, AiravataException {
        String subject = message.getSubject();
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(subject);
        try {
            if (matcher.find()) {
                jobStatusResult.setJobId(matcher.group(JOBID));
                jobStatusResult.setJobName(matcher.group(JOBNAME));
                String content = (String) message.getContent();
                jobStatusResult.setState(getJobState(matcher.group(STATUS), content));
            } else {
                log.error("[EJM]: No matched found for subject => \n" + subject);
            }
        } catch (IOException e) {
            throw new AiravataException("[EJM]: Error while reading content of the email message");
        }
    }

    private JobState getJobState(String status, String content) {
        switch (status) {
            case STARTED:
                return JobState.ACTIVE;
            case COMPLETE:
                return JobState.COMPLETE;
            case FAILED:
                return JobState.FAILED;
            default:
                return JobState.UNKNOWN;
        }
    }
}
