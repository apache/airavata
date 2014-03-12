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
package org.apache.airavata.job.monitor.impl.push.amqp;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.airavata.ComputingActivity;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.core.MessageParser;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class JSONMessageParser implements MessageParser {
    private final static Logger logger = LoggerFactory.getLogger(JSONMessageParser.class);

    public JobStatus parseMessage(String message, MonitorID monitorID)throws AiravataMonitorException{
        /*todo write a json message parser here*/
        logger.info("Mesage parse invoked");
        System.out.println(message);
//        JSONParser parser = new JSONParser();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
        try {
            ComputingActivity computingActivity = objectMapper.readValue(message.getBytes(), ComputingActivity.class);
            logger.info(computingActivity.getIDFromEndpoint());
            List<String> stateList = computingActivity.getState();
            for (String aState : stateList) {
                logger.info(aState);
            }
        } catch (IOException e) {
            throw new AiravataMonitorException(e);
        }
        return new JobStatus();
    }
}
