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
package org.apache.airavata.gfac.monitor.impl.push.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.ComputingActivity;
import org.apache.airavata.gfac.monitor.core.MessageParser;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class JSONMessageParser implements MessageParser {
    private final static Logger logger = LoggerFactory.getLogger(JSONMessageParser.class);

    public JobState parseMessage(String message)throws AiravataMonitorException {
        /*todo write a json message parser here*/
        logger.debug(message);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ComputingActivity computingActivity = objectMapper.readValue(message.getBytes(), ComputingActivity.class);
            logger.info(computingActivity.getIDFromEndpoint());
            List<String> stateList = computingActivity.getState();
            JobState jobState = null;
            for (String aState : stateList) {
                jobState = getStatusFromString(aState);
            }
            // we get the last value of the state array
            return jobState;
        } catch (IOException e) {
            throw new AiravataMonitorException(e);
        }
    }

private JobState getStatusFromString(String status) {
        logger.info("parsing the job status returned : " + status);
        if(status != null){
            if("ipf:finished".equals(status)){
                return JobState.COMPLETE;
            }else if("ipf:pending".equals(status)|| "ipf:starting".equals(status)){
                return JobState.QUEUED;
            }else if("ipf:running".equals(status) || "ipf:finishing".equals(status)){
                return JobState.ACTIVE;
            }else if ("ipf:held".equals(status) || "ipf:teminating".equals(status) || "ipf:teminated".equals(status)) {
                return JobState.HELD;
            } else if ("ipf:suspending".equals(status)) {
                return JobState.SUSPENDED;
            }else if ("ipf:failed".equals(status)) {
                return JobState.FAILED;
            }else if ("ipf:unknown".equals(status)){
                return JobState.UNKNOWN;
            }
        }
        return JobState.UNKNOWN;
    }

}
