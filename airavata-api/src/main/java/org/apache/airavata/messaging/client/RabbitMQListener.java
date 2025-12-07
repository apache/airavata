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
package org.apache.airavata.messaging.client;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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
            Options options = new Options();

            options.addOption("gId", true, "Gateway ID");
            options.addOption("eId", true, "Experiment ID");
            options.addOption("jId", true, "Job ID");
            options.addOption("a", false, "All Notifications");

            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.getOptions() == null || cmd.getOptions().length == 0) {
                logger.info("You have not specified any options. We assume you need to listen to all the messages...");
                gatewayId = "*";
            }
            if (cmd.hasOption("a")) {
                logger.info("Listening to all the messages...");
                gatewayId = "*";
            } else {
                gatewayId = cmd.getOptionValue("gId");
                if (gatewayId == null) {
                    gatewayId = "*";
                    logger.info(
                            "You have not specified a gateway id. We assume you need to listen to all the messages...");
                }
                experimentId = cmd.getOptionValue("eId");
                if (experimentId == null && !gatewayId.equals("*")) {
                    experimentId = "*";
                    logger.info(
                            "You have not specified a experiment id. We assume you need to listen to all the messages for the gateway with id "
                                    + gatewayId);
                } else if (experimentId == null && gatewayId.equals("*")) {
                    experimentId = "*";
                    logger.info(
                            "You have not specified a experiment id and a gateway id. We assume you need to listen to all the messages...");
                }
                jobId = cmd.getOptionValue("jId");
                if (jobId == null && !gatewayId.equals("*") && !experimentId.equals("*")) {
                    jobId = "*";
                    logger.info(
                            "You have not specified a job id. We assume you need to listen to all the messages for the gateway with id "
                                    + gatewayId + " with experiment id : " + experimentId);
                } else if (jobId == null && gatewayId.equals("*") && experimentId.equals("*")) {
                    jobId = "*";
                    logger.info(
                            "You have not specified a job Id or experiment Id or a gateway Id. We assume you need to listen to all the messages...");
                }
            }
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters", e);
        }
    }

    // Unused enum - commented out
    /*
    private enum LEVEL {
        ALL,
        GATEWAY,
        EXPERIMENT,
        JOB;
    }
    */
}
