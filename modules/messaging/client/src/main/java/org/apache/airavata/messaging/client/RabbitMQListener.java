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

package org.apache.airavata.messaging.client;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusConsumer;
import org.apache.airavata.model.messaging.event.*;
import org.apache.commons.cli.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RabbitMQListener {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private final static Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);
    private static String gatewayId = "*";
    private static boolean gatewayLevelMessages = false;
    private static boolean experimentLevelMessages = false;
    private static boolean jobLevelMessages = false;
    private static String experimentId = "*";
    private static String jobId = "*";
    private static boolean allMessages = false;

    public static void main(String[] args) {
        File file = new File("/tmp/latency_client");
        parseArguments(args);
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            String brokerUrl = ServerSettings.getSetting(RABBITMQ_BROKER_URL);
            System.out.println("broker url " + brokerUrl);
            final String exchangeName = ServerSettings.getSetting(RABBITMQ_EXCHANGE_NAME);
            RabbitMQStatusConsumer consumer = new RabbitMQStatusConsumer(brokerUrl, exchangeName);
            consumer.listen(new MessageHandler() {
                @Override
                public Map<String, Object> getProperties() {
                    Map<String, Object> props = new HashMap<String, Object>();
                    List<String> routingKeys = new ArrayList<String>();
                    if (allMessages){
                        routingKeys.add("*");
                        routingKeys.add("*.*");
                        routingKeys.add("*.*.*");
                        routingKeys.add("*.*.*.*");
                        routingKeys.add("*.*.*.*.*");
                    }else {
                        if (gatewayLevelMessages){
                            routingKeys.add(gatewayId);
                            routingKeys.add(gatewayId + ".*");
                            routingKeys.add(gatewayId + ".*.*");
                            routingKeys.add(gatewayId + ".*.*.*");
                            routingKeys.add(gatewayId + ".*.*.*.*");
                        }else if (experimentLevelMessages){
                            routingKeys.add(gatewayId);
                            routingKeys.add(gatewayId + "." + experimentId);
                            routingKeys.add(gatewayId + "." + experimentId+ ".*");
                            routingKeys.add(gatewayId + "." + experimentId+ ".*.*");
                            routingKeys.add(gatewayId + "." + experimentId+ ".*.*.*");
                        }else if  (jobLevelMessages){
                            routingKeys.add(gatewayId);
                            routingKeys.add(gatewayId + "." + experimentId);
                            routingKeys.add(gatewayId + "." + experimentId+ ".*");
                            routingKeys.add(gatewayId + "." + experimentId+ ".*.*");
                            routingKeys.add(gatewayId + "." + experimentId+ ".*." + jobId);
                        }
                    }
                    props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
                    return props;
                }

                @Override
                public void onMessage(MessageContext message) {
                    try {
                        long latency = System.currentTimeMillis() - message.getUpdatedTime().getTime();
                        bw.write(message.getMessageId() + " :" + latency);
                        bw.newLine();
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (message.getType().equals(MessageType.EXPERIMENT)){
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
                    }else if (message.getType().equals(MessageType.PROCESS)){
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
                    }else if (message.getType().equals(MessageType.TASK)){
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
                    }else if (message.getType().equals(MessageType.JOB)){
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
                }
            });
        } catch (ApplicationSettingsException e) {
            logger.error("Error reading airavata server properties", e);
        }catch (Exception e) {
           logger.error(e.getMessage(), e);
        }

    }

    public static void parseArguments(String[] args) {
        try{
            Options options = new Options();

            options.addOption("gId", true , "Gateway ID");
            options.addOption("eId", true, "Experiment ID");
            options.addOption("jId", true, "Job ID");
            options.addOption("a", false, "All Notifications");

            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);
            if (cmd.getOptions() == null || cmd.getOptions().length == 0){
                logger.info("You have not specified any options. We assume you need to listen to all the messages...");
                allMessages = true;
                gatewayId = "*";
            }
            if (cmd.hasOption("a")){
                logger.info("Listening to all the messages...");
                allMessages = true;
                gatewayId = "*";
            }else {
                gatewayId = cmd.getOptionValue("gId");
                if (gatewayId == null){
                    gatewayId = "*";
                    logger.info("You have not specified a gateway id. We assume you need to listen to all the messages...");
                } else {
                    gatewayLevelMessages = true;
                }
                experimentId = cmd.getOptionValue("eId");
                if (experimentId == null && !gatewayId.equals("*")){
                    experimentId = "*";
                    logger.info("You have not specified a experiment id. We assume you need to listen to all the messages for the gateway with id " + gatewayId);
                } else if (experimentId == null && gatewayId.equals("*")) {
                    experimentId = "*";
                    logger.info("You have not specified a experiment id and a gateway id. We assume you need to listen to all the messages...");
                }else {
                    experimentLevelMessages = true;
                }
                jobId = cmd.getOptionValue("jId");
                if (jobId == null && !gatewayId.equals("*") && !experimentId.equals("*")){
                    jobId = "*";
                    logger.info("You have not specified a job id. We assume you need to listen to all the messages for the gateway with id " + gatewayId
                            + " with experiment id : " + experimentId );
                } else if (jobId == null && gatewayId.equals("*") && experimentId.equals("*")) {
                    jobId = "*";
                    logger.info("You have not specified a job Id or experiment Id or a gateway Id. We assume you need to listen to all the messages...");
                }else {
                    jobLevelMessages = true;
                }
            }
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters" , e);
        }
    }
}
