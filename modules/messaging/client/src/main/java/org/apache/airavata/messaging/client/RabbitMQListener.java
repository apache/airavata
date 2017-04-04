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
package org.apache.airavata.messaging.client;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.messaging.core.Type;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class RabbitMQListener {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private final static Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);
    private static String gatewayId = "*";
    private static String experimentId = "*";
    private static String jobId = "*";
    private static LEVEL level = LEVEL.ALL;

    public static void main(String[] args) {
        File file = new File("/tmp/latency_client");
        parseArguments(args);
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            String brokerUrl = ServerSettings.getSetting(RABBITMQ_BROKER_URL);
            System.out.println("broker url " + brokerUrl);
            final String exchangeName = ServerSettings.getSetting(RABBITMQ_EXCHANGE_NAME);
            List<String> routingKeys = getRoutingKeys(level);
            Subscriber subscriber = MessagingFactory.getSubscriber(message -> {}, routingKeys, Type.STATUS);
        } catch (ApplicationSettingsException e) {
            logger.error("Error reading airavata server properties", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private static MessageHandler getMessageHandler(final BufferedWriter bw) {
        return message -> {
            try {
                long latency = System.currentTimeMillis() - message.getUpdatedTime().getTime();
                bw.write(message.getMessageId() + " :" + latency);
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message.getType().equals(MessageType.EXPERIMENT)) {
                try {
                    ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType() + "' and with state : '" + event.getState().toString() +
                            " for Gateway " + event.getGatewayId());
                } catch (TException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (message.getType().equals(MessageType.PROCESS)) {
                        /*try {
                            WorkflowNodeStatusChangeEvent event = new WorkflowNodeStatusChangeEvent();
                            TBase messageEvent = message.getEvent();
                            byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                            ThriftUtils.createThriftFromBytes(bytes, event);
                            System.out.println(" Message Received with message id '" + message.getMessageId()
                                    + "' and with message type '" + message.getType() + "' and with state : '" + event.getState().toString() +
                                    " for Gateway " + event.getWorkflowNodeIdentity().getGatewayId());
                        } catch (TException e) {
                            logger.error(e.getMessage(), e);
                        }*/
            } else if (message.getType().equals(MessageType.TASK)) {
                try {
                    TaskStatusChangeEvent event = new TaskStatusChangeEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType() + "' and with state : '" + event.getState().toString() +
                            " for Gateway " + event.getTaskIdentity().getGatewayId());
                } catch (TException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (message.getType().equals(MessageType.JOB)) {
                try {
                    JobStatusChangeEvent event = new JobStatusChangeEvent();
                    TBase messageEvent = message.getEvent();
                    byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                    ThriftUtils.createThriftFromBytes(bytes, event);
                    System.out.println(" Message Received with message id '" + message.getMessageId()
                            + "' and with message type '" + message.getType() + "' and with state : '" + event.getState().toString() +
                            " for Gateway " + event.getJobIdentity().getGatewayId());
                } catch (TException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        };

    }

    private static List<String> getRoutingKeys(LEVEL level) {
        List<String> routingKeys = new ArrayList<String>();
        switch (level) {
            case ALL:
                routingKeys.add("*");
                routingKeys.add("*.*");
                routingKeys.add("*.*.*");
                routingKeys.add("*.*.*.*");
                routingKeys.add("*.*.*.*.*");
                break;
            case GATEWAY:
                routingKeys.add(gatewayId);
                routingKeys.add(gatewayId + ".*");
                routingKeys.add(gatewayId + ".*.*");
                routingKeys.add(gatewayId + ".*.*.*");
                routingKeys.add(gatewayId + ".*.*.*.*");
                break;
            case EXPERIMENT:
                routingKeys.add(gatewayId);
                routingKeys.add(gatewayId + "." + experimentId);
                routingKeys.add(gatewayId + "." + experimentId + ".*");
                routingKeys.add(gatewayId + "." + experimentId + ".*.*");
                routingKeys.add(gatewayId + "." + experimentId + ".*.*.*");
                break;
            case JOB:
                routingKeys.add(gatewayId);
                routingKeys.add(gatewayId + "." + experimentId);
                routingKeys.add(gatewayId + "." + experimentId + ".*");
                routingKeys.add(gatewayId + "." + experimentId + ".*.*");
                routingKeys.add(gatewayId + "." + experimentId + ".*." + jobId);
                break;
            default:
                break;
        }
        return routingKeys;
    }

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
                level = LEVEL.ALL;
                gatewayId = "*";
            }
            if (cmd.hasOption("a")) {
                logger.info("Listening to all the messages...");
                level = LEVEL.ALL;
                gatewayId = "*";
            } else {
                gatewayId = cmd.getOptionValue("gId");
                if (gatewayId == null) {
                    gatewayId = "*";
                    logger.info("You have not specified a gateway id. We assume you need to listen to all the messages...");
                } else {
                    level = LEVEL.GATEWAY;
                }
                experimentId = cmd.getOptionValue("eId");
                if (experimentId == null && !gatewayId.equals("*")) {
                    experimentId = "*";
                    logger.info("You have not specified a experiment id. We assume you need to listen to all the messages for the gateway with id " + gatewayId);
                } else if (experimentId == null && gatewayId.equals("*")) {
                    experimentId = "*";
                    logger.info("You have not specified a experiment id and a gateway id. We assume you need to listen to all the messages...");
                } else {
                    level = LEVEL.EXPERIMENT;
                }
                jobId = cmd.getOptionValue("jId");
                if (jobId == null && !gatewayId.equals("*") && !experimentId.equals("*")) {
                    jobId = "*";
                    logger.info("You have not specified a job id. We assume you need to listen to all the messages for the gateway with id " + gatewayId
                            + " with experiment id : " + experimentId);
                } else if (jobId == null && gatewayId.equals("*") && experimentId.equals("*")) {
                    jobId = "*";
                    logger.info("You have not specified a job Id or experiment Id or a gateway Id. We assume you need to listen to all the messages...");
                } else {
                    level = LEVEL.JOB;
                }
            }
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters", e);
        }
    }

    private enum LEVEL {
        ALL,
        GATEWAY,
        EXPERIMENT,
        JOB;
    }
}

