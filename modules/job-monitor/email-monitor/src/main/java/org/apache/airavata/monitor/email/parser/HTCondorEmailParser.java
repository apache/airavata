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
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTCondorEmailParser implements EmailParser {
    private static final Logger log = LoggerFactory.getLogger(HTCondorEmailParser.class);

    // Group matterns to match against
    private static final String JOBID = "jobid";
    private static final String STATUS = "status";

    // Regex used to match desired information
    private static final String JOBID_REGEX = "(?<" + JOBID + ">\\d+)\\.\\d+";  // Regex pattern to match a Job ID from an HTCondor email
    private static final String CONTENTS_REGEX = "\\s*(?=exited\\s+\\S+\\s+with status\\s+(?<" + STATUS + ">-?\\d+\\.?\\d*))"; // Regex pattern to match a Job Status

    // Regex Patterns
    private static final Pattern jobIdPattern = Pattern.compile(JOBID_REGEX);
    private static final Pattern statusPattern = Pattern.compile(CONTENTS_REGEX);


    /*
     * Name    : JobStatusResult
     * Params  : Message message : The email message that was received
     * Returns : JobStatusResult
     * Purpose : Responsible for parsing the email to access an HTCondor job status
     */
    public JobStatusResult parseEmail(Message message, RegistryService.Client registryClient) throws MessagingException, AiravataException{
        // Job Status Results
        JobStatusResult jobStatusResult = new JobStatusResult();

        try {
            // Parse the Subject Line to get the job ID
            parseSubject(message.getSubject(), jobStatusResult);

            // Parse the email contents to get the job state
            parseJobState((String) message.getContent(), jobStatusResult);

            String processId = fetchProcessId((String)message.getContent());
            List<JobModel> jobs = registryClient.getJobs("processId", processId);
            Optional<JobModel> firstJob = jobs.stream().filter(job -> job.getJobId().equals(jobStatusResult.getJobId())).findFirst();
            if (firstJob.isPresent()) {
                jobStatusResult.setJobName(firstJob.get().getJobName());
            } else {
                throw new Exception("No job found matching job id " + jobStatusResult.getJobId() + " for HTCondor");
            }

        } catch (Exception e) {
            throw new AiravataException("[EJM]: There was an error while parsing the content of the HTCondor email -> " + e);
        }

        return jobStatusResult;
    }


    /*
     * Name    : parseSubject
     * Params  : String subject : The email's subject line
     *           JobStatusResult jobStatusResult : The JobStatusResult to fill out
     * Returns : None
     * Purpose : To parse the HTCondor email subject line for the job ID
     */
    private void parseSubject(String subject, JobStatusResult jobStatusResult) {
        // Create a new Matcher object to use for parsing the subject line
        Matcher matcher = jobIdPattern.matcher(subject);

        // Parse the job ID if the Job ID is available in the subject line
        if (matcher.find()) {
            jobStatusResult.setJobId(matcher.group(JOBID));
        } else {
            log.error("[EJM]: The Job ID was not found in the HTCondor email subject -> " + subject);
        }
    }


    /*
     * Name    : parseJobState
     * Params  : String content : The email's message content
     *           JobStatusResult jobStatusResult : The JobStatusResult to fill out
     * Returns : None
     * Purpose : To parse the HTCondor email for the job status.
     *           [NOTE] Due to the limited information available in the HTCondor status emails, the only
     *                  statuses that may be parsed are FAILURE and COMPLETE
     */

    private String fetchProcessId(String content) {
        int start = content.indexOf("submitted from directory");
        int end = content.indexOf("\n", start);
        String path = content.substring(start + "submitted from directory".length() + 1, end-1);
        int pathSeperatorIndex = path.lastIndexOf("/");
        String processId = path.substring(pathSeperatorIndex + 1);
        return processId;
    }

    private void parseJobState(String content, JobStatusResult jobStatusResult) {
        // Split message content into an array of lines
//        String[] messageArray = content.split("\n");

        // Access the line of the email with the status result
//        String statusLine = messageArray[5];

        // Match the job status in the status line
        Matcher matcher = statusPattern.matcher(content);

        // Determine the state that the job is in
        if(matcher.find()) {
            String status = matcher.group(STATUS);

            if (status.equals("0")) {
                jobStatusResult.setState(JobState.COMPLETE);
            }else if (!status.isEmpty()) {
                jobStatusResult.setState(JobState.FAILED);
            } else {
                log.error("[EJM] An unknown job status result was found in the content of the HTCondor email. Status found: " + status);
            }
        }else{
            log.error("[EJM]: The Job Status was not found in the content of the HTCondor email.");
        }
    }
}