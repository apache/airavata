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
package org.apache.airavata.job.monitor.core;

import org.apache.airavata.job.monitor.HostMonitorData;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.UserMonitorData;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.state.JobStatus;

/**
 * This is an interface to implement messageparser, it could be
 * pull based or push based still monitor has to parse the content of
 * the message it gets from remote monitoring system and finalize
 * them to internal job state, Ex: JSON parser for AMQP and Qstat reader
 * for pull based monitor.
 */
public interface MessageParser {
    /**
     * This method is to implement how to parse the incoming message
     * and implement a logic to finalize the status of the job,
     * we have to makesure the correct message is given to the messageparser
     * parse method, it will not do any filtering
     * @param message content of the message
     * @param hostMonitorData monitorID object
     * @return
     */
    JobStatus parseMessage(String message,HostMonitorData hostMonitorData)throws AiravataMonitorException;
}
