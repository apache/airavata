/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.messaging.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQListener {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);
    private static String gatewayId = "*";
    private static String experimentId = "*";
    private static String jobId = "*";

    public static void parseArguments(String[] args) {
        try {
            if (args == null || args.length == 0) {
                logger.info("You have not specified any options. We assume you need to listen to all the messages...");
                gatewayId = "*";
                return;
            }

            // Simple manual parsing
            java.util.Map<String, String> options = new java.util.HashMap<>();
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-a")) {
                    logger.info("Listening to all the messages...");
                    gatewayId = "*";
                    return;
                } else if (args[i].equals("-gId") && i + 1 < args.length) {
                    options.put("gId", args[++i]);
                } else if (args[i].equals("-eId") && i + 1 < args.length) {
                    options.put("eId", args[++i]);
                } else if (args[i].equals("-jId") && i + 1 < args.length) {
                    options.put("jId", args[++i]);
                }
            }

            gatewayId = options.getOrDefault("gId", "*");
            if (gatewayId.equals("*")) {
                logger.info("You have not specified a gateway id. We assume you need to listen to all the messages...");
            }

            experimentId = options.getOrDefault("eId", "*");
            if (experimentId.equals("*") && !gatewayId.equals("*")) {
                logger.info(
                        "You have not specified a experiment id. We assume you need to listen to all the messages for the gateway with id "
                                + gatewayId);
            } else if (experimentId.equals("*") && gatewayId.equals("*")) {
                logger.info(
                        "You have not specified a experiment id and a gateway id. We assume you need to listen to all the messages...");
            }

            jobId = options.getOrDefault("jId", "*");
            if (jobId.equals("*") && !gatewayId.equals("*") && !experimentId.equals("*")) {
                logger.info(
                        "You have not specified a job id. We assume you need to listen to all the messages for the gateway with id "
                                + gatewayId + " with experiment id : " + experimentId);
            } else if (jobId.equals("*") && gatewayId.equals("*") && experimentId.equals("*")) {
                logger.info(
                        "You have not specified a job Id or experiment Id or a gateway Id. We assume you need to listen to all the messages...");
            }
        } catch (Exception e) {
            logger.error("Error while reading command line parameters", e);
        }
    }
}
