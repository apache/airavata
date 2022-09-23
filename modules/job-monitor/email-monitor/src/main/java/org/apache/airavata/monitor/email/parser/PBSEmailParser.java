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

public class PBSEmailParser implements EmailParser {
    private static final Logger log = LoggerFactory.getLogger(PBSEmailParser.class);
    public static final String BEGUN_EXECUTION = "Begun execution";
    public static final String EXECUTION_TERMINATED = "Execution terminated";
    public static final String ABORTED_BY_PBS_SERVER = "Aborted by PBS Server";

    static final String REGEX = "[a-zA-Z ]*:[ ]*(?<" + JOBID + ">[a-zA-Z0-9-_\\.]*)\\s+[a-zA-Z ]*:[ ]*(?<" +
            JOBNAME + ">[a-zA-Z0-9-\\.]*)\\s[\\S|\\s]*(?<" + STATUS + ">" + BEGUN_EXECUTION + "|" +
            EXECUTION_TERMINATED + "|" + ABORTED_BY_PBS_SERVER + ")";

    private static final String REGEX_EXIT_STATUS = "Exit_status=(?<" + EXIT_STATUS + ">[\\d]+)";

    @Override
    public JobStatusResult parseEmail(Message message, RegistryService.Client registryClient) throws MessagingException, AiravataException {
        JobStatusResult jobStatusResult = new JobStatusResult();
//        log.info("Parsing -> " + message.getSubject());
        try {
            String content = ((String) message.getContent());
            parseContent(content, jobStatusResult);
        } catch (Exception e) {
            throw new AiravataException("[EJM]: Error while reading content of the email message");
        }
        return jobStatusResult;
    }

    void parseContent(String content, JobStatusResult jobStatusResult) throws MessagingException, AiravataException {
        content = content.replaceAll("[^\\x00-\\x7F]", "");
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            jobStatusResult.setJobId(matcher.group(JOBID));
            jobStatusResult.setJobName(matcher.group(JOBNAME));
            String statusLine = matcher.group(STATUS);
            jobStatusResult.setState(getJobState(statusLine, content));
        } else {
            log.error("[EJM]: No matched found for content => \n" + content);
        }
    }

    private JobState getJobState(String statusLine, String content) {
        switch (statusLine) {
            case BEGUN_EXECUTION:
                return JobState.ACTIVE;
            case EXECUTION_TERMINATED:
                int exitStatus = getExitStatus(content);
                if (exitStatus == 0) {
                    // TODO - Remove rabbitmq client script line from the script.
                    return JobState.COMPLETE;
                } else if (exitStatus == 271) {
                    return JobState.CANCELED;
                } else {
                    return JobState.FAILED;
                }
            case ABORTED_BY_PBS_SERVER:
                return JobState.FAILED;
            default:
                return JobState.UNKNOWN;
        }
    }

    private int getExitStatus(String content) {
        Pattern pattern = Pattern.compile(REGEX_EXIT_STATUS);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String group = matcher.group(EXIT_STATUS);
            if (group != null && !group.trim().isEmpty()) {
                return Integer.valueOf(group.trim());
            }
        }
        return -1;
    }

}
