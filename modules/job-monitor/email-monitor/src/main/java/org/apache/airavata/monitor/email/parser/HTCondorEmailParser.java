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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTCondorEmailParser implements EmailParser {
    private static final Logger log = LoggerFactory.getLogger(HTCondorEmailParser.class);

    private static final String JOBID_REGEX = "(\\d+(?:\\.\\d+)?)";  // Regex pattern to match a Job ID from an HTCondor email
    private static final String CONTENTS_REGEX = "(\\d+)";           // Regex pattern to match a Job Status

    // Regex Patterns
    private static final Pattern jobIdPattern = Pattern.compile(JOBID_REGEX);
    private static final Pattern statusPattern = Pattern.compile(CONTENTS_REGEX);


    public JobStatusResult parseEmail(Message message) throws MessagingException, AiravataException{
        // Job Status Results
        JobStatusResult jobStatusResult = new JobStatusResult();

        try {
            parseSubject(message.getSubject(), jobStatusResult);
            parseJobState((String) message.getContent(), jobStatusResult);
        } catch (IOException e) {
            throw new AiravataException("[EJM]: There was an error while parsing the content of the HTCondor email -> " + e);
        }

        return jobStatusResult;
    }


    private void parseSubject(String subject, JobStatusResult jobStatusResult) {
        Matcher matcher = jobIdPattern.matcher(subject);

        if (matcher.find()) {
            jobStatusResult.setJobId(matcher.group());
            jobStatusResult.setJobName(matcher.group());
        } else {
            log.error("[EJM]: The Job ID was not found in the HTCondor email subject -> " + subject);
        }
    }


    private void parseJobState(String content, JobStatusResult jobStatusResult) {
        // Split message content into an array of lines
        String[] messageArray = content.split("\n");

        // Access the line of the email with the status result
        String statusLine = messageArray[5];

        // Match the job status in the status line
        Matcher matcher = statusPattern.matcher(statusLine);

        // Determine the state that the job is in
        if(matcher.find()) {
           String status = matcher.group();

           if (status == "0") {
               jobStatusResult.setState(JobState.COMPLETE);
           }else {
               jobStatusResult.setState(JobState.FAILED);
           }
        }else{
            log.error("[EJM]: The Job Status was not found in the content of the HTCondor email.");
        }
    }
}
