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
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.registry.api.RegistryService;

import javax.mail.Message;
import javax.mail.MessagingException;

public interface EmailParser {
    static final String STATUS = "status";
    static final String JOBID = "jobId";
    static final String JOBNAME = "jobName";
    static final String EXIT_STATUS = "exitStatus";

    JobStatusResult parseEmail(Message message, RegistryService.Client registryClient) throws MessagingException, AiravataException;
}
