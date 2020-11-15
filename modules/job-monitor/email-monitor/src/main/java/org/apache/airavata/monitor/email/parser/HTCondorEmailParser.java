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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTCondorEmailParser implements EmailParser {
    private static final Logger log = LoggerFactory.getLogger(HTCondorEmailParser.class);

    public JobStatusResult parseEmail(Message message) throws MessagingException, AiravataException{
        // TODO
        return null;
    }

    private void parseSubject(String subject, JobStatusResult jobStatusResult) throws MessagingException {
        // TODO
    }

    private JobState getJobState(String state, String subject) {
        // TODO
        return null;
    }
}
